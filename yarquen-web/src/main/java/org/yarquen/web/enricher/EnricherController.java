package org.yarquen.web.enricher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.author.Author;
import org.yarquen.author.AuthorRepository;
import org.yarquen.category.Category;
import org.yarquen.category.CategoryRepository;
import org.yarquen.category.SubCategory;
import org.yarquen.keyword.Keyword;
import org.yarquen.keyword.KeywordRepository;

/**
 * Search form
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * @version $Id$
 * 
 */
@Controller
public class EnricherController {
	private static final String ID_SEPARATOR = ".";
	private static final String NAME_SEPARATOR = "/";
	private static final Logger LOGGER = LoggerFactory
			.getLogger(EnricherController.class);

	@Resource
	private ArticleRepository articleRepository;
	@Resource
	private AuthorRepository authorRepository;
	@Resource
	private CategoryRepository categoryRepository;
	@Resource
	private KeywordRepository keywordRepository;

	@RequestMapping(value = "/articles/enricher", method = RequestMethod.GET)
	public ModelAndView setupForm(@RequestParam("id") String articleId) {
		final ModelAndView mv = new ModelAndView("articles/enricher");

		final Article article = articleRepository.findOne(articleId);
		if (article == null) {
			throw new RuntimeException("Article " + articleId + " not found");
		} else {
			LOGGER.debug("enriching article id={} title={}", article.getId(),
					article.getTitle());

			// TODO:remove?
			final List<String> cats = new ArrayList<String>();
			article.setCategories(cats);

			// article to enrich
			mv.addObject("article", article);

			// authors
			final List<String> authorsName = new LinkedList<String>();
			final Iterable<Author> authors = authorRepository.findAll();
			for (Author author : authors) {
				authorsName.add(author.getName());
			}
			mv.addObject("authors", authorsName);

			// keywords
			final List<String> keywordsName = new LinkedList<String>();
			final Iterable<Keyword> keywords = keywordRepository.findAll();
			for (Keyword keyword : keywords) {
				keywordsName.add(keyword.getName());
			}
			mv.addObject("keywords", keywordsName);

			// categories
			final List<Map<String, Object>> categoriesJson = new ArrayList<Map<String, Object>>();
			final Iterable<Category> categories = categoryRepository.findAll();
			for (Category c : categories) {
				final Map<String, Object> cj = new HashMap<String, Object>();
				cj.put("metadata", buildMetadataMap(c, null, null));
				cj.put("data", c.getName());
				if (c.getSubCategories() != null
						&& !c.getSubCategories().isEmpty()) {
					final List<Map<String, Object>> subCategories = new ArrayList<Map<String, Object>>();
					for (SubCategory sc : c.getSubCategories()) {
						final Map<String, Object> scj = buildSubcategory(sc,
								c.getCode(), c.getName());
						subCategories.add(scj);
					}
					cj.put("children", subCategories);
				}
				categoriesJson.add(cj);
			}
			mv.addObject("categories", categoriesJson);
		}
		return mv;
	}

	@RequestMapping(value = "/articles/enricher", method = RequestMethod.POST)
	public String update(@ModelAttribute("article") Article article,
			BindingResult result) {

		LOGGER.debug("errors? {}", result.hasErrors());
		LOGGER.debug(
				"{}\n {}\n {}\n {}\n {}\n {}\n {}\n {}\n {}\n",
				new Object[] { article.getAuthor(), article.getCategories(),
						article.getDate(), article.getId(),
						article.getKeywords(), article.getPlainText(),
						article.getSummary(), article.getTitle(),
						article.getUrl() });

		return "redirect:search";
	}

	private Map<String, Object> buildMetadataMap(SubCategory c, String code,
			String name) {
		final Map<String, Object> map = new HashMap<String, Object>();
		map.put("code",
				code != null ? code + ID_SEPARATOR + c.getCode() : c.getCode());
		map.put("name",
				name != null ? name + NAME_SEPARATOR + c.getName() : c
						.getName());
		return map;
	}

	private Map<String, Object> buildSubcategory(SubCategory c, String code,
			String name) {
		final Map<String, Object> cj = new HashMap<String, Object>();
		cj.put("metadata", buildMetadataMap(c, code, name));
		cj.put("data", c.getName());
		if (c.getSubCategories() != null && !c.getSubCategories().isEmpty()) {
			final List<Map<String, Object>> subCategories = new ArrayList<Map<String, Object>>();
			for (SubCategory sc : c.getSubCategories()) {
				final Map<String, Object> scj = buildSubcategory(sc, code
						+ ID_SEPARATOR + c.getCode(),
						name + NAME_SEPARATOR + c.getName());
				subCategories.add(scj);
			}
			cj.put("children", subCategories);
		}
		return cj;
	}
}
