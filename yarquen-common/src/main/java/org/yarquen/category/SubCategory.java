package org.yarquen.category;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Category
 * 
 * @author Jorge Riquelme Santana
 * @date 18/01/2013
 * @version $Id$
 * 
 */
public class SubCategory {
	@Pattern(regexp = "[\\w\\d_]+")
	@NotEmpty
	private String code;
	@NotEmpty
	private String name;
	@Valid
	private List<SubCategory> subCategories;

	public SubCategory() {
	}

	public SubCategory(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public boolean addSubCategory(SubCategory subCategory) {
		if (!subCategories.contains(subCategory)) {
			boolean added = subCategories.add(subCategory);
			return added;
		} else {
			return false;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubCategory other = (SubCategory) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		return true;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public List<SubCategory> getSubCategories() {
		return subCategories;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		return result;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSubCategories(List<SubCategory> subCategories) {
		this.subCategories = subCategories;
	}
}
