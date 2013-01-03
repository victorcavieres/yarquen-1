package org.yarquen.web.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yarquen.model.Article;

/**
 * 
 * @author Jorge Riquelme Santana
 * @date 22/11/2012
 * @version $Id$
 * 
 */
public class IndexBuilder
{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(IndexBuilder.class);

	public void createIndex() throws IOException, JAXBException
	{
		final StandardAnalyzer analyzer = new StandardAnalyzer(
				Version.LUCENE_40);
		final Directory index = new NIOFSDirectory(new File(
				"/home/totex/local/tmp/lucene"));

		final IndexWriterConfig config = new IndexWriterConfig(
				Version.LUCENE_40, analyzer);

		final File articlesDir = new File("/home/totex/data-crawling-it");
		final Collection<File> articles = FileUtils.listFiles(articlesDir,
				new String[] { "xml" }, false);
		LOGGER.debug("{} files to index", articles.size());
		if (!articles.isEmpty())
		{
			final IndexWriter writer = new IndexWriter(index, config);

			final JAXBContext context = JAXBContext
					.newInstance("org.yarquen.model");
			final Unmarshaller unmarshaller = context.createUnmarshaller();

			for (File file : articles)
			{
				try
				{
					final JAXBElement<Article> articleElement = unmarshaller
							.unmarshal(new StreamSource(file), Article.class);
					addArticle(writer, articleElement.getValue());
				}
				catch (Exception ex)
				{
					LOGGER.error(
							"error while indexing " + file.getAbsolutePath(),
							ex);
				}
			}

			writer.close();
		}
	}

	private static void addArticle(IndexWriter writer, Article article)
			throws IOException
	{
		final Document doc = new Document();
		doc.add(new StringField("url", article.getUrl(), Field.Store.YES));
		doc.add(new TextField("title", article.getTitle(), Field.Store.YES));
		doc.add(new TextField("summary", article.getSummary(), Field.Store.YES));
		doc.add(new StringField("author", article.getAuthor(), Field.Store.YES));
		doc.add(new StringField("date", article.getDate(), Field.Store.YES));
		doc.add(new TextField("plainText", article.getPlainText(),
				Field.Store.YES));
		for (String kw : article.getKeywords())
		{
			doc.add(new StringField("keyword", kw, Field.Store.YES));
		}
		writer.addDocument(doc);
	}
}
