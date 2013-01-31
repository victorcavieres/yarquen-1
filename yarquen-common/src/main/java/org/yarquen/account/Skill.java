package org.yarquen.account;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.yarquen.category.CategoryBranch;
import org.yarquen.category.CategoryBranchNode;

public class Skill {
	public static int ADVANCED = 3;
	// FIXME: convert to enum
	public static int BASIC = 1;
	public static final String LEVEL_SEPARATOR = ".";
	public static String[] mapLevel = { "Basic", "Medium", "Advanced" };
	public static int MEDIUM = 2;

	@NotNull
	private CategoryBranch categoryBranch;
	@Min(value = 1)
	@Max(value = 3)
	private int level;

	public String getAsText() {
		return categoryBranch.getCode() + LEVEL_SEPARATOR + level;
	}

	public CategoryBranch getCategoryBranch() {
		return categoryBranch;
	}

	public String getCode() {
		String code = categoryBranch.getCode();
		if (level != 0) {
			code += LEVEL_SEPARATOR + level;
		}
		return code;
	}

	public String[] getCodeAsArray(String categoryCode) {
		int arrayLength = 1 + categoryBranch.getNodes().size();
		if (level != 0) {
			arrayLength++;
		}
		final String[] codeArray = new String[arrayLength];
		int i = 0;
		codeArray[i++] = categoryCode;
		for (CategoryBranchNode node : categoryBranch.getNodes()) {
			codeArray[i++] = node.getCode();
		}
		if (level != 0) {
			codeArray[i++] = String.valueOf(level);
		}
		return codeArray;
	}

	public int getLevel() {
		return level;
	}

	public String getLevelName() {
		return mapLevel[level - 1];
	}

	public void setCategoryBranch(CategoryBranch categoryBranch) {
		this.categoryBranch = categoryBranch;
	}

	public void setLevel(int level) {
		if (level < 0 || level > 3) {
			throw new IllegalArgumentException("Invalid level value(" + level
					+ "), it has to be in the range [0, 3]");
		} else {
			this.level = level;
		}
	}

	@Override
	public String toString() {
		return "AccountSkill [categoryBranch=" + categoryBranch + ", level="
				+ level + "]";
	}
}
