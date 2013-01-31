package org.yarquen.category;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yarquen.category.impl.CategoryServiceImpl;

@IfProfileValue(name = "test-groups", values = { "itests" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/context.xml", "/category-context.xml" })
public class CategoryServiceImplTest {
	@Resource
	CategoryService categoryService;
	@Resource
	CategoryServiceImpl categoryServiceImpl;
	@Resource
	CategoryRepository categoryRepository;
	private CategoryBranch categoryBranch;
	private Category category;

	@Before
	public void setUp() throws Exception {
		categoryBranch = new CategoryBranch();
		categoryBranch.addSubCategory("Informatica", null);
		categoryBranch.addSubCategory("TecnologiasDeAlmacenamiento", null);
		categoryBranch.addSubCategory("DispositivosDeAlmacenamiento", null);
		categoryBranch.addSubCategory("CDDVDDiskette", null);
		category = categoryRepository.findByCode("Informatica");
	}

	@Test
	public void testGetLeaf() {

		SubCategory subCategory = categoryService.getLeaf(category,
				categoryBranch);
		Assert.assertNotNull(subCategory);
		Assert.assertEquals("CDDVDDiskette", subCategory.getCode());
	}

	@Test
	public void generateCode() {
		String code = CategoryServiceImpl.generateCode("test code");
		Assert.assertEquals("TestCode", code);
		code = CategoryServiceImpl.generateCode("test Code");
		Assert.assertEquals("TestCode", code);
		code = CategoryServiceImpl.generateCode("Test Code");
		Assert.assertEquals("TestCode", code);
		code = CategoryServiceImpl.generateCode("Test code");
		Assert.assertEquals("TestCode", code);
		code = CategoryServiceImpl.generateCode("Testcode");
		Assert.assertEquals("Testcode", code);
		code = CategoryServiceImpl.generateCode("Test ");
		Assert.assertEquals("Test", code);
	}

	@Test
	public void hasArticlesWith() {
		String code = "HerramientasDeDesarrolloDeSoftware";
		boolean hasArticlesWith = categoryServiceImpl.hasArticlesWith(code);
		Assert.assertTrue(hasArticlesWith);
		code = "nothingtoSearch";
		hasArticlesWith = categoryServiceImpl.hasArticlesWith(code);
		Assert.assertFalse(hasArticlesWith);
		code = "Informatica";
		hasArticlesWith = categoryServiceImpl.hasArticlesWith(code);
		Assert.assertTrue(hasArticlesWith);
		
		code= "SoftwareDeDocumentacion";
		hasArticlesWith = categoryServiceImpl.hasArticlesWith(code);
		Assert.assertFalse(hasArticlesWith);

	}

}
