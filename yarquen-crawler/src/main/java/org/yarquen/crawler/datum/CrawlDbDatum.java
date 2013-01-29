package org.yarquen.crawler.datum;

import bixo.datum.UrlStatus;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

import com.bixolabs.cascading.BaseDatum;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 23/05/2012
 * @version $Id$
 * 
 */
@SuppressWarnings("serial")
public class CrawlDbDatum extends BaseDatum {
	public static final String LAST_FETCHED_FIELD = fieldName(
			CrawlDbDatum.class, "lastFetched");
	public static final String LAST_STATUS_FIELD = fieldName(
			CrawlDbDatum.class, "lastStatus");
	public static final String LINKS_SCORE_FIELD = fieldName(
			CrawlDbDatum.class, "linksScore");
	public static final String PAGE_SCORE_FIELD = fieldName(CrawlDbDatum.class,
			"pageScore");

	@SuppressWarnings("rawtypes")
	public static Class[] TYPES = { String.class, Long.class, String.class,
			Float.class, Float.class };

	public static final String URL_FIELD = fieldName(CrawlDbDatum.class, "url");

	public static final Fields FIELDS = new Fields(URL_FIELD,
			LAST_FETCHED_FIELD, LAST_STATUS_FIELD, LINKS_SCORE_FIELD,
			PAGE_SCORE_FIELD);

	public CrawlDbDatum() {
		super(FIELDS);
	}

	public CrawlDbDatum(String url) {
		this(url, 0, UrlStatus.UNFETCHED, 0.0f, 0.0f);
	}

	public CrawlDbDatum(String url, long lastFetched, UrlStatus lastStatus,
			float linksScore, float pageScore) {
		super(FIELDS);

		setUrl(url);
		setLastFetched(lastFetched);
		setLastStatus(lastStatus);
		setLinksScore(linksScore);
		setPageScore(pageScore);
	}

	public CrawlDbDatum(Tuple tuple) {
		super(FIELDS, tuple);
	}

	public CrawlDbDatum(TupleEntry tupleEntry) {
		super(tupleEntry);
		validateFields(tupleEntry.getFields(), FIELDS);
	}

	public long getLastFetched() {
		return _tupleEntry.getLong(LAST_FETCHED_FIELD);
	}

	public UrlStatus getLastStatus() {
		return UrlStatus.valueOf(_tupleEntry.getString(LAST_STATUS_FIELD));
	}

	public float getLinksScore() {
		return _tupleEntry.getFloat(LINKS_SCORE_FIELD);
	}

	public float getPageScore() {
		return _tupleEntry.getFloat(PAGE_SCORE_FIELD);
	}

	public String getUrl() {
		return _tupleEntry.getString(URL_FIELD);
	}

	public void setLastFetched(long lastFetched) {
		_tupleEntry.set(LAST_FETCHED_FIELD, lastFetched);
	}

	public void setLastStatus(UrlStatus lastStatus) {
		_tupleEntry.set(LAST_STATUS_FIELD, lastStatus.name());
	}

	public void setLinksScore(float linksScore) {
		_tupleEntry.set(LINKS_SCORE_FIELD, linksScore);
	}

	public void setPageScore(float pageScore) {
		_tupleEntry.set(PAGE_SCORE_FIELD, pageScore);
	}

	public void setUrl(String url) {
		_tupleEntry.set(URL_FIELD, url);
	}

	public String toString() {
		return String.format(
				"Page %s: last status %s, links score %.4f, page score %.4f",
				getUrl(), getLastStatus(), getLinksScore(), getPageScore());
	}
}
