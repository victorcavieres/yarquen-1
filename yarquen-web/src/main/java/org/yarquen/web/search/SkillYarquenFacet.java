package org.yarquen.web.search;

/**
 * Skill facet
 * 
 * @author Jorge Riquelme Santana
 * @date 30/01/2013
 * @version $Id$
 * 
 */
public class SkillYarquenFacet extends YarquenFacet {
	private int level;
	private String levelName;

	public int getLevel() {
		return level;
	}

	public String getLevelName() {
		return levelName;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}
}
