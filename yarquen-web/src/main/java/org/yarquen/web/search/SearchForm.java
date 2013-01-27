package org.yarquen.web.search;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.http.client.utils.URIBuilder;
import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.yarquen.web.lucene.ArticleSearcher;

/**
 * Search form
 * 
 * @author Jorge Riquelme Santana
 * @date 23/11/2012
 * @version $Id$
 * 
 */
@Controller
public class SearchForm
{
	private static final String BASE_URL = "http://asdf.cl/jkl";
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SearchForm.class);

	@Resource
	private ArticleSearcher articleSearcher;

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
				final YarquenFacets facetsCount = new YarquenFacets();
				final List<SearchResult> results = articleSearcher.search(
						searchFields, facetsCount);

				// send back applied facets
				final List<YarquenFacet> appliedFacets = addAppliedFacetsToModel(
						searchFields, facetsCount, model);

				// facet count
				model.addAttribute("facets", facetsCount);

				// calc facet's url
				final Set<YarquenFacet> allFacets = new HashSet<YarquenFacet>();
				allFacets.addAll(facetsCount.getAuthor());
				allFacets.addAll(facetsCount.getKeyword());
				allFacets.addAll(facetsCount.getYear());
				allFacets.addAll(appliedFacets);

				generateFacetsUrl(allFacets, query);

				// result
				if (!results.isEmpty())
				{
					model.addAttribute("results", results);
				}

				return "articles/search";
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

	@RequestMapping(value = "/articles/search", method = RequestMethod.GET)
	public String setupForm(Model model)
	{
		model.addAttribute("searchFields", new SearchFields());
		return "articles/search";
	}

	private List<YarquenFacet> addAppliedFacetsToModel(
			SearchFields searchFields, YarquenFacets facetsCount, Model model)
	{
		final List<YarquenFacet> appliedFacets = new ArrayList<YarquenFacet>();

		if (searchFields.getAuthor() != null
				&& !facetsCount.getAuthor().isEmpty())
		{
			final YarquenFacet facet = facetsCount.getAuthor().get(0);
			facet.setApplied(true);
			model.addAttribute("authorFacet", facet);

			appliedFacets.add(facet);
		}
		if (searchFields.getYear() != null && !facetsCount.getYear().isEmpty())
		{
			final YarquenFacet facet = facetsCount.getYear().get(0);
			facet.setApplied(true);
			model.addAttribute("yearFacet", facet);

			appliedFacets.add(facet);
		}

		// this is trickier, I have to move applied facets from count to a
		// new list
		if (searchFields.getKeyword() != null
				&& !searchFields.getKeyword().isEmpty()
				&& !facetsCount.getKeyword().isEmpty())
		{
			final List<YarquenFacet> appliedKw = new ArrayList<YarquenFacet>();
			for (YarquenFacet kwFc : facetsCount.getKeyword())
			{
				// if the facet is applied, move it to appliedKw list
				if (searchFields.getKeyword().contains(kwFc.getValue()))
				{
					kwFc.setApplied(true);
					appliedKw.add(kwFc);
				}
			}
			if (!appliedKw.isEmpty())
			{
				// remove applied facets from count collection
				for (YarquenFacet kwFc : appliedKw)
				{
					facetsCount.getKeyword().remove(kwFc);
				}

				appliedFacets.addAll(appliedKw);

				model.addAttribute("keywordFacets", appliedKw);
			}
		}

		return appliedFacets;
	}

	private void generateFacetsUrl(Set<YarquenFacet> allFacets, String query)
	{
		for (YarquenFacet facet : allFacets)
		{
			final String generatedUrl = generateUrl(allFacets, facet,
					facet.isApplied(), query);
			facet.setUrl(generatedUrl.substring(BASE_URL.length()));
		}
	}

	private String generateUrl(Set<YarquenFacet> allFacets, YarquenFacet facet,
			boolean remove, String query)
	{
		URIBuilder uriBuilder = null;
		try
		{
			uriBuilder = new URIBuilder(BASE_URL);
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException("error in the matrix", e);
		}

		uriBuilder.addParameter("query", query);

		for (YarquenFacet f : allFacets)
		{
			if (f == facet)
			{
				if (!f.isApplied() && !remove)
				{
					uriBuilder.addParameter(f.getName(), f.getValue());
				}
				else if (f.isApplied() && remove)
				{
					// nothing to do
				}
				else
				{
					LOGGER.warn(
							"this doesn't make sense: facet={} vaue={} applied={} remove={}",
							new Object[] { f.getName(), f.getValue(),
									f.isApplied(), remove });
				}
			}
			else if (f.isApplied())
			{
				uriBuilder.addParameter(f.getName(), f.getValue());
			}
		}

		try
		{
			return uriBuilder.build().toString();
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException("douh!", e);
		}
	}
}
