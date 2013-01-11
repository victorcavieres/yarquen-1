package org.yarquen.web.search;

import java.util.List;

/**
 * Search fields
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * @version $Id$
 * 
 */
public class SearchFields
{
	private String author;
	private List<String> keywords;
	private String query;
	private Integer results;
	private String year;

	public String getAuthor()
	{
		return author;
	}

	public List<String> getKeywords()
	{
		return keywords;
	}

	public String getQuery()
	{
		return query;
	}

	public Integer getResults()
	{
		return results;
	}

	public String getYear()
	{
		return year;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public void setKeywords(List<String> keywords)
	{
		this.keywords = keywords;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public void setResults(Integer results)
	{
		this.results = results;
	}

	public void setYear(String year)
	{
		this.year = year;
	}
}
