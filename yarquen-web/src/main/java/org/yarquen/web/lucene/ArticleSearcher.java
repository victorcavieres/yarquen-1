package org.yarquen.web.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	// FIXME
	private static final char CPATH_DELIMITER = '.';
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
		final CountFacetRequest countAuthorFacet = new CountFacetRequest(
				new CategoryPath(Article.Facets.AUTHOR.toString()),
				numberOfFacets);
		final CountFacetRequest countKeywordFacet = new CountFacetRequest(
				new CategoryPath(Article.Facets.KEYWORD.toString()),
				numberOfFacets);
		final CountFacetRequest countYearFacet = new CountFacetRequest(
				new CategoryPath(Article.Facets.YEAR.toString()),
				numberOfFacets);
		final CountFacetRequest countCategoryFacet = new CountFacetRequest(
				new CategoryPath(Article.Facets.CATEGORY.toString()),
				numberOfFacets);
		final FacetSearchParams facetSearchParams = new FacetSearchParams(
				countAuthorFacet, countKeywordFacet, countYearFacet,
				countCategoryFacet);
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
		for (YarquenFacet fc : facetsCount.getAuthor()) {
			LOGGER.debug("{} = {}", fc.getValue(), fc.getCount());
		}
		for (YarquenFacet fc : facetsCount.getKeyword()) {
			LOGGER.debug("{} = {}", fc.getValue(), fc.getCount());
		}
		for (YarquenFacet fc : facetsCount.getYear()) {
			LOGGER.debug("{} = {}", fc.getValue(), fc.getCount());
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

	private YarquenFacet createFacet(FacetResultNode facetResultNode) {
		final String name = facetResultNode.getLabel().components[0];
		final String value = facetResultNode.getLabel().components[1];
		final double count = facetResultNode.getValue();

		final YarquenFacet yfacet = new YarquenFacet();
		yfacet.setName(name);
		yfacet.setValue(value);
		yfacet.setCount((int) count);
		return yfacet;
	}

	private Query createFacetedQuery(Query textQuery,
			SearchFields searchFields, FacetSearchParams facetSearchParams) {
		boolean facetedSearch = false;
		final List<CategoryPath> facets = new ArrayList<CategoryPath>();

		final String author = searchFields.getAuthor();
		if (author != null) {
			facetedSearch = true;
			facets.add(new CategoryPath(Article.Facets.AUTHOR.toString(),
					author));
		}
		final String year = searchFields.getYear();
		if (year != null) {
			facetedSearch = true;
			facets.add(new CategoryPath(Article.Facets.YEAR.toString(), year));
		}
		final List<String> keywordValues = searchFields.getKeyword();
		if (keywordValues != null && !keywordValues.isEmpty()) {
			facetedSearch = true;
			for (String kw : keywordValues) {
				facets.add(new CategoryPath(Article.Facets.KEYWORD.toString(),
						kw));
			}
		}
		// TODO: take categories in account

		if (facetedSearch) {
			LOGGER.debug("faceted search: author={} year={}", author, year);
			return DrillDown.query(facetSearchParams, textQuery,
					facets.toArray(new CategoryPath[facets.size()]));
		} else {
			return null;
		}
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
				final String branchCode = branch.getCode();
				// branch code is something like
				// 'Software.ProgrammingLanguages.C#', so we have to prepend
				// 'Category.' and use '.' as delimiter
				final CategoryPath categoryPath = new CategoryPath(
						Article.Facets.CATEGORY.toString() + CPATH_DELIMITER
								+ branchCode, CPATH_DELIMITER);
				facets.add(categoryPath);
			}
		}

		// FIXME
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
}
