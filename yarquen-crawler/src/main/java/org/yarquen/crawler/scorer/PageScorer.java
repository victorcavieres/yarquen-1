package org.yarquen.crawler.scorer;

import java.util.List;

import org.dom4j.Document;

import bixo.datum.Outlink;
import bixo.datum.ParsedDatum;

/**
 * Page Scorer
 * 
 * @author Jorge Riquelme Santana
 * @date 07/08/2012
 * @version $Id$
 * 
 */
public interface PageScorer
{
	float getPageScore(ParsedDatum datum, Document doc, List<Outlink> outlinks);
}
