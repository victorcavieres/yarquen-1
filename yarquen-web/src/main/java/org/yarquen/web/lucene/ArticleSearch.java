package org.yarquen.web.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yarquen.web.search.SearchResult;

/**
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

	private Directory index;
	private StandardAnalyzer analyzer;
	private IndexSearcher searcher;

	@PostConstruct
	public void init() throws IOException
	{
		index = new NIOFSDirectory(new File("/var/yarquen-lucene"));
//		index = new NIOFSDirectory(new File("/home/totex/local/tmp/lucene"));
		analyzer = new StandardAnalyzer(Version.LUCENE_40);

		final IndexReader reader = IndexReader.open(index);
		searcher = new IndexSearcher(reader);
	}

	public List<SearchResult> search(String query, int hitsPerPage)
			throws IOException, ParseException
	{
		final Query q = new QueryParser(Version.LUCENE_40, "title", analyzer)
				.parse(query);

		final TopScoreDocCollector collector = TopScoreDocCollector.create(
				hitsPerPage, true);
		searcher.search(q, collector);
		final ScoreDoc[] hits = collector.topDocs().scoreDocs;
		final List<SearchResult> results = new ArrayList<SearchResult>(
				hits.length);
		for (ScoreDoc scoreDoc : hits)
		{
			final Document doc = searcher.doc(scoreDoc.doc);
			final IndexableField urlField = doc.getField("url");
			final IndexableField titleField = doc.getField("title");
			final IndexableField dateField = doc.getField("date");
			final IndexableField authorField = doc.getField("author");
			final IndexableField summaryField = doc.getField("summary");

			final SearchResult searchResult = new SearchResult();
			searchResult.setUrl(urlField.stringValue());
			searchResult.setTitle(titleField.stringValue());
			searchResult.setDate(dateField.stringValue());
			searchResult.setAuthor(authorField.stringValue());
			final String summary = summaryField.stringValue();
			if (summary == null || summary.trim().length() == 0)
			{
				searchResult
						.setSummary("No summary was found for this result :(");
			}
			else
			{
				searchResult.setSummary(summary);
			}

			final IndexableField[] keywordFields = doc.getFields("keyword");
			final List<String> keywords = new ArrayList<String>();
			for (IndexableField field : keywordFields)
			{
				keywords.add(field.stringValue());
			}
			searchResult.setKeywords(keywords);

			float score = (float) (Math.round(scoreDoc.score * 10.0) / 10.0);
			searchResult.setScore(score);

			LOGGER.debug("result:\n\ttitle:'{}'\n\turl:{}",
					searchResult.getTitle(), searchResult.getUrl());

			// FIXME: hack to eliminate duplicate results
			if (contains(results, searchResult))
			{
				LOGGER.debug("discarding result: {}", searchResult.getTitle());
			}
			else
			{
				results.add(searchResult);
			}
		}
		return results;
	}

	private boolean contains(List<SearchResult> results, SearchResult result)
	{
		for (SearchResult searchResult : results)
		{
			if (searchResult.getTitle().equals(result.getTitle()))
			{
				return true;
			}
		}
		return false;
	}
}
