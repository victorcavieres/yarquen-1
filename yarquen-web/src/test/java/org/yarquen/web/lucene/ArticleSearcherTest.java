package org.yarquen.web.lucene;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yarquen.web.search.SearchFields;
import org.yarquen.web.search.SearchResult;
import org.yarquen.web.search.YarquenFacets;

/**
 * {@link ArticleSearcher} itests
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * @version $Id$
 * 
 */
@IfProfileValue(name = "test-groups", value = "itests")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/context.xml", "/articleSearcher-context.xml" })
public class ArticleSearcherTest {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ArticleSearcherTest.class);

	@Resource
	private ArticleSearcher articleSearcher;

	@Test
	public void test() throws IOException, ParseException {
		final SearchFields searchFields = new SearchFields();
		searchFields.setQuery("java");

		final YarquenFacets facetsCount = new YarquenFacets();

		final List<SearchResult> results = articleSearcher.search(searchFields,
				facetsCount);
		for (SearchResult result : results) {
			LOGGER.debug("title: {}", result.getTitle());
		}
	}
}
