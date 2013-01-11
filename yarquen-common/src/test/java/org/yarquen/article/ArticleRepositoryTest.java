package org.yarquen.article;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;

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
public class ArticleRepositoryTest
{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ArticleRepositoryTest.class);
	private static final String TITLE = "the title";
	private static final String URL = "http://asdf.com";

	private Article article;
	@Resource
	private ArticleRepository articleRepository;

	@Before
	public void setup()
	{
		article = new Article();
		article.setAuthor("jriquelme");
		article.setDate("22/11/2012");
		final List<String> keywords = new ArrayList<String>(3);
		keywords.add("REST");
		keywords.add("http");
		keywords.add("API");
		article.setKeywords(keywords);
		article.setPlainText("asdf");
		article.setSummary("very intedezting");
		article.setTitle(TITLE);
		article.setUrl(URL);
	}

	@Test
	public void t1Save()
	{
		final Article savedArticle = articleRepository.save(article);
		Assert.assertNotNull(savedArticle);
		LOGGER.info("article saved with id {}", savedArticle.getId());
	}

	@Test
	public void t2FindByUrl()
	{
		final Article art = articleRepository.findByUrl(URL);
		Assert.assertNotNull(art);
		Assert.assertEquals(TITLE, art.getTitle());
	}

	@Test
	public void t3Remove()
	{
		final Article art = articleRepository.findByUrl(URL);
		Assert.assertNotNull(art);
		articleRepository.delete(art);

		Assert.assertNull(articleRepository.findByUrl(URL));
	}

	@Test
	public void t4findById()
	{
		final Article art = articleRepository
				.findOne("50eee67d2b5e52b1ef9792bf");
		Assert.assertNotNull(art);
		LOGGER.info("title is: {}", art.getTitle());
		Assert.assertEquals("Why Page Speed Isn't Enough", art.getTitle());
	}
}
