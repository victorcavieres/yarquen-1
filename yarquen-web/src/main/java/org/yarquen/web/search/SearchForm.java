package org.yarquen.web.search;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.yarquen.web.lucene.ArticleSearch;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * @version $Id$
 * 
 */
@Controller
public class SearchForm
{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SearchForm.class);

	@Resource
	private ArticleSearch articleSearch;

	@RequestMapping(value = "/articles/search", method = RequestMethod.GET)
	public String setupForm(Model model)
	{
		model.addAttribute("searchFields", new SearchFields());
		return "articles/search";
	}

	@RequestMapping(value = "/articles", method = RequestMethod.GET)
	public String processSubmit(SearchFields searchFields,
			BindingResult result, Model model)
	{
		final String query = searchFields.getQuery();
		LOGGER.debug("query: {}", query);
		if (query == null || query.trim().equals(""))
		{
			return "articles/search";
		}
		else
		{
			try
			{
				List<SearchResult> results = articleSearch.search(searchFields);

				if (results.isEmpty())
				{
					return "articles/search";
				}
				else
				{
					model.addAttribute("results", results);
					return "articles/search";
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException("ahg!", e);
			}
			catch (ParseException e)
			{
				throw new RuntimeException("ua!", e);
			}
		}
	}
}
