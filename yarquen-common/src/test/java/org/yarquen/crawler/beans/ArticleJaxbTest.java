package org.yarquen.crawler.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Before;
import org.junit.Test;
import org.yarquen.model.Article;
import org.yarquen.model.ObjectFactory;

/**
 * Jaxb test
 * 
 * @author Jorge Riquelme Santana
 * @date 22/11/2012
 * @version $Id$
 * 
 */
public class ArticleJaxbTest
{
	private JAXBContext context;

	@Before
	public void setup() throws JAXBException
	{
		context = JAXBContext.newInstance("org.yarquen.model");
	}

	@Test
	public void test() throws JAXBException
	{
		final Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		final Article article = new Article();
		article.setAuthor("jriquelme");
		article.setDate("22/11/2012");
		final List<String> keywords = new ArrayList<String>(3);
		keywords.add("REST");
		keywords.add("http");
		keywords.add("API");
		article.setKeywords(keywords);
		article.setPlainText("asdf");
		article.setSummary("very intedezting");
		article.setTitle("the title");
		article.setUrl("http://asdf.com");

		final ObjectFactory objectFactory = new ObjectFactory();
		marshaller.marshal(objectFactory.createArticle(article), System.out);
	}
}
