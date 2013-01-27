package org.yarquen.article;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * repo test
 * 
 * @author Jorge Riquelme Santana
 * @date 22/11/2012
 * @version $Id$
 * 
 */
@IfProfileValue(name = "test-groups", values = { "itests" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/context.xml", "/article-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FixArticlesDuplicatedKeywordsTest {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FixArticlesDuplicatedKeywordsTest.class);

	@Resource
	private ArticleRepository articleRepository;

	@Test
	public void test() {
		final Iterable<Article> articles = articleRepository.findAll();
		for (Article article : articles) {
			final List<String> keywords = article.getKeywords();
			if (keywords != null) {
				final int count = keywords.size();
				Set<String> uniqueKeywords = new HashSet<String>();
				Iterator<String> iterator = keywords.iterator();
				while (iterator.hasNext()) {
					final String kw = iterator.next();
					if (uniqueKeywords.contains(kw)) {
						iterator.remove();
					} else {
						uniqueKeywords.add(kw);
					}
				}

				if (keywords.size() != count) {
					LOGGER.debug("updating article, from {} => {} keywords",
							count, keywords.size());
					articleRepository.save(article);
				}
			}
		}
	}
}
