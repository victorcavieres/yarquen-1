package org.yarquen.keyword;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;

/**
 * Fake test to extract keywords from articles
 * 
 * FIXME: remove this
 * 
 * @author Jorge Riquelme Santana
 * @date 20/01/2013
 * @version $Id$
 * 
 */
@IfProfileValue(name = "test-groups", values = { "itests" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/context.xml", "/keyword-context.xml",
		"/article-context.xml" })
public class ExtractKeywordsTest {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ExtractKeywordsTest.class);
	@Resource
	private KeywordRepository keywordRepository;
	@Resource
	private ArticleRepository articleRepository;

	@Test
	public void test() {
		final Iterable<Article> articles = articleRepository.findAll();
		for (Article article : articles) {
			final List<String> keywords = article.getKeywords();
			for (String kwName : keywords) {
				Keyword keyword = keywordRepository.findByName(kwName);
				if (keyword == null) {
					keyword = new Keyword();
					keyword.setName(kwName);
					LOGGER.debug("saving keyword {}", keyword.getName());
					keywordRepository.save(keyword);
				}
			}

		}
	}
}
