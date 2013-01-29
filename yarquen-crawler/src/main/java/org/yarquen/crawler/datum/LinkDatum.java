package org.yarquen.crawler.datum;

import cascading.tuple.Fields;
import cascading.tuple.Tuple;

import com.bixolabs.cascading.BaseDatum;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 23/05/2012
 * @version $Id$
 * 
 */
@SuppressWarnings("serial")
public class LinkDatum extends BaseDatum {
	// Cascading field names that correspond to the datum fields.
	public static final String URL_FN = fieldName(LinkDatum.class, "url");
	public static final String PAGE_SCORE_FN = fieldName(LinkDatum.class,
			"pagescore");
	public static final String LINK_SCORE_FN = fieldName(LinkDatum.class,
			"linkscore");
	public static final Fields FIELDS = new Fields(URL_FN, PAGE_SCORE_FN,
			LINK_SCORE_FN);

	public LinkDatum(Tuple tuple) {
		super(FIELDS, tuple);
	}

	public LinkDatum() {
		super(FIELDS);
	}

	public void setUrl(String url) {
		_tupleEntry.set(URL_FN, url);
	}

	public String getUrl() {
		return _tupleEntry.getString(URL_FN);
	}

	public void setLinkScore(float linkScore) {
		_tupleEntry.set(LINK_SCORE_FN, linkScore);
	}

	public float getLinkScore() {
		return _tupleEntry.getFloat(LINK_SCORE_FN);
	}

	public void setPageScore(float pageScore) {
		_tupleEntry.set(PAGE_SCORE_FN, pageScore);
	}

	public float getPageScore() {
		return _tupleEntry.getFloat(PAGE_SCORE_FN);
	}

	@Override
	public String toString() {
		return getUrl() + "\t" + getPageScore() + "\t" + getLinkScore();
	}
}
