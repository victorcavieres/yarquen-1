package org.yarquen.crawler.filters;

import org.yarquen.crawler.datum.CrawlDbDatum;

import bixo.datum.UrlStatus;
import cascading.tuple.TupleEntry;


import com.bixolabs.cascading.BaseSplitter;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 23/05/2012
 * @version $Id$
 * 
 */
public class SplitFetchedUnfetchedSSCrawlDatums extends BaseSplitter
{
	private static final long serialVersionUID = 3030137500118978806L;

	@Override
	public String getLHSName()
	{
		return "unfetched crawl db datums";
	}

	@Override
	// LHS represents unfetched tuples
	public boolean isLHS(TupleEntry tupleEntry)
	{
		final CrawlDbDatum datum = new CrawlDbDatum(tupleEntry);
		final UrlStatus status = datum.getLastStatus();
		return status == UrlStatus.UNFETCHED
				|| status == UrlStatus.SKIPPED_DEFERRED
				|| status == UrlStatus.SKIPPED_BY_SCORER
				|| status == UrlStatus.SKIPPED_BY_SCORE
				|| status == UrlStatus.SKIPPED_TIME_LIMIT
				|| status == UrlStatus.SKIPPED_INTERRUPTED
				|| status == UrlStatus.SKIPPED_INEFFICIENT
				|| status == UrlStatus.ABORTED_SLOW_RESPONSE
				|| status == UrlStatus.ERROR_IOEXCEPTION;
	}
}