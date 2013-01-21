package org.yarquen.category;

import org.springframework.data.annotation.Id;

/**
 * Category
 * 
 * @author Jorge Riquelme Santana
 * @date 18/01/2013
 * @version $Id$
 * 
 */
public class Category extends SubCategory {
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
