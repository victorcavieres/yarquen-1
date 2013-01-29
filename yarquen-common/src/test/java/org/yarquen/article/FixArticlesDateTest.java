package org.yarquen.article;

import javax.annotation.Resource;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
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
public class FixArticlesDateTest {

	@Resource
	private ArticleRepository articleRepository;

	@Test
	public void t4findById() {
		final Iterable<Article> articles = articleRepository.findAll();
		for (Article article : articles) {
			final String date = article.getDate();
			if (date != null) {
				final String newDate = date.replace('.', '/');
				article.setDate(newDate);
				articleRepository.save(article);
			}
		}
	}
}
