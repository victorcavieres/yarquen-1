package org.yarquen.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 22/11/2012
 * @version $Id$
 * 
 */
@XmlRegistry
public class ObjectFactory
{
	private final static QName _Article_QNAME = new QName(
			"http://www.yarquen.org/ns/yarquen-article", "article");

	public Article createArticle()
	{
		return new Article();
	}

	@XmlElementDecl(namespace = "http://www.yarquen.org/ns/yarquen-article", name = "article")
	public JAXBElement<Article> createArticle(Article value)
	{
		return new JAXBElement<Article>(_Article_QNAME, Article.class, null,
				value);
	}
}
