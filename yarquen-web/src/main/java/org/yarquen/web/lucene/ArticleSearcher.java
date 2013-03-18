package org.yarquen.web.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.index.FacetFields;
import org.apache.lucene.facet.search.DrillDown;
import org.apache.lucene.facet.search.FacetsCollector;
import org.apache.lucene.facet.search.params.CountFacetRequest;
import org.apache.lucene.facet.search.params.FacetSearchParams;
import org.apache.lucene.facet.search.results.FacetResult;
import org.apache.lucene.facet.search.results.FacetResultNode;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.yarquen.account.Skill;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.category.CategoryBranch;
import org.yarquen.category.CategoryService;
import org.yarquen.web.search.SearchFields;
import org.yarquen.web.search.SearchResult;
import org.yarquen.web.search.SkillYarquenFacet;
import org.yarquen.web.search.YarquenFacet;
import org.yarquen.web.search.YarquenFacets;

/**
 * Article search component
 * 
 * FIXME: refactor this mess
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * @version $Id$
 * 
 */
public class ArticleSearcher {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ArticleSearcher.class);

	private static final int MAX_FACETS = 15;
	private static final int MAX_RESULTS = 100;

	private Analyzer analyzer;
	@Resource
	private ArticleRepository articleRepository;
	@Resource
	private CategoryService categoryService;
	private Directory indexDirectory;
	@Value("#{config.indexDirectory}")
	private String indexDirectoryPath;
	private DirectoryReader indexReader;
	private IndexWriter indexWriter;
	private IndexSearcher searcher;
	private Directory taxoDirectory;
	@Value("#{config.taxoDirectory}")
	private String taxoDirectoryPath;
	private TaxonomyReader taxoReader;
	private TaxonomyWriter taxoWriter;

	@PreDestroy
	public void destroy() throws IOException {
		LOGGER.info("closing readers");
		taxoReader.close();
		indexReader.close();

		LOGGER.info("closing writers");
		taxoWriter.close();
		indexWriter.close();

		LOGGER.info("closing directories");
		indexDirectory.close();
		taxoDirectory.close();
	}

	@PostConstruct
	public void init() throws IOException {
		analyzer = new StandardAnalyzer(Version.LUCENE_41);
		final IndexWriterConfig config = new IndexWriterConfig(
				Version.LUCENE_41, analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);
		indexDirectory = new NIOFSDirectory(new File(indexDirectoryPath));
		indexWriter = new IndexWriter(indexDirectory, config);
		indexReader = DirectoryReader.open(indexWriter, true);

		taxoDirectory = new NIOFSDirectory(new File(taxoDirectoryPath));
		taxoWriter = new DirectoryTaxonomyWriter(taxoDirectory,
				OpenMode.CREATE_OR_APPEND);
		taxoReader = new DirectoryTaxonomyReader(taxoDirectory);

		searcher = new IndexSearcher(indexReader);
	}

	public void reindexArticle(Article article) throws IOException {
		// remove current doc from index
		indexWriter.deleteDocuments(new Term(Article.Fields.ID.toString(),
				article.getId()));

		// new doc
		final Document doc = new Document();
		// add fields
		addFieldsToDoc(doc, article);
		// add facets
		addFacetsToDoc(doc, article);

		// add doc to index
		indexWriter.addDocument(doc);

		// commit change (taxo first!)
		taxoWriter.commit();
		indexWriter.commit();
	}

	public List<SearchResult> search(SearchFields searchFields,
			YarquenFacets facetsCount) throws IOException, ParseException {
		final String queryString = searchFields.getQuery();
		LOGGER.debug("searching: {}", queryString);

		boolean recreateIndexSearcher = false;
		// check for index changes
		final DirectoryReader newIndexReader = DirectoryReader.openIfChanged(
				indexReader, indexWriter, true);
		if (newIndexReader != null) {
			LOGGER.trace("reopening index reader...");
			DirectoryReader oldIndexReader = indexReader;
			indexReader = newIndexReader;
			oldIndexReader.close();

			recreateIndexSearcher = true;
		}
		// check for taxo index changes
		final TaxonomyReader newTaxoReader = TaxonomyReader
				.openIfChanged(taxoReader);
		if (newTaxoReader != null) {
			LOGGER.trace("reopening taxo reader...");
			taxoReader.close();
			taxoReader = newTaxoReader;

			recreateIndexSearcher = true;
		}

		if (recreateIndexSearcher) {
			searcher = new IndexSearcher(indexReader);
		}

		LOGGER.trace("max results: {}", searchFields.getResults());
		final int numberOfResults = searchFields.getResults() == null ? MAX_RESULTS
				: searchFields.getResults();
		final TopScoreDocCollector collector = TopScoreDocCollector.create(
				numberOfResults, true);

		// facets to gather
		LOGGER.trace("max facets: {}", searchFields.getFacets());
		final int numberOfFacets = searchFields.getFacets() == null ? MAX_FACETS
				: searchFields.getFacets();
		final FacetSearchParams facetSearchParams = createFacetSearchParams(
				numberOfFacets, searchFields);
		final FacetsCollector facetsCollector = new FacetsCollector(
				facetSearchParams, indexReader, taxoReader);

		// query construction
		// text query
		final Query textQuery = new MultiFieldQueryParser(Version.LUCENE_41,
				new String[] { Article.Fields.PLAIN_TEXT.toString(),
						Article.Fields.TITLE.toString(),
						Article.Fields.URL.toString() }, analyzer)
				.parse(queryString);
		// faceted query
		final Query facetedQuery = createFacetedQuery(textQuery, searchFields,
				facetSearchParams);

		// search
		searcher.search(facetedQuery != null ? facetedQuery : textQuery,
				MultiCollector.wrap(collector, facetsCollector));

		// populate facetsCount
		final List<FacetResult> facetResults = facetsCollector
				.getFacetResults();
		populateFacets(facetsCount, facetResults);
		if (LOGGER.isDebugEnabled()) {
			for (YarquenFacet fc : facetsCount.getAuthor()) {
				LOGGER.debug("author: {} = {}", fc.getValue(), fc.getCount());
			}
			for (YarquenFacet fc : facetsCount.getKeyword()) {
				LOGGER.debug("keyword: {} = {}", fc.getValue(), fc.getCount());
			}
			for (YarquenFacet fc : facetsCount.getYear()) {
				LOGGER.debug("year: {} = {}", fc.getValue(), fc.getCount());
			}
			for (YarquenFacet fc : facetsCount.getProvidedSkill()) {
				LOGGER.debug("providedSkill: {} = {}", fc.getValue(),
						fc.getCount());
			}
			for (YarquenFacet fc : facetsCount.getRequiredSkill()) {
				LOGGER.debug("requiredSkill: {} = {}", fc.getValue(),
						fc.getCount());
			}
		}

		final ScoreDoc[] hits = collector.topDocs().scoreDocs;
		final List<SearchResult> results = new ArrayList<SearchResult>(
				hits.length);
		LOGGER.debug("{} results from index", hits.length);
		for (ScoreDoc scoreDoc : hits) {
			final Document doc = searcher.doc(scoreDoc.doc);
			final IndexableField idField = doc.getField(Article.Fields.ID
					.toString());
			final String id = idField.stringValue();
			final Article article = articleRepository.findOne(id);
			if (article == null) {
				LOGGER.warn(
						"the article {} is indexed but doesn't exists in the database",
						id);
			} else {
				final SearchResult searchResult = createSearchResult(article);

				float score = (float) (Math.round(scoreDoc.score * 10.0) / 10.0);
				searchResult.setScore(score);

				LOGGER.trace("result:\n\ttitle:'{}'\n\turl:{}",
						searchResult.getTitle(), searchResult.getUrl());

				results.add(searchResult);
			}
		}
		return results;
	}

	private void addFacetsToDoc(Document doc, Article article)
			throws IOException {
		final List<CategoryPath> facets = new ArrayList<CategoryPath>();

		if (article.getAuthor() != null) {
			facets.add(new CategoryPath(Article.Facets.AUTHOR.toString(),
					article.getAuthor()));
		}
		if (article.getDate() != null) {
			final String date = article.getDate();
			// FIXME: the date shouldn't be a plain String :o
			final String year = date.substring(date.lastIndexOf("/") + 1);
			facets.add(new CategoryPath(Article.Facets.YEAR.toString(), year));
		}
		if (article.getKeywords() != null) {
			for (String kw : article.getKeywords()) {
				facets.add(new CategoryPath(Article.Facets.KEYWORD.toString(),
						kw));
			}
		}
		if (article.getProvidedSkills() != null) {
			for (Skill skill : article.getProvidedSkills()) {
				final String[] components = skill
						.getCodeAsArray(Article.Facets.PROVIDED_SKILL
								.toString());
				LOGGER.trace(
						"article={}, adding CategoryPath {} to lucene taxo index",
						article.getId(), components);
				final CategoryPath categoryPath = new CategoryPath(components);
				facets.add(categoryPath);
			}
		}
		if (article.getRequiredSkills() != null) {
			for (Skill skill : article.getRequiredSkills()) {
				final String[] components = skill
						.getCodeAsArray(Article.Facets.REQUIRED_SKILL
								.toString());
				LOGGER.trace(
						"article={}, adding CategoryPath {} to lucene taxo index",
						article.getId(), components);
				final CategoryPath categoryPath = new CategoryPath(components);
				facets.add(categoryPath);
			}
		}

		// TODO: maybe we can reuse this instance
		FacetFields facetDocumentBuilder = new FacetFields(taxoWriter);
		facetDocumentBuilder.addFields(doc, facets);
	}

	private void addFieldsToDoc(Document doc, Article article) {
		doc.add(new TextField(Article.Fields.ID.toString(), article.getId(),
				Field.Store.YES));
		doc.add(new TextField(Article.Fields.PLAIN_TEXT.toString(), article
				.getPlainText(), Field.Store.YES));
		doc.add(new TextField(Article.Fields.TITLE.toString(), article
				.getTitle(), Field.Store.YES));
		doc.add(new StringField(Article.Fields.URL.toString(),
				article.getUrl(), Field.Store.YES));
	}

	private YarquenFacet createFacet(FacetResultNode facetResultNode) {
		final String name = facetResultNode.getLabel().components[0];
		final String value = facetResultNode.getLabel().components[1];
		final double count = facetResultNode.getValue();

		final YarquenFacet yfacet = new YarquenFacet();
		yfacet.setName(name);
		yfacet.setCode(value);
		yfacet.setValue(value);
		yfacet.setCount((int) count);
		return yfacet;
	}

	private Query createFacetedQuery(Query textQuery,
			SearchFields searchFields, FacetSearchParams facetSearchParams) {
		boolean facetedSearch = false;
		final List<CategoryPath> facets = new ArrayList<CategoryPath>();

		// author
		final String author = searchFields.getAuthor();
		if (author != null) {
			facetedSearch = true;
			facets.add(new CategoryPath(Article.Facets.AUTHOR.toString(),
					author));
		}
		// year
		final String year = searchFields.getYear();
		if (year != null) {
			facetedSearch = true;
			facets.add(new CategoryPath(Article.Facets.YEAR.toString(), year));
		}
		// keywords
		final List<String> keywordValues = searchFields.getKeyword();
		if (keywordValues != null && !keywordValues.isEmpty()) {
			facetedSearch = true;
			for (String kw : keywordValues) {
				facets.add(new CategoryPath(Article.Facets.KEYWORD.toString(),
						kw));
			}
		}
		// provided skills
		final List<Skill> providedSkills = searchFields.getProvidedSkill();
		if (providedSkills != null && !providedSkills.isEmpty()) {
			facetedSearch = true;
			for (Skill skill : providedSkills) {
				final String[] components = skill
						.getCodeAsArray(Article.Facets.PROVIDED_SKILL
								.toString());
				LOGGER.trace("adding provided skill facet to query: {}{}",
						components, "");
				facets.add(new CategoryPath(components));
			}
		}
		// required skills
		final List<Skill> requiredSkills = searchFields.getRequiredSkill();
		if (requiredSkills != null && !requiredSkills.isEmpty()) {
			facetedSearch = true;
			for (Skill skill : requiredSkills) {
				final String[] components = skill
						.getCodeAsArray(Article.Facets.REQUIRED_SKILL
								.toString());
				LOGGER.trace("adding required skill facet to query: {}{}",
						components, "");
				facets.add(new CategoryPath(components));
			}
		}

		if (facetedSearch) {
			LOGGER.debug(
					"faceted search: author={} year={} keywords={} providedSkills={} requiredSkills={}",
					new Object[] { author, year, keywordValues, providedSkills,
							requiredSkills });
			return DrillDown.query(facetSearchParams, textQuery,
					facets.toArray(new CategoryPath[facets.size()]));
		} else {
			return null;
		}
	}

	private FacetSearchParams createFacetSearchParams(int numberOfFacets,
			SearchFields searchFields) {
		LOGGER.trace("creating faceted search params...");

		LOGGER.trace("providedSkills: {}, requiredSkills: {}",
				searchFields.getProvidedSkill(),
				searchFields.getRequiredSkill());
		final int providedSkillsSelected = searchFields.getProvidedSkill() != null ? searchFields
				.getProvidedSkill().size() : 0;
		final int requiredSkillsSelected = searchFields.getRequiredSkill() != null ? searchFields
				.getRequiredSkill().size() : 0;
		LOGGER.trace("providedSkills selected = {}", providedSkillsSelected);
		LOGGER.trace("requiredSkills selected = {}", requiredSkillsSelected);

		// I have to count author, keyword, year, providedSkill and
		// requiredSkill
		int i = 0;
		final CountFacetRequest[] countFacetRequests = new CountFacetRequest[5
				+ providedSkillsSelected + requiredSkillsSelected];
		countFacetRequests[i++] = new CountFacetRequest(new CategoryPath(
				Article.Facets.AUTHOR.toString()), numberOfFacets);
		countFacetRequests[i++] = new CountFacetRequest(new CategoryPath(
				Article.Facets.KEYWORD.toString()), numberOfFacets);
		countFacetRequests[i++] = new CountFacetRequest(new CategoryPath(
				Article.Facets.YEAR.toString()), numberOfFacets);
		countFacetRequests[i++] = new CountFacetRequest(new CategoryPath(
				Article.Facets.PROVIDED_SKILL.toString()), numberOfFacets);
		countFacetRequests[i++] = new CountFacetRequest(new CategoryPath(
				Article.Facets.REQUIRED_SKILL.toString()), numberOfFacets);

		if (providedSkillsSelected > 0) {
			for (Skill skill : searchFields.getProvidedSkill()) {
				final String[] components = skill
						.getCodeAsArray(Article.Facets.PROVIDED_SKILL
								.toString());
				LOGGER.trace(
						"counting facet providedSkill code={} components={}",
						skill.getAsText(), components);
				final CategoryPath categoryPath = new CategoryPath(components);
				countFacetRequests[i++] = new CountFacetRequest(categoryPath,
						numberOfFacets);
			}
		}
		if (requiredSkillsSelected > 0) {
			for (Skill skill : searchFields.getRequiredSkill()) {
				final String[] components = skill
						.getCodeAsArray(Article.Facets.REQUIRED_SKILL
								.toString());
				LOGGER.trace(
						"counting facet requiredSkill code={} components={}",
						skill.getAsText(), components);
				final CategoryPath categoryPath = new CategoryPath(components);
				countFacetRequests[i++] = new CountFacetRequest(categoryPath,
						numberOfFacets);
			}
		}
		LOGGER.trace("{} count facet requests (index={})",
				countFacetRequests.length, i - 1);
		return new FacetSearchParams(countFacetRequests);
	}

	private SearchResult createSearchResult(Article article) {
		final SearchResult searchResult = new SearchResult();
		searchResult.setId(article.getId());
		searchResult.setUrl(article.getUrl());
		searchResult.setTitle(article.getTitle());
		searchResult.setDate(article.getDate());
		searchResult.setAuthor(article.getAuthor());
		final String summary = article.getSummary();
		if (summary == null || summary.trim().length() == 0) {
			searchResult.setSummary("No summary was found for this result :(");
		} else {
			searchResult.setSummary(summary);
		}

		final List<String> keywords = new ArrayList<String>();
		if (article.getKeywords() != null) {
			for (String kw : article.getKeywords()) {
				keywords.add(kw);
			}
		}
		searchResult.setKeywords(keywords);

		return searchResult;
	}

	// TODO:improve this (maybe encapsulate in skill)
	private void createSkillFacet(FacetResultNode facetResultNode,
			List<SkillYarquenFacet> skillFacets) {
		final String[] components = facetResultNode.getLabel().components;
		final String lastComponent = components[components.length - 1];
		// TODO: improve this
		boolean leaf = lastComponent.equals("1") || lastComponent.equals("2")
				|| lastComponent.equals("3");

		LOGGER.trace("creating skill facet from {} subResults:{}", components,
				facetResultNode.getNumSubResults());

		// first element tell us if is a provided or required skill
		final String name = components[0];

		// if it's a leaf, discard last element (level)
		final int branchLength = leaf ? components.length - 1
				: components.length;
		// element from index 2 to branchLength-1 forms the category branch code
		final CategoryBranch branch = new CategoryBranch();
		for (int i = 1; i < branchLength; i++) {
			branch.addSubCategory(components[i], null);
		}
		categoryService.completeCategoryBranchNodeNames(branch);
		final String code = branch.getCode();
		final String value = branch.getName();

		final SkillYarquenFacet yfacet = new SkillYarquenFacet();
		yfacet.setName(name);
		yfacet.setCode(code);
		yfacet.setValue(value);
		yfacet.setCount((int) facetResultNode.getValue());
		if (leaf) {
			// last element contains the code level
			final int level = Integer
					.valueOf(components[components.length - 1]);
			yfacet.setLevel(level);
			// ughhh
			yfacet.setLevelName(Skill.Level.parse(level).getName());
		}
		// else: level will be 0 and levelName null
		LOGGER.trace("leaf:{} level:{} levelName:{}", new Object[] { leaf,
				yfacet.getLevel(), yfacet.getLevelName() });

		if (!skillFacets.contains(yfacet)) {
			skillFacets.add(yfacet);
		}

		// sub facets
		final int numSubResults = facetResultNode.getNumSubResults();
		LOGGER.trace("numSubResults = {}", numSubResults);
		if (numSubResults > 0) {
			for (FacetResultNode subResult : facetResultNode.getSubResults()) {
				createSkillFacet(subResult, skillFacets);
			}
		}
	}

	private void logThisShit(FacetResultNode facetResultNode, int n) {
		LOGGER.trace("({}) result!: {}", n,
				facetResultNode.getLabel().components);
		Iterable<? extends FacetResultNode> subResults = facetResultNode
				.getSubResults();
		for (FacetResultNode facetResultNode2 : subResults) {
			logThisShit(facetResultNode2, n + 1);
		}
	}

	private void populateFacets(YarquenFacets facetsCount,
			List<FacetResult> facetResults) {
		int fc = 0;

		// author
		facetsCount.setAuthor(new ArrayList<YarquenFacet>());
		final FacetResultNode authorFacetCount = facetResults.get(fc++)
				.getFacetResultNode();
		final Iterable<? extends FacetResultNode> authorFacetsResults = authorFacetCount
				.getSubResults();
		for (FacetResultNode facetResultNode : authorFacetsResults) {
			facetsCount.getAuthor().add(createFacet(facetResultNode));
		}

		// keyword
		facetsCount.setKeyword(new ArrayList<YarquenFacet>());
		final FacetResultNode keywordFacetCount = facetResults.get(fc++)
				.getFacetResultNode();
		final Iterable<? extends FacetResultNode> keywordFacetsResults = keywordFacetCount
				.getSubResults();
		for (FacetResultNode facetResultNode : keywordFacetsResults) {
			facetsCount.getKeyword().add(createFacet(facetResultNode));
		}

		// year
		facetsCount.setYear(new ArrayList<YarquenFacet>());
		final FacetResultNode yearFacetCount = facetResults.get(fc++)
				.getFacetResultNode();
		final Iterable<? extends FacetResultNode> yearFacetsResults = yearFacetCount
				.getSubResults();
		for (FacetResultNode facetResultNode : yearFacetsResults) {
			facetsCount.getYear().add(createFacet(facetResultNode));
		}

		// provided skills
		facetsCount.setProvidedSkill(new ArrayList<SkillYarquenFacet>());
		final FacetResultNode providedSkillFacetCount = facetResults.get(fc++)
				.getFacetResultNode();
		logThisShit(providedSkillFacetCount, 0);
		LOGGER.trace("providedSkillFacetCount = {}", providedSkillFacetCount);
		final Iterable<? extends FacetResultNode> providedSkillFacetsResults = providedSkillFacetCount
				.getSubResults();
		for (FacetResultNode facetResultNode : providedSkillFacetsResults) {
			LOGGER.trace("node: {}", facetResultNode);
			createSkillFacet(facetResultNode, facetsCount.getProvidedSkill());
		}

		// required skills
		facetsCount.setRequiredSkill(new ArrayList<SkillYarquenFacet>());
		final FacetResultNode requiredSkillFacetCount = facetResults.get(fc++)
				.getFacetResultNode();
		logThisShit(requiredSkillFacetCount, 0);
		LOGGER.trace("requiredSkillFacetCount = {}", requiredSkillFacetCount);
		final Iterable<? extends FacetResultNode> requiredSkillFacetsResults = requiredSkillFacetCount
				.getSubResults();
		for (FacetResultNode facetResultNode : requiredSkillFacetsResults) {
			LOGGER.trace("node: {}", facetResultNode);
			createSkillFacet(facetResultNode, facetsCount.getRequiredSkill());
		}

		final int lastIndex = 5;
		int results = facetResults.size();
		if (results > lastIndex) {
			for (int i = lastIndex; i < results; i++) {
				final FacetResultNode cfc = facetResults.get(i)
						.getFacetResultNode();
				final String skillFacetName = cfc.getLabel().components[0];
				if (skillFacetName.equals(Article.Facets.PROVIDED_SKILL
						.toString())) {
					createSkillFacet(cfc, facetsCount.getProvidedSkill());
				} else if (skillFacetName.equals(Article.Facets.REQUIRED_SKILL
						.toString())) {
					createSkillFacet(cfc, facetsCount.getRequiredSkill());
				} else {
					throw new RuntimeException("Unknow facet skill name: "
							+ skillFacetName);
				}
			}
		}

		// TODO: uggh, fix this
		final Comparator<YarquenFacet> simpleCodeComparator = new Comparator<YarquenFacet>() {
			@Override
			public int compare(YarquenFacet o1, YarquenFacet o2) {
				return o1.getCode().compareTo(o2.getCode());
			}
		};
		Collections.sort(facetsCount.getProvidedSkill(), simpleCodeComparator);
		Collections.sort(facetsCount.getRequiredSkill(), simpleCodeComparator);
	}
}
