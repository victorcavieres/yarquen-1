package org.yarquen.web.enricher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.yarquen.category.Category;
import org.yarquen.category.CategoryBranch;
import org.yarquen.category.CategoryRepository;
import org.yarquen.category.SubCategory;

/**
 * Transforms a Category to a Map of Maps of Maps... and so on :)
 * 
 * @author Jorge Riquelme Santana
 * @date 21/01/2013
 * @version $Id$
 * 
 */
@Component
public class CategoryTreeBuilder {
	private static final String CHILDREN = "children";
	private static final String CODE = "code";
	private static final String DATA = "data";
	private static final String METADATA = "metadata";
	private static final String NAME = "name";
	@Resource
	private CategoryRepository categoryRepository;

	public List<Map<String, Object>> buildTree() {
		final List<Map<String, Object>> tree = new ArrayList<Map<String, Object>>();
		final Iterable<Category> categories = categoryRepository.findAll();
		for (Category category : categories) {
			final Map<String, Object> categoryNode = new HashMap<String, Object>();
			categoryNode.put(METADATA, buildMetadataMap(category, null, null));
			categoryNode.put(DATA, category.getName());
			if (category.getSubCategories() != null
					&& !category.getSubCategories().isEmpty()) {
				final List<Map<String, Object>> subCategoryNodes = new ArrayList<Map<String, Object>>();
				for (SubCategory subCategory : category.getSubCategories()) {
					final Map<String, Object> subCategoryNode = buildSubcategory(
							subCategory, category.getCode(), category.getName());
					subCategoryNodes.add(subCategoryNode);
				}
				categoryNode.put(CHILDREN, subCategoryNodes);
			}
			tree.add(categoryNode);
		}
		return tree;
	}

	private Map<String, Object> buildMetadataMap(SubCategory c, String code,
			String name) {
		final Map<String, Object> map = new HashMap<String, Object>();
		map.put(CODE,
				code != null ? code + CategoryBranch.CODE_SEPARATOR
						+ c.getCode() : c.getCode());
		map.put(NAME,
				name != null ? name + CategoryBranch.NAME_SEPARATOR
						+ c.getName() : c.getName());
		return map;
	}

	private Map<String, Object> buildSubcategory(SubCategory category,
			String code, String name) {
		final Map<String, Object> categoryNode = new HashMap<String, Object>();
		categoryNode.put(METADATA, buildMetadataMap(category, code, name));
		categoryNode.put(DATA, category.getName());
		if (category.getSubCategories() != null
				&& !category.getSubCategories().isEmpty()) {
			final List<Map<String, Object>> subCategoryNodes = new ArrayList<Map<String, Object>>();
			for (SubCategory subCategory : category.getSubCategories()) {
				final String subCategoryNodeCode = code
						+ CategoryBranch.CODE_SEPARATOR + category.getCode();
				final String subCategoryNodeName = name
						+ CategoryBranch.NAME_SEPARATOR + category.getName();
				final Map<String, Object> subCategoryNode = buildSubcategory(
						subCategory, subCategoryNodeCode, subCategoryNodeName);
				subCategoryNodes.add(subCategoryNode);
			}
			categoryNode.put(CHILDREN, subCategoryNodes);
		}
		return categoryNode;
	}
}
