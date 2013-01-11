package org.yarquen.web.search;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 11/01/2013
 * @version $Id$
 * 
 */
public class YarquenFacet
{
	private boolean applied;
	private int count;
	private String name;
	// TODO: temporal solution
	private String url;
	private String value;

	public int getCount()
	{
		return count;
	}

	public String getName()
	{
		return name;
	}

	public String getUrl()
	{
		return url;
	}

	public String getValue()
	{
		return value;
	}

	public boolean isApplied()
	{
		return applied;
	}

	public void setApplied(boolean applied)
	{
		this.applied = applied;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}
