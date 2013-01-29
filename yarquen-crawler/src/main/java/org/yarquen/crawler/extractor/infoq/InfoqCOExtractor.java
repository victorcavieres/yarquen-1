package org.yarquen.crawler.extractor.infoq;

import java.util.List;

import org.dom4j.Document;
import org.yarquen.crawler.datum.ArticleDatum;
import org.yarquen.crawler.extractor.ArticleExtractor;

import bixo.datum.Outlink;
import bixo.datum.ParsedDatum;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 05/08/2012
 * @version $Id$
 * 
 */
public class InfoqCOExtractor implements ArticleExtractor {
	@Override
	public ArticleDatum extractArticles(ParsedDatum parsedDatum, Document doc,
			List<Outlink> outlinks) {
		return null;
		// LOGGER.info("extracting articles from {}", sourceUrl);
		//
		// // keywoords
		// String[] keywords = null;
		// @SuppressWarnings("unchecked")
		// final List<Node> tagNodes = doc
		// .selectNodes("//div[@id='articleText']//a[@rel='tag']/text()");
		// if (tagNodes != null && !tagNodes.isEmpty())
		// {
		// keywords = new String[tagNodes.size()];
		// int i = 0;
		// for (Node node : tagNodes)
		// {
		// keywords[i++] = node.getText();
		// }
		// LOGGER.trace("tagNodes: {}", keywords);
		// }
		//
		// // title
		// @SuppressWarnings("unchecked")
		// final List<Node> titleNodes = doc
		// .selectNodes("//div[@id='articleHead']/h1/text()");
		// LOGGER.info("titleNodes: {}", titleNodes);
		// String title = "";
		// if (titleNodes != null && !titleNodes.isEmpty())
		// {
		// title = titleNodes.get(0).getText();
		// }
		// LOGGER.trace("title: {}", title);
		//
		// final ArticleDatum co = new ArticleDatum();
		// co.setUrl(sourceUrl);
		// co.setTitle(title);
		// co.setDate("");
		// co.setSummary("");
		// co.setAuthor("");
		// co.setKeywords(keywords);
		// co.setPlainText("");
		//
		// return co;
	}
}
