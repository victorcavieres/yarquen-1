package org.yarquen.article;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;

/**
 * Article
 * 
 * @author Jorge Riquelme Santana
 * @date 22/11/2012
 * @version $Id$
 * 
 */
public class Article
{
	public static enum Facets
	{
		AUTHOR("author"), KEYWORD("keyword"), YEAR("year");

		private String label;

		private Facets(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		};
	}

	public static enum Fields
	{
		ID("id"), PLAIN_TEXT("plainText"), TITLE("title"), URL("url");

		private String label;

		private Fields(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		};
	}

	private String author;
	private String date;
	@Id
	private String id;
	private List<String> keywords;
	@NotEmpty
	private String plainText;
	private String summary;
	@NotEmpty
	private String title;
	@NotEmpty
	private String url;

	public String getAuthor()
	{
		return author;
	}

	public String getDate()
	{
		return date;
	}

	public String getId()
	{
		return id;
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

	public void setId(String id)
	{
		this.id = id;
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
