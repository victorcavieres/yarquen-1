package org.yarquen.web.record;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;
import org.yarquen.web.enricher.EnrichmentRecord;
import org.yarquen.web.enricher.EnrichmentRecordRepository;

/**
 * 
 * @author Choon-ho Yoon
 * @date Mar 20, 2013
 * @version $id$
 * 
 */
@Controller
@RequestMapping("/articles/record/{articleId}")
public class RecordController {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RecordController.class);

	@Resource
	private EnrichmentRecordRepository enrichmentRecordRepository;
	@Resource
	private ArticleRepository articleRepository;

	@RequestMapping(method = RequestMethod.GET)
	public String initRecordView(@PathVariable String articleId, Model model,
			HttpServletRequest request) {
		LOGGER.trace("looking for versions of article");
		final Article article = articleRepository.findOne(articleId);
		if (article != null) {
			final List<EnrichmentRecord> articleHistory = enrichmentRecordRepository
					.findByArticleId(articleId);
			
			LOGGER.debug("Article ID: [{}] has {} enrichment records.",
					articleId, articleHistory.size());
			return "articles/record";
		} else {
			throw new RuntimeException("Article ID " + articleId
					+ " does not exist.");
		}
	}
}
