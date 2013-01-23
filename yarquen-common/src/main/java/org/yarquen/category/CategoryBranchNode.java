package org.yarquen.category;

/**
 * Branch node
 * 
 * @author Jorge Riquelme Santana
 * @date 23/01/2013
 * @version $Id$
 * 
 */
public class CategoryBranchNode {
	private String code;
	private String name;

	public CategoryBranchNode() {
	}

	public CategoryBranchNode(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setName(String name) {
		this.name = name;
	}
}
