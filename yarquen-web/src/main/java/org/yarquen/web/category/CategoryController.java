package org.yarquen.web.category;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.yarquen.category.CategoryBranch;
import org.yarquen.category.CategoryService;
import org.yarquen.web.enricher.CategoryTreeBuilder;

@Controller
@RequestMapping("/category")
public class CategoryController {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CategoryController.class);

	@Resource
	private CategoryService categoryService;
	
	@Resource
	private CategoryTreeBuilder categoryTreeBuilder;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(CategoryBranch.class,
				new PropertyEditorSupport() {
					@Override
					public void setAsText(String branch) {
						try {
							LOGGER.trace(
									"converting {} to a CategoryBranch object",
									branch);
							final CategoryBranch categoryBranch = new CategoryBranch();
							final StringTokenizer tokenizer = new StringTokenizer(
									branch, ".");
							while (tokenizer.hasMoreTokens()) {
								final String code = tokenizer.nextToken();
								categoryBranch.addSubCategory(code, null);
							}
							// fill names
							categoryService
									.completeCategoryBranchNodeNames(categoryBranch);
							setValue(categoryBranch);
						} catch (RuntimeException e) {
							LOGGER.error(":(", e);
							throw e;
						}
					}
				});
	}

	@RequestMapping(value = "/setupCategory", method = RequestMethod.GET)
	public String setupSkills(Model model) {
		LOGGER.debug("setuping category edit");
		// categories
		final List<Map<String, Object>> categoryTree = categoryTreeBuilder
				.buildTree();
		model.addAttribute("categories", categoryTree);
		return "category/edit";

	}
	
	@RequestMapping(value = "/addCategory", method = RequestMethod.GET)
	public void addcategory(@RequestParam("parent") String parentCategory,@RequestParam("child") String childCategory,HttpServletResponse rsp) throws IOException {
		final CategoryBranch categoryBranch = CategoryBranch.parse(parentCategory);
		LOGGER.debug("adding category {} to parent {}",childCategory,categoryBranch);
		try{
			String code=categoryService.addCategory(categoryBranch, childCategory);
			rsp.setStatus(HttpServletResponse.SC_OK);
			rsp.getWriter().print(code);
		}catch(Exception e){
			LOGGER.error("can't add Category", e);
			rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			rsp.getWriter().print(e.getMessage());
			
		}
	}
	
	@RequestMapping(value = "/CategoryFake", method = RequestMethod.GET)
	@ResponseStatus( HttpStatus.OK )
	public void addcategoryFake(@RequestParam("parent") String parentCategory,@RequestParam("child") String childCategory) {
		LOGGER.debug("adding category {} to parent {}",childCategory,parentCategory);
	}
	
	@RequestMapping(value = "/renameCategory", method = RequestMethod.GET)
	public void renameCategory(@RequestParam("parent") String parentCategory,@RequestParam("oldCode") String oldCode, @RequestParam("newName") String newName,HttpServletResponse rsp) throws IOException {
		final CategoryBranch categoryBranch = CategoryBranch.parse(parentCategory);
		LOGGER.debug("renaming category to {} from parent {}",newName,categoryBranch);
		try{
			categoryService.renameCategory(categoryBranch, oldCode,newName);
			rsp.setStatus(HttpServletResponse.SC_OK);
		}catch(Exception e){
			LOGGER.error("can't add Category", e);
			rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			rsp.getWriter().print(e.getMessage());
			
		}
	}
	
	@RequestMapping(value = "/renameCategoryFake", method = RequestMethod.GET)
	@ResponseStatus( HttpStatus.OK )
	public void renameCategoryFake(@RequestParam("parent") String parentCategory,@RequestParam("oldCode") String oldCode, @RequestParam("newName") String newName) {
		LOGGER.debug("renaming category to {} from parent {}",newName,parentCategory);
	}
	
	@RequestMapping(value = "/removeCategory", method = RequestMethod.GET)
	public void removeCategory(@RequestParam("branch") String branch,HttpServletResponse rsp) throws IOException {
		final CategoryBranch categoryBranch = CategoryBranch.parse(branch);
		LOGGER.debug("removing category branch {}",categoryBranch);
		try{
			categoryService.deleteCategory(categoryBranch);
			rsp.setStatus(HttpServletResponse.SC_OK);
		}catch(Exception e){
			LOGGER.error("can't add Category", e);
			rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			rsp.getWriter().print(e.getMessage());
			
		}
	}
	
	@RequestMapping(value = "/removeCategoryFake", method = RequestMethod.GET)
	@ResponseStatus( HttpStatus.OK )
	public void removeCategoryFake(@RequestParam("brach") String branch) {
		final CategoryBranch categoryBranch = CategoryBranch.parse(branch);
		LOGGER.debug("removing category branch {}",categoryBranch);
	}
	
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Internal error in server")
	class InternalServerException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InternalServerException(String msg) {
	        super(msg);
	    }
	}

}
