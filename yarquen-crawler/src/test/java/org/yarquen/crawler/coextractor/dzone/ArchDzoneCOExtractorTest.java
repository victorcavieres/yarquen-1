package org.yarquen.crawler.coextractor.dzone;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.ccil.cowan.tagsoup.Parser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import org.yarquen.crawler.datum.ArticleDatum;
import org.yarquen.crawler.extractor.dzone.DzoneArticleExtractor;

import bixo.datum.Outlink;
import bixo.datum.ParsedDatum;

/**
 * {@link DzoneArticleExtractor} tests
 * 
 * @author Jorge Riquelme Santana
 * @date 15/08/2012
 * @version $Id$
 * 
 */
public class ArchDzoneCOExtractorTest
{
	/**
	 * Strip out XML namespace, so that XPath can be easily used to extract
	 * elements.
	 * 
	 */
	private static class DowngradeXmlFilter extends XMLFilterImpl
	{

		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException
		{
			String lower = localName.toLowerCase();
			super.endElement(XMLConstants.NULL_NS_URI, lower, lower);
		}

		@Override
		public void endPrefixMapping(String prefix)
		{
		}

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes atts) throws SAXException
		{
			String lower = localName.toLowerCase();

			AttributesImpl attributes = new AttributesImpl();
			for (int i = 0; i < atts.getLength(); i++)
			{
				String local = atts.getLocalName(i);
				String qname = atts.getQName(i);
				if (!XMLConstants.NULL_NS_URI.equals(atts.getURI(i).length())
						&& !local.equals(XMLConstants.XMLNS_ATTRIBUTE)
						&& !qname
								.startsWith(XMLConstants.XMLNS_ATTRIBUTE + ":"))
				{
					attributes.addAttribute(atts.getURI(i), local, qname,
							atts.getType(i), atts.getValue(i));
				}
			}

			super.startElement(XMLConstants.NULL_NS_URI, lower, lower,
					attributes);
		}

		@Override
		public void startPrefixMapping(String prefix, String uri)
		{
		}
	}

	private SAXReader reader;

	@Before
	public void setup()
	{
		reader = new SAXReader(new Parser());
		reader.setXMLFilter(new DowngradeXmlFilter());
		reader.setEncoding("UTF-8");
	}

	@Test
	public void test_why_having_api_matters_testing() throws DocumentException
	{
		final InputStream resourceAsStream = getClass().getResourceAsStream(
				"/Why having an API matters  testing   Agile Zone.html");
		Assert.assertNotNull(resourceAsStream);
		final Document parsedContent = reader.read(resourceAsStream);

		final DzoneArticleExtractor articleExtractor = new DzoneArticleExtractor();
		String url = "http://agile.dzone.com/articles/why-having-api-matters-testing";
		String hostAddress = "";
		String parsedText = getPlainText(parsedContent);
		String language = "";
		String title = "";
		Outlink[] outlinks = new Outlink[] {};
		Map<String, String> parsedMeta = new HashMap<String, String>();
		final ParsedDatum parsedDatum = new ParsedDatum(url, hostAddress,
				parsedText, language, title, outlinks, parsedMeta);
		final ArticleDatum article = articleExtractor.extractArticles(
				parsedDatum, parsedContent,
				(List<Outlink>) new ArrayList<Outlink>());

		System.out.println(article.getPlainText());

		Assert.assertEquals(article.getAuthor(), "Giorgio Sironi");
		Assert.assertEquals(article.getDate(), "11.19.2012");
		Assert.assertEquals(
				article.getSummary(),
				"You know when they tell you exposing an HTTP API for your application (usually a REST-like one) is positive for reuse and accessing it in unforeseen ways? That's whay just happened to me last week while trying to put together some functional tests.");
		Assert.assertEquals(article.getTitle(),
				"Why having an API matters: testing");
		Assert.assertEquals(article.getUrl(),
				"http://agile.dzone.com/articles/why-having-api-matters-testing");
		Assert.assertEquals(article.getKeywords().length, 3);
		String[] kw = new String[] { "http", "REST", "testing" };
		for (int i = 0; i < 3; i++)
		{
			Assert.assertEquals(article.getKeywords()[i], kw[i]);
		}

		Assert.assertTrue(article.getPlainText().contains(
				"is positive for reuse and accessing"));
		Assert.assertTrue(article.getPlainText().contains(
				"It communicates with the server-side through a REST-like API"));
	}

	@Test
	public void test_election_analytics_tetris_and() throws DocumentException
	{
		final InputStream resourceAsStream = getClass()
				.getResourceAsStream(
						"/Election Analytics, Tetris, and More Data Links of the Week   Architects Zone.html");
		Assert.assertNotNull(resourceAsStream);
		final Document parsedContent = reader.read(resourceAsStream);

		final DzoneArticleExtractor articleExtractor = new DzoneArticleExtractor();
		String url = "http://architects.dzone.com/articles/election-analytics-tetris-and";
		String hostAddress = "";
		String parsedText = getPlainText(parsedContent);
		String language = "";
		String title = "";
		Outlink[] outlinks = new Outlink[] {};
		Map<String, String> parsedMeta = new HashMap<String, String>();
		final ParsedDatum parsedDatum = new ParsedDatum(url, hostAddress,
				parsedText, language, title, outlinks, parsedMeta);
		final ArticleDatum article = articleExtractor.extractArticles(
				parsedDatum, parsedContent,
				(List<Outlink>) new ArrayList<Outlink>());

		System.out.println(article.getPlainText());

		Assert.assertEquals(article.getAuthor(), "Arthur Charpentier");
		Assert.assertEquals(article.getDate(), "11.14.2012");
		Assert.assertEquals(article.getSummary(), "");
		Assert.assertEquals(article.getTitle(),
				"Election Analytics, Tetris, and More Data Links of the Week");
		Assert.assertEquals(article.getUrl(),
				"http://architects.dzone.com/articles/election-analytics-tetris-and");
		Assert.assertEquals(article.getKeywords().length, 3);
		String[] kw = new String[] { "Big Data", "Tips and Tricks",
				"Tools & Methods" };
		for (int i = 0; i < 3; i++)
		{
			Assert.assertEquals(article.getKeywords()[i], kw[i]);
		}

		Assert.assertTrue(article.getPlainText().contains(
				"Why supervisors should continue measuring financial risks"));
		Assert.assertTrue(article.getPlainText()
				.contains("Supermarket banking"));
	}

	private String getPlainText(Document doc)
	{
		BodyContentHandler bodyContentHandler = new BodyContentHandler();
		XHTMLContentHandler xhtmlContentHandler = new XHTMLContentHandler(
				bodyContentHandler, new Metadata());
		SAXWriter writer = new SAXWriter(xhtmlContentHandler);
		try
		{
			writer.write(doc);
		}
		catch (SAXException e)
		{
			throw new RuntimeException(e);
		}

		return bodyContentHandler.toString();
	}
}
