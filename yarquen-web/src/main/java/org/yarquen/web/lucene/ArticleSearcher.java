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
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.category.CategoryBranch;
import org.yarquen.category.CategoryService;
import org.yarquen.web.search.SearchFields;
import org.yarquen.web.search.SearchResult;
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
	@Resource
	private CategoryService categoryService;

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
			for (YarquenFacet fc : facetsCount.getCategory()) {
				LOGGER.debug("category: {} = {}", fc.getValue(), fc.getCount());
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
		if (article.getCategories() != null) {
			for (CategoryBranch branch : article.getCategories()) {
				final CategoryPath categoryPath = new CategoryPath(
						branch.getCodeAsArray(Article.Facets.CATEGORY
								.toString()));
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

	private void createCategoryFacet(FacetResultNode facetResultNode,
			List<YarquenFacet> categoryFacets) {
		// TODO:improve this (maybe encapsulate in CategoryBranch)
		final CategoryBranch branch = new CategoryBranch();
		for (int i = 1; i < facetResultNode.getLabel().components.length; i++) {
			branch.addSubCategory(facetResultNode.getLabel().components[i],
					null);
		}
		categoryService.completeCategoryBranchNodeNames(branch);

		final String name = Article.Facets.CATEGORY.toString();
		final String code = branch.getCode();
		final String value = branch.getName();
		final double count = facetResultNode.getValue();

		final YarquenFacet yfacet = new YarquenFacet();
		yfacet.setName(name);
		yfacet.setCode(code);
		yfacet.setValue(value);
		yfacet.setCount((int) count);

		if (!categoryFacets.contains(yfacet)) {
			categoryFacets.add(yfacet);
		}

		// sub facets
		final int numSubResults = facetResultNode.getNumSubResults();
		LOGGER.trace("numSubResults = {}", numSubResults);
		if (numSubResults > 0) {
			for (FacetResultNode subResult : facetResultNode.getSubResults()) {
				createCategoryFacet(subResult, categoryFacets);
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
		// categories
		final List<String> categoryValues = searchFields.getCategory();
		if (categoryValues != null && !categoryValues.isEmpty()) {
			facetedSearch = true;
			for (String category : categoryValues) {
				final CategoryBranch incompleteBranch = CategoryBranch
						.incompleteFromCode(category);
				final String[] components = incompleteBranch
						.getCodeAsArray(Article.Facets.CATEGORY.toString());
				facets.add(new CategoryPath(components));
			}
		}

		if (facetedSearch) {
			LOGGER.debug(
					"faceted search: author={} year={} keywords={} categories={}",
					new Object[] { author, year, keywordValues, categoryValues });
			return DrillDown.query(facetSearchParams, textQuery,
					facets.toArray(new CategoryPath[facets.size()]));
		} else {
			return null;
		}
	}

	private FacetSearchParams createFacetSearchParams(int numberOfFacets,
			SearchFields searchFields) {
		final int selectedCategories = searchFields.getCategory() != null ? searchFields
				.getCategory().size() : 0;

		// I have to count author, keyword, year, category and each category
		// branch
		final CountFacetRequest[] countFacetRequests = new CountFacetRequest[4 + selectedCategories];
		countFacetRequests[0] = new CountFacetRequest(new CategoryPath(
				Article.Facets.AUTHOR.toString()), numberOfFacets);
		countFacetRequests[1] = new CountFacetRequest(new CategoryPath(
				Article.Facets.KEYWORD.toString()), numberOfFacets);
		countFacetRequests[2] = new CountFacetRequest(new CategoryPath(
				Article.Facets.YEAR.toString()), numberOfFacets);
		countFacetRequests[3] = new CountFacetRequest(new CategoryPath(
				Article.Facets.CATEGORY.toString()), numberOfFacets);

		if (selectedCategories > 0) {
			int i = 4;
			for (String categoryBranchCode : searchFields.getCategory()) {
				final CategoryBranch incompleteFromCode = CategoryBranch
						.incompleteFromCode(categoryBranchCode);
				final String[] components = incompleteFromCode
						.getCodeAsArray(Article.Facets.CATEGORY.toString());
				LOGGER.trace("counting facet code={} components={}",
						categoryBranchCode, components);
				final CategoryPath categoryPath = new CategoryPath(components);
				countFacetRequests[i++] = new CountFacetRequest(categoryPath,
						numberOfFacets);
			}
		}
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

	private void populateFacets(YarquenFacets facetsCount,
			List<FacetResult> facetResults) {
		// author
		facetsCount.setAuthor(new ArrayList<YarquenFacet>());
		final FacetResultNode authorFacetCount = facetResults.get(0)
				.getFacetResultNode();
		final Iterable<? extends FacetResultNode> authorFacetsResults = authorFacetCount
				.getSubResults();
		for (FacetResultNode facetResultNode : authorFacetsResults) {
			facetsCount.getAuthor().add(createFacet(facetResultNode));
		}

		// keyword
		facetsCount.setKeyword(new ArrayList<YarquenFacet>());
		final FacetResultNode keywordFacetCount = facetResults.get(1)
				.getFacetResultNode();
		final Iterable<? extends FacetResultNode> keywordFacetsResults = keywordFacetCount
				.getSubResults();
		for (FacetResultNode facetResultNode : keywordFacetsResults) {
			facetsCount.getKeyword().add(createFacet(facetResultNode));
		}

		// year
		facetsCount.setYear(new ArrayList<YarquenFacet>());
		final FacetResultNode yearFacetCount = facetResults.get(2)
				.getFacetResultNode();
		final Iterable<? extends FacetResultNode> yearFacetsResults = yearFacetCount
				.getSubResults();
		for (FacetResultNode facetResultNode : yearFacetsResults) {
			facetsCount.getYear().add(createFacet(facetResultNode));
		}

		// category
		facetsCount.setCategory(new ArrayList<YarquenFacet>());
		final FacetResultNode categoryFacetCount = facetResults.get(3)
				.getFacetResultNode();
		logThisShit(categoryFacetCount, 0);
		LOGGER.trace("categoryFacetCount = {}", categoryFacetCount);
		final Iterable<? extends FacetResultNode> categoryFacetsResults = categoryFacetCount
				.getSubResults();
		for (FacetResultNode facetResultNode : categoryFacetsResults) {
			LOGGER.trace("node: {}", facetResultNode);
			createCategoryFacet(facetResultNode, facetsCount.getCategory());
		}

		int results = facetResults.size();
		if (results > 4) {
			for (int i = 4; i < results; i++) {
				final FacetResultNode cfc = facetResults.get(i)
						.getFacetResultNode();
				createCategoryFacet(cfc, facetsCount.getCategory());
			}
		}

		// TODO: uggh, fix this
		Collections.sort(facetsCount.getCategory(),
				new Comparator<YarquenFacet>() {
					@Override
					public int compare(YarquenFacet o1, YarquenFacet o2) {
						return o1.getCode().compareTo(o2.getCode());
					}
				});
	}
}
