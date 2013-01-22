package org.yarquen.category;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

/**
 * Category branch (path from root to leaf)
 * 
 * @author Jorge Riquelme Santana
 * @date 21/01/2013
 * @version $Id$
 * 
 */
public class CategoryBranch {
	public static final String CODE_SEPARATOR = ".";
	public static final String NAME_SEPARATOR = "/";

	@Valid
	private List<SubCategory> categories = new ArrayList<SubCategory>();

	public List<SubCategory> getCategories() {
		return categories;
	}

	public String getCode() {
		final StringBuilder sb = new StringBuilder();
		for (SubCategory c : categories) {
			sb.append(c.getCode());
			sb.append(CODE_SEPARATOR);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public String getName() {
		final StringBuilder sb = new StringBuilder();
		for (SubCategory c : categories) {
			sb.append(c.getName());
			sb.append(NAME_SEPARATOR);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public void setCategories(List<SubCategory> categories) {
		if (categories.get(0) instanceof Category) {
			this.categories = categories;
		} else {
			throw new RuntimeException(
					"the CategoryBranch root must be an instance of Category");
		}
	}

	@Override
	public String toString() {
		return getCode();
	}
}
