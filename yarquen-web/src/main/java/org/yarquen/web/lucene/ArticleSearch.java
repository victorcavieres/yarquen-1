package org.yarquen.web.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.search.FacetsCollector;
import org.apache.lucene.facet.search.params.CountFacetRequest;
import org.apache.lucene.facet.search.params.FacetSearchParams;
import org.apache.lucene.facet.search.results.FacetResult;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
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
import org.springframework.stereotype.Component;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.web.search.SearchFields;
import org.yarquen.web.search.SearchResult;

/**
 * Article search component
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * @version $Id$
 * 
 */
@Component
public class ArticleSearch
{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ArticleSearch.class);

	private static final int MAX_RESULTS = 100;

	private StandardAnalyzer analyzer;
	@Resource
	private ArticleRepository articleRepository;
	@Value("#{config.indexDirectory}")
	private String indexDirectoryPath;
	private IndexReader indexReader;
	private IndexSearcher searcher;
	@Value("#{config.taxoDirectory}")
	private String taxoDirectoryPath;
	private TaxonomyReader taxoReader;

	@PostConstruct
	public void init() throws IOException
	{
		analyzer = new StandardAnalyzer(Version.LUCENE_40);

		final Directory indexDirectory = new NIOFSDirectory(new File(
				indexDirectoryPath));
		indexReader = DirectoryReader.open(indexDirectory);

		final Directory taxoDirectory = new NIOFSDirectory(new File(
				taxoDirectoryPath));
		taxoReader = new DirectoryTaxonomyReader(taxoDirectory);

		searcher = new IndexSearcher(indexReader);
	}

	public List<SearchResult> search(SearchFields searchFields)
			throws IOException, ParseException
	{
		final String query = searchFields.getQuery();
		LOGGER.debug("searching: {}", query);

		LOGGER.trace("max results: {}", searchFields.getResults());
		final int numberOfResults = searchFields.getResults() == null ? MAX_RESULTS
				: searchFields.getResults();

		final Query q = new MultiFieldQueryParser(Version.LUCENE_40,
				new String[] { Article.Fields.PLAIN_TEXT.toString(),
						Article.Fields.TITLE.toString(),
						Article.Fields.URL.toString() }, analyzer).parse(query);

		final String year = searchFields.getYear();
		if (year != null)
		{
			return facetedSearch(q, numberOfResults, year);
		}
		else
		{
			return textSearchOnly(q, numberOfResults);
		}
	}

	private SearchResult createSearchResult(Article article)
	{
		final SearchResult searchResult = new SearchResult();
		searchResult.setUrl(article.getUrl());
		searchResult.setTitle(article.getTitle());
		searchResult.setDate(article.getDate());
		searchResult.setAuthor(article.getAuthor());
		final String summary = article.getSummary();
		if (summary == null || summary.trim().length() == 0)
		{
			searchResult.setSummary("No summary was found for this result :(");
		}
		else
		{
			searchResult.setSummary(summary);
		}

		final List<String> keywords = new ArrayList<String>();
		if (article.getKeywords() != null)
		{
			for (String kw : article.getKeywords())
			{
				keywords.add(kw);
			}
		}
		searchResult.setKeywords(keywords);

		return searchResult;
	}

	private List<SearchResult> facetedSearch(Query query, int numberOfResults,
			String year) throws IOException, ParseException
	{
		LOGGER.debug("faceted search: year={}", year);

		final TopScoreDocCollector collector = TopScoreDocCollector.create(
				numberOfResults, true);

		final FacetSearchParams facetSearchParams = new FacetSearchParams();
		facetSearchParams.addFacetRequest(new CountFacetRequest(
				new CategoryPath(Article.Facets.YEAR.toString()),
				numberOfResults));

		final FacetsCollector facetsCollector = new FacetsCollector(
				facetSearchParams, indexReader, taxoReader);
		searcher.search(query, MultiCollector.wrap(collector, facetsCollector));
		final List<FacetResult> facetResults = facetsCollector
				.getFacetResults();
		for (FacetResult fr : facetResults)
		{
			LOGGER.trace(
					"facet: {}={} {}",
					new Object[] { fr.getFacetResultNode().getLabel(),
							fr.getFacetRequest().getCategoryPath(),
							fr.getFacetResultNode().getValue() });
		}

		final ScoreDoc[] hits = collector.topDocs().scoreDocs;
		final List<SearchResult> results = new ArrayList<SearchResult>(
				hits.length);
		LOGGER.debug("{} results from index", hits.length);
		for (ScoreDoc scoreDoc : hits)
		{
			final Document doc = searcher.doc(scoreDoc.doc);
			final IndexableField idField = doc.getField(Article.Fields.ID
					.toString());
			final String id = idField.stringValue();
			final Article article = articleRepository.findOne(id);
			if (article == null)
			{
				LOGGER.warn(
						"the article {} is indexed but doesn't exists in the database",
						id);
			}
			else
			{
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

	private List<SearchResult> textSearchOnly(Query query, int numberOfResults)
			throws IOException, ParseException
	{
		LOGGER.debug("free text search");

		final TopScoreDocCollector collector = TopScoreDocCollector.create(
				numberOfResults, true);

		searcher.search(query, collector);
		final ScoreDoc[] hits = collector.topDocs().scoreDocs;
		final List<SearchResult> results = new ArrayList<SearchResult>(
				hits.length);
		LOGGER.debug("{} results from index", hits.length);
		for (ScoreDoc scoreDoc : hits)
		{
			final Document doc = searcher.doc(scoreDoc.doc);
			final IndexableField idField = doc.getField(Article.Fields.ID
					.toString());
			final String id = idField.stringValue();
			final Article article = articleRepository.findOne(id);
			if (article == null)
			{
				LOGGER.warn(
						"the article {} is indexed but doesn't exists in the database",
						id);
			}
			else
			{
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
}
