package org.yarquen.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.persistence.oxm.annotations.XmlCDATA;

/**
 * Article
 * 
 * @author Jorge Riquelme Santana
 * @date 22/11/2012
 * @version $Id$
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Article", propOrder = { "url", "title", "date", "summary",
		"author", "keywords", "plainText" })
public class Article
{
	@XmlElement(required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	@XmlSchemaType(name = "normalizedString")
	private String author;

	@XmlElement(required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlSchemaType(name = "token")
	private String date;

	@XmlElementWrapper(name = "keywords")
	@XmlElement(name = "keyword", required = true)
	private List<String> keywords;

	@XmlCDATA
	@XmlElement(required = true)
	private String plainText;

	@XmlElement(required = true)
	private String summary;

	@XmlElement(required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	@XmlSchemaType(name = "normalizedString")
	private String title;

	@XmlElement(required = true)
	@XmlSchemaType(name = "anyURI")
	private String url;

	public String getAuthor()
	{
		return author;
	}

	public String getDate()
	{
		return date;
	}

	public List<String> getKeywords()
	{
		return keywords;
	}

	public String getPlainText()
	{
		return plainText;
	}

	public String getSummary()
	{
		return summary;
	}

	public String getTitle()
	{
		return title;
	}

	public String getUrl()
	{
		return url;
	}

	public void setAuthor(String value)
	{
		this.author = value;
	}

	public void setDate(String value)
	{
		this.date = value;
	}

	public void setKeywords(List<String> value)
	{
		this.keywords = value;
	}

	public void setPlainText(String value)
	{
		this.plainText = value;
	}

	public void setSummary(String value)
	{
		this.summary = value;
	}

	public void setTitle(String value)
	{
		this.title = value;
	}

	public void setUrl(String value)
	{
		this.url = value;
	}
}
