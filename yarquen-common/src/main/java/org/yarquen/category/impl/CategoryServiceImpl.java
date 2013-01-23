package org.yarquen.category.impl;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.yarquen.category.Category;
import org.yarquen.category.CategoryBranch;
import org.yarquen.category.CategoryBranchNode;
import org.yarquen.category.CategoryRepository;
import org.yarquen.category.CategoryService;
import org.yarquen.category.SubCategory;

/**
 * Category service impl
 * 
 * @author Jorge Riquelme Santana
 * @date 23/01/2013
 * @version $Id$
 * 
 */
@Service
public class CategoryServiceImpl implements CategoryService {
	@Resource
	private CategoryRepository categoryRepository;

	@Override
	public void completeCategoryBranchNodeNames(CategoryBranch categoryBranch) {
		final List<CategoryBranchNode> nodes = categoryBranch.getNodes();
		if (nodes == null || nodes.isEmpty()) {
			throw new IllegalArgumentException(
					"category branch nodes cannot be null/empty");
		} else {
			final String categoryCode = nodes.get(0).getCode();
			final SubCategory category = categoryRepository
					.findByCode(categoryCode);
			if (category == null) {
				throw new RuntimeException("category " + categoryCode
						+ " not found");
			} else if (!(category instanceof Category)) {
				throw new IllegalArgumentException(
						"category branch root must be of type Category");
			} else {
				nodes.get(0).setName(category.getName());

				final Iterator<CategoryBranchNode> iterator = nodes.iterator();
				// discard element already processed
				iterator.next();

				completeSubCategory(iterator, category);
			}
		}
	}

	private void completeSubCategory(Iterator<CategoryBranchNode> iterator,
			SubCategory parentCategory) {
		if (iterator.hasNext()) {
			final CategoryBranchNode node = iterator.next();
			final List<SubCategory> subCategories = parentCategory
					.getSubCategories();
			if (subCategories == null) {
				throw new RuntimeException(
						"Invalid branch, no more subCategories from node "
								+ node.getCode());
			}
			for (SubCategory subCategory : subCategories) {
				if (subCategory.getCode().equals(node.getCode())) {
					node.setName(subCategory.getName());
					completeSubCategory(iterator, subCategory);
					return;
				}
			}
			throw new RuntimeException("Invalid branch, subCategory for node "
					+ node.getCode() + " not found");
		}
	}
}
