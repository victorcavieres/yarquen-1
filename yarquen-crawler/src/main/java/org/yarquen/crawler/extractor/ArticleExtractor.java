package org.yarquen.crawler.extractor;

import java.util.List;

import org.dom4j.Document;
import org.yarquen.crawler.datum.ArticleDatum;

import bixo.datum.Outlink;
import bixo.datum.ParsedDatum;

/**
 * Article extractor
 * 
 * @author Jorge Riquelme Santana
 * @date 05/08/2012
 * @version $Id$
 * 
 */
public interface ArticleExtractor {
	/**
	 * Extract an article from the document
	 * 
	 * @param parsedDatum
	 *            bixo parsed datum
	 * @param doc
	 *            page DOM
	 * @param outlinks
	 *            page links
	 * @return extracted article or <code>null</code>
	 */
	ArticleDatum extractArticles(ParsedDatum parsedDatum, Document doc,
			List<Outlink> outlinks);
}
