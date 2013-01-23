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
	private List<CategoryBranchNode> nodes = new ArrayList<CategoryBranchNode>();

	public void addSubCategory(String code, String name) {
		nodes.add(new CategoryBranchNode(code, name));
	}

	public String getCode() {
		final StringBuilder sb = new StringBuilder();
		for (CategoryBranchNode c : nodes) {
			sb.append(c.getCode());
			sb.append(CODE_SEPARATOR);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public String getName() {
		final StringBuilder sb = new StringBuilder();
		for (CategoryBranchNode c : nodes) {
			sb.append(c.getName());
			sb.append(NAME_SEPARATOR);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public List<CategoryBranchNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<CategoryBranchNode> nodes) {
		this.nodes = nodes;
	}

	@Override
	public String toString() {
		return getCode();
	}
}
