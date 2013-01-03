package org.yarquen.crawler.extractor.dzone;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.crawler.datum.ArticleDatum;
import org.yarquen.crawler.extractor.ArticleExtractor;
import org.yarquen.crawler.util.DocumentUtils;

import bixo.datum.Outlink;
import bixo.datum.ParsedDatum;

/**
 * DZone article extractor
 * 
 * @author Jorge Riquelme Santana
 * @date 05/08/2012
 * @version $Id$
 * 
 */
public class DzoneArticleExtractor implements ArticleExtractor
{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DzoneArticleExtractor.class);

	@Override
	public ArticleDatum extractArticles(ParsedDatum parsedDatum, Document doc,
			List<Outlink> outlinks)
	{
		LOGGER.info("extracting articles from {} {}", parsedDatum.getUrl(),
				parsedDatum.getHostAddress());

		final Node articleNode = doc.selectSingleNode("//div[@id='article']");
		if (articleNode != null)
		{
			// title
			final Node titleNode = articleNode
					.selectSingleNode("//div[@id='articleHead']/h1/text()");
			String title = "";
			if (titleNode != null)
			{
				title = titleNode.getText().trim();
			}
			LOGGER.trace("title: {}", title);

			// date
			final Node dateNode = articleNode
					.selectSingleNode("//div[@id='date']/text()");
			String date = "";
			if (dateNode != null)
			{
				date = dateNode.getText().trim();
			}
			LOGGER.trace("date: {}", date);

			// author
			final Node authorNode = articleNode
					.selectSingleNode("//b[@id='authorname']/text()");
			String author = "";
			if (authorNode != null)
			{
				author = authorNode.getText().trim();
			}
			LOGGER.trace("author: {}", author);

			// summary
			final Node summaryNode = articleNode
					.selectSingleNode("//div[@id='articleText']/*/div[@class='content']/p[1]");
			String summary = "";
			if (summaryNode != null)
			{
				summary = summaryNode.getText().trim();
			}
			LOGGER.trace("summary: {}", summary);

			// keywoords
			String[] keywords = null;
			@SuppressWarnings("unchecked")
			final List<Node> tagNodes = articleNode
					.selectNodes("//div[@id='articleText']//a[@rel='tag']");
			if (tagNodes != null && !tagNodes.isEmpty())
			{
				LOGGER.trace("{} keywords detected", tagNodes.size());
				keywords = new String[tagNodes.size()];
				int i = 0;
				for (Node node : tagNodes)
				{
					final String kw = node.getText().trim();
					LOGGER.trace("keyword[{}]: {}", i, kw);
					keywords[i++] = kw;
				}
			}
			else
			{
				//FIXME
				LOGGER.trace("no keywords detected");
				return null;
			}

			// plain text
			final Node contentNode = articleNode
					.selectSingleNode("//div[@id='articleText']/*/div[@class='content']");
			String plainText = DocumentUtils.getPlainText(contentNode);
			LOGGER.trace("plainText: {}", plainText);

			final ArticleDatum article = new ArticleDatum();
			article.setUrl(parsedDatum.getUrl());
			article.setTitle(title);
			article.setDate(date);
			article.setSummary(summary);
			article.setAuthor(author);
			article.setKeywords(keywords);
			article.setPlainText(plainText);

			return article;
		}
		else
		{
			return null;
		}
	}
}
