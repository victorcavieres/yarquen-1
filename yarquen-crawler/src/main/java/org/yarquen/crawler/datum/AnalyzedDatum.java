package org.yarquen.crawler.datum;

import org.yarquen.crawler.util.TupleUtils;

import bixo.datum.Outlink;
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
public class AnalyzedDatum extends BaseDatum
{
	public static final String ARTICLES = fieldName(AnalyzedDatum.class, "articles");
	public static final Fields FIELDS;
	public static final String OUTLINKS = fieldName(AnalyzedDatum.class, "outlinks");
	public static final String PAGE_SCORE = fieldName(AnalyzedDatum.class, "pageScore");
	public static final String URL = fieldName(AnalyzedDatum.class, "url");

	static
	{
		FIELDS = new Fields(URL, PAGE_SCORE, ARTICLES,
				OUTLINKS);
	}

	public AnalyzedDatum()
	{
		super(FIELDS);
	}

	public AnalyzedDatum(Fields fields, String url, float pageScore,
			ArticleDatum[] pageResults, Outlink[] outlinks)
	{
		super(fields);

		setUrl(url);
		setPageScore(pageScore);
		setArticles(pageResults);
		setOutlinks(outlinks);
	}

	public AnalyzedDatum(Fields fields, Tuple tuple)
	{
		super(fields, tuple);
	}

	public AnalyzedDatum(String url, float pageScore,
			ArticleDatum[] pageResults, Outlink[] outlinks)
	{
		this(FIELDS, url, pageScore, pageResults, outlinks);
	}

	public AnalyzedDatum(Tuple tuple)
	{
		super(FIELDS, tuple);
	}

	public ArticleDatum[] getArticles()
	{
		return TupleUtils.makeObjectArrayFromTuple(ArticleDatum.class,
				(Tuple) _tupleEntry.get(ARTICLES));
	}

	public Outlink[] getOutlinks()
	{
		return TupleUtils.makeObjectArrayFromTuple(Outlink.class,
				(Tuple) _tupleEntry.get(OUTLINKS));
	}

	public float getPageScore()
	{
		return _tupleEntry.getFloat(PAGE_SCORE);
	}

	public String getUrl()
	{
		return _tupleEntry.getString(URL);
	}

	public void setArticles(ArticleDatum[] results)
	{
		_tupleEntry.set(ARTICLES, TupleUtils.makeTupleFrom(results));
	}

	public void setOutlinks(Outlink[] outlinks)
	{
		_tupleEntry.set(OUTLINKS, TupleUtils.makeTupleFrom(outlinks));
	}

	public void setPageScore(float pageScore)
	{
		_tupleEntry.set(PAGE_SCORE, pageScore);
	}

	public void setUrl(String url)
	{
		_tupleEntry.set(URL, url);
	}
}
