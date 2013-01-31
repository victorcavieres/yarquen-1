package org.yarquen.web.search;

import java.util.List;

import org.yarquen.account.Skill;

/**
 * Search fields
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * @version $Id$
 * 
 */
public class SearchFields {
	private String author;
	private Integer facets;
	private List<String> keyword;
	private List<Skill> providedSkill;
	private String query;
	private List<Skill> requiredSkill;
	private Integer results;
	private String year;

	public String getAuthor() {
		return author;
	}

	public Integer getFacets() {
		return facets;
	}

	public List<String> getKeyword() {
		return keyword;
	}

	public List<Skill> getProvidedSkill() {
		return providedSkill;
	}

	public String getQuery() {
		return query;
	}

	public List<Skill> getRequiredSkill() {
		return requiredSkill;
	}

	public Integer getResults() {
		return results;
	}

	public String getYear() {
		return year;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setFacets(Integer facets) {
		this.facets = facets;
	}

	public void setKeyword(List<String> keyword) {
		this.keyword = keyword;
	}

	public void setProvidedSkill(List<Skill> providedSkill) {
		this.providedSkill = providedSkill;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setRequiredSkill(List<Skill> requiredSkill) {
		this.requiredSkill = requiredSkill;
	}

	public void setResults(Integer results) {
		this.results = results;
	}

	public void setYear(String year) {
		this.year = year;
	}
}
