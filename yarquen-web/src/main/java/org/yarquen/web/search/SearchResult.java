package org.yarquen.web.search;

import java.util.List;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * @version $Id$
 * 
 */
public class SearchResult
{
	private String author;
	private String date;
	private String id;
	private List<String> keywords;
	private float score;
	private String summary;
	private String title;
	private String url;

	public String getAuthor()
	{
		return author;
	}

	public String getDate()
	{
		return date;
	}

	public String getId() {
		return id;
	}

	public List<String> getKeywords()
	{
		return keywords;
	}

	public float getScore()
	{
		return score;
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

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setKeywords(List<String> keywords)
	{
		this.keywords = keywords;
	}

	public void setScore(float score)
	{
		this.score = score;
	}

	public void setSummary(String summary)
	{
		this.summary = summary;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}
}
