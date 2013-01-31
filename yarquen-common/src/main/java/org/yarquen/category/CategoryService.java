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
	void completeCategoryBranchNodeNames(CategoryBranch categoryBranch);
	void renameCategory(CategoryBranch categoryBranch,String newNameNode);
	void deleteCategory(CategoryBranch categoryBranch);
	String addCategory(CategoryBranch categoryBranch, String newCategory);
	SubCategory getLeaf(Category category, CategoryBranch categoryBranch);
}
