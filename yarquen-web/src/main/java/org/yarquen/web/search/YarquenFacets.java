package org.yarquen.web.search;

import java.util.List;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 11/01/2013
 * @version $Id$
 * 
 */
public class YarquenFacets {
	private List<YarquenFacet> author;
	private List<YarquenFacet> keyword;
	private List<SkillYarquenFacet> providedSkill;
	private List<SkillYarquenFacet> requiredSkill;
	private List<YarquenFacet> year;

	public List<YarquenFacet> getAuthor() {
		return author;
	}

	public List<YarquenFacet> getKeyword() {
		return keyword;
	}

	public List<SkillYarquenFacet> getProvidedSkill() {
		return providedSkill;
	}

	public List<SkillYarquenFacet> getRequiredSkill() {
		return requiredSkill;
	}

	public List<YarquenFacet> getYear() {
		return year;
	}

	public void setAuthor(List<YarquenFacet> author) {
		this.author = author;
	}

	public void setKeyword(List<YarquenFacet> keyword) {
		this.keyword = keyword;
	}

	public void setProvidedSkill(List<SkillYarquenFacet> providedSkill) {
		this.providedSkill = providedSkill;
	}

	public void setRequiredSkill(List<SkillYarquenFacet> requiredSkill) {
		this.requiredSkill = requiredSkill;
	}

	public void setYear(List<YarquenFacet> year) {
		this.year = year;
	}
}
