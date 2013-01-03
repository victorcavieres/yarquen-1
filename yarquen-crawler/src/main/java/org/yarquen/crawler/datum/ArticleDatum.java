package org.yarquen.crawler.datum;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;
import org.yarquen.crawler.util.TupleUtils;

import cascading.tuple.Fields;
import cascading.tuple.Tuple;

import com.bixolabs.cascading.BaseDatum;

/**
 * Article datum
 * 
 * @author Jorge Riquelme Santana
 * @date 17/08/2012
 * @version $Id$
 * 
 */
public class ArticleDatum extends BaseDatum implements Writable
{
	public static final String AUTHOR = fieldName(ArticleDatum.class, "author");
	public static final String DATE = fieldName(ArticleDatum.class, "date");
	public static final Fields FIELDS;
	public static final String KEYWORDS = fieldName(ArticleDatum.class,
			"keywords");
	public static final String PLAIN_TEXT = fieldName(ArticleDatum.class,
			"plainText");
	public static final String SUMMARY = fieldName(ArticleDatum.class,
			"summary");
	public static final String TITLE = fieldName(ArticleDatum.class, "title");
	public static final String URL = fieldName(ArticleDatum.class, "url");
	private static final long serialVersionUID = -4745882286075851714L;

	static
	{
		FIELDS = new Fields(URL, TITLE, DATE, SUMMARY, AUTHOR, KEYWORDS,
				PLAIN_TEXT);
	}

	public ArticleDatum()
	{
		super(FIELDS);
	}

	public ArticleDatum(Tuple tuple)
	{
		super(FIELDS, tuple);
	}

	public String getAuthor()
	{
		return _tupleEntry.getString(AUTHOR);
	}

	public String getDate()
	{
		return _tupleEntry.getString(DATE);
	}

	public String[] getKeywords()
	{
		return TupleUtils.makeObjectArrayFromTuple(String.class,
				(Tuple) _tupleEntry.get(KEYWORDS));
	}

	public String getPlainText()
	{
		return _tupleEntry.getString(PLAIN_TEXT);
	}

	public String getSummary()
	{
		return _tupleEntry.getString(SUMMARY);
	}

	public String getTitle()
	{
		return _tupleEntry.getString(TITLE);
	}

	public String getUrl()
	{
		return _tupleEntry.getString(URL);
	}

	@Override
	public void readFields(DataInput in) throws IOException
	{
		setUrl(in.readUTF());
		setTitle(in.readUTF());
		setDate(in.readUTF());
		setSummary(in.readUTF());
		setAuthor(in.readUTF());

		final int k = in.readInt();
		final String[] keywords = new String[k];
		for (int i = 0; i < k; i++)
		{
			keywords[i] = in.readUTF();
		}
		setKeywords(keywords);

		setPlainText(in.readUTF());
	}

	public void setAuthor(String author)
	{
		_tupleEntry.set(AUTHOR, author);
	}

	public void setDate(String date)
	{
		_tupleEntry.set(DATE, date);
	}

	public void setKeywords(String[] keywords)
	{
		_tupleEntry.set(KEYWORDS, TupleUtils.makeTupleFrom(keywords));
	}

	public void setPlainText(String plainText)
	{
		_tupleEntry.set(PLAIN_TEXT, plainText);
	}

	public void setSummary(String summary)
	{
		_tupleEntry.set(SUMMARY, summary);
	}

	public void setTitle(String title)
	{
		_tupleEntry.set(TITLE, title);
	}

	public void setUrl(String url)
	{
		_tupleEntry.set(URL, url);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(getUrl());
		out.writeUTF(getTitle());
		out.writeUTF(getDate());
		out.writeUTF(getSummary());
		out.writeUTF(getAuthor());

		final String[] keywords = getKeywords();
		out.writeInt(keywords.length);
		for (String kw : keywords)
		{
			out.writeUTF(kw);
		}

		out.writeUTF(getPlainText());
	}
}
