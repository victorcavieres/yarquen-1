package org.yarquen.category;

/**
 * Category repo
 * 
 * @author Jorge Riquelme Santana
 * @date 18/01/2013
 * @version $Id$
 * 
 */
public interface CategoryService {
	String addCategory(CategoryBranch categoryBranch, String newCategory);

	void completeCategoryBranchNodeNames(CategoryBranch categoryBranch);

	void deleteCategory(CategoryBranch categoryBranch);

	SubCategory getLeaf(Category category, CategoryBranch categoryBranch);

	void renameCategory(CategoryBranch categoryBranch, String newNameNode);
}
