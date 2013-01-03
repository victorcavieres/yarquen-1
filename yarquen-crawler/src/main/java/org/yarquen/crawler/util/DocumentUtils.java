package org.yarquen.crawler.util;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXWriter;
import org.xml.sax.SAXException;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 22/11/2012
 * @version $Id$
 * 
 */
public class DocumentUtils
{
	public static String getPlainText(Document doc)
	{
		final BodyContentHandler bodyContentHandler = new BodyContentHandler();
		final XHTMLContentHandler xhtmlContentHandler = new XHTMLContentHandler(
				bodyContentHandler, new Metadata());
		final SAXWriter writer = new SAXWriter(xhtmlContentHandler);
		try
		{
			writer.write(doc);
			return bodyContentHandler.toString();
		}
		catch (SAXException e)
		{
			throw new RuntimeException("Ugh!", e);
		}
	}
	
	public static String getPlainText(Node node)
	{
		final BodyContentHandler bodyContentHandler = new BodyContentHandler();
		final XHTMLContentHandler xhtmlContentHandler = new XHTMLContentHandler(
				bodyContentHandler, new Metadata());
		final SAXWriter writer = new SAXWriter(xhtmlContentHandler);
		try
		{
			writer.write(node);
			return bodyContentHandler.toString();
		}
		catch (SAXException e)
		{
			throw new RuntimeException("Ugh!", e);
		}
	}

	private DocumentUtils()
	{
	}
}
