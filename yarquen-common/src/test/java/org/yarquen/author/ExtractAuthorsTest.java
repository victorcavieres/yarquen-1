package org.yarquen.author;

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
 * Fake test to extract authors from articles
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
@ContextConfiguration(locations = { "/context.xml", "/author-context.xml",
		"/article-context.xml" })
public class ExtractAuthorsTest {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ExtractAuthorsTest.class);
	@Resource
	private AuthorRepository authorRepository;
	@Resource
	private ArticleRepository articleRepository;

	@Test
	public void test() {
		final Iterable<Article> articles = articleRepository.findAll();
		for (Article article : articles) {
			final String authorName = article.getAuthor();
			Author author = authorRepository.findByName(authorName);
			if (author == null) {
				author = new Author();
				author.setName(authorName);
				LOGGER.debug("saving author {}", author.getName());
				authorRepository.save(author);
			}
		}
	}
}
