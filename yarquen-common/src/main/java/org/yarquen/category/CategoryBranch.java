package org.yarquen.category;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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

	public static CategoryBranch incompleteFromCode(String code) {
		// escape code separator in regex
		final String[] components = code.split("\\" + CODE_SEPARATOR);
		final CategoryBranch branch = new CategoryBranch();
		for (String component : components) {
			branch.addSubCategory(component, null);
		}
		return branch;
	}

	@Valid
	private List<CategoryBranchNode> nodes = new ArrayList<CategoryBranchNode>();

	public CategoryBranch addSubCategory(String code, String name) {
		nodes.add(new CategoryBranchNode(code, name));
		return this;
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

	public String getCode(String categoryCode) {
		final StringBuilder sb = new StringBuilder();
		sb.append(categoryCode);
		sb.append(CODE_SEPARATOR);
		for (CategoryBranchNode c : nodes) {
			sb.append(c.getCode());
			sb.append(CODE_SEPARATOR);
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public String[] getCodeAsArray() {
		final String[] codeArray = new String[nodes.size()];
		int i = 0;
		for (CategoryBranchNode node : nodes) {
			codeArray[i++] = node.getCode();
		}
		return codeArray;
	}

	public String[] getCodeAsArray(String categoryCode) {
		final String[] codeArray = new String[nodes.size() + 1];
		int i = 0;
		codeArray[i++] = categoryCode;
		for (CategoryBranchNode node : nodes) {
			codeArray[i++] = node.getCode();
		}
		return codeArray;
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
	
	public static CategoryBranch parse(String branch){
		final CategoryBranch categoryBranch = new CategoryBranch();
		final StringTokenizer tokenizer = new StringTokenizer(
				branch, ".");
		while (tokenizer.hasMoreTokens()) {
			final String code = tokenizer.nextToken();
			categoryBranch.addSubCategory(code, null);
		}
		return categoryBranch;
	}

	@Override
	public String toString() {
		return getCode();
	}
}
