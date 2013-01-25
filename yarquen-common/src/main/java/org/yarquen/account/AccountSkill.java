package org.yarquen.account;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.yarquen.category.CategoryBranch;

public class AccountSkill {
	public static int BASIC = 1;
	public static int MEDIUM = 2;
	public static int ADVANCED = 3;
	public static String[] mapLevel={"Basic","Medium","Advanced"};
	
	@NotNull
	private CategoryBranch categoryBranch;
	@Min(value = 1)
	@Max(value = 3)
	private int level;

	public CategoryBranch getCategoryBranch() {
		return categoryBranch;
	}

	public void setCategoryBranch(CategoryBranch categoryBranch) {
		this.categoryBranch = categoryBranch;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public String getLevelName(){
		return mapLevel[level-1];
	}

	@Override
	public String toString() {
		return "AccountSkill [categoryBranch=" + categoryBranch + ", level="
				+ level + "]";
	}
}
