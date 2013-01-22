package org.yarquen.web.enricher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.author.Author;
import org.yarquen.author.AuthorRepository;
import org.yarquen.category.Category;
import org.yarquen.category.CategoryBranch;
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
@RequestMapping(value = "/articles/enricher/{id}")
public class EnricherController {
	private static final String ARTICLE = "article";
	private static final String AUTHORS = "authors";
	private static final String CATEGORIES = "categories";
	private static final String KEYWORDS = "keywords";
	private static final Logger LOGGER = LoggerFactory
			.getLogger(EnricherController.class);
	private static final String REFERER = "referer";

	@Resource
	private ArticleRepository articleRepository;
	@Resource
	private AuthorRepository authorRepository;
	@Resource
	private CategoryRepository categoryRepository;
	@Resource
	private CategoryTreeBuilder categoryTreeBuilder;
	@Resource
	private KeywordRepository keywordRepository;

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(@PathVariable String id, Model model,
			HttpServletRequest request) {

		final Article article = articleRepository.findOne(id);
		if (article == null) {
			throw new RuntimeException("Article " + id + " not found");
		} else {
			LOGGER.debug("enriching article id={} title={}", article.getId(),
					article.getTitle());

			// TODO:remove
			{
				final CategoryBranch categoryBranch = new CategoryBranch();
				List<SubCategory> c = new ArrayList<SubCategory>();
				c.add(new Category("Softwate", "Software"));
				c.add(new SubCategory("Eclipse", "Eclipse"));
				categoryBranch.setCategories(c);

				final List<CategoryBranch> cats = new ArrayList<CategoryBranch>();
				cats.add(categoryBranch);
				article.setCategories(cats);
			}

			// save referer
			final String referer = request.getHeader("Referer");
			LOGGER.trace("referer: {}", referer);
			model.addAttribute(REFERER, referer);

			// article to enrich
			LOGGER.trace("articles: {}", article);
			model.addAttribute(ARTICLE, article);

			// authors
			final List<String> authorsName = new LinkedList<String>();
			final Iterable<Author> authors = authorRepository.findAll();
			for (Author author : authors) {
				authorsName.add(author.getName());
			}
			model.addAttribute(AUTHORS, authorsName);

			// keywords
			final List<String> keywordsName = new LinkedList<String>();
			final Iterable<Keyword> keywords = keywordRepository.findAll();
			for (Keyword keyword : keywords) {
				keywordsName.add(keyword.getName());
			}
			model.addAttribute(KEYWORDS, keywordsName);

			// categories
			final List<Map<String, Object>> categoryTree = categoryTreeBuilder
					.buildTree();
			model.addAttribute(CATEGORIES, categoryTree);
		}
		return "articles/enricher";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String update(@Valid Article article, BindingResult result,
			Model model) {

		if (result.hasErrors()) {
			return "articles/enricher";
		} else {
			String referer = null;
			LOGGER.trace("pars: referer={} article={}", new Object[] { referer,
					article });
			LOGGER.debug(
					"{}\n {}\n {}\n {}\n {}\n {}\n {}\n {}\n {}\n",
					new Object[] { article.getAuthor(),
							article.getCategories(), article.getDate(),
							article.getId(), article.getKeywords(),
							article.getPlainText(), article.getSummary(),
							article.getTitle(), article.getUrl() });

			LOGGER.trace("referer: '{}'", referer);
			model.addAttribute("message", "article successfully enriched");
			model.addAttribute(REFERER, referer);
			return "message";
		}
	}
}
