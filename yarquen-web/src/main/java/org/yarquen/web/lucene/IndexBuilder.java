package org.yarquen.web.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.index.CategoryDocumentBuilder;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.yarquen.article.Article;
import org.yarquen.article.ArticleRepository;

/**
 * Lucenes index builder
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

	@Resource
	private ArticleRepository articleRepository;

	@Value("#{config.indexDirectory}")
	private String indexDirectoryPath;

	@Value("#{config.taxoDirectory}")
	private String taxoDirectoryPath;

	public void createIndex() throws IOException
	{
		final StandardAnalyzer analyzer = new StandardAnalyzer(
				Version.LUCENE_40);
		final IndexWriterConfig config = new IndexWriterConfig(
				Version.LUCENE_40, analyzer);

		final Directory indexDirectory = new NIOFSDirectory(new File(
				indexDirectoryPath));
		final IndexWriter indexWriter = new IndexWriter(indexDirectory, config);

		final Directory taxoDirectory = new NIOFSDirectory(new File(
				taxoDirectoryPath));
		final TaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(
				taxoDirectory, OpenMode.CREATE);

		final Iterable<Article> articles = articleRepository.findAll();
		int c = 0;
		for (Article article : articles)
		{
			addArticle(indexWriter, taxoWriter, article);
			c++;
		}
		indexWriter.close();
		taxoWriter.close();

		LOGGER.debug("{} articles indexed", c);
	}

	private void addArticle(IndexWriter indexWriter, TaxonomyWriter taxoWriter,
			Article article) throws IOException
	{
		// extract facets
		final List<CategoryPath> facets = getFacetsWithValue(article);
		final CategoryDocumentBuilder categoryBuilder = new CategoryDocumentBuilder(
				taxoWriter).setCategoryPaths(facets);

		// doc
		final Document doc = new Document();
		// add fields
		addFieldsToDoc(doc, article);
		// add facets
		categoryBuilder.build(doc);

		// add doc to index
		indexWriter.addDocument(doc);
	}

	private void addFieldsToDoc(Document doc, Article article)
	{
		doc.add(new TextField(Article.Fields.ID.toString(), article.getId(),
				Field.Store.YES));
		doc.add(new TextField(Article.Fields.PLAIN_TEXT.toString(), article
				.getPlainText(), Field.Store.YES));
		doc.add(new TextField(Article.Fields.TITLE.toString(), article
				.getTitle(), Field.Store.YES));
		doc.add(new StringField(Article.Fields.URL.toString(),
				article.getUrl(), Field.Store.YES));
	}

	private List<CategoryPath> getFacetsWithValue(Article article)
	{
		final List<CategoryPath> facets = new ArrayList<CategoryPath>();

		if (article.getAuthor() != null)
		{
			facets.add(new CategoryPath(Article.Facets.AUTHOR.toString(),
					article.getAuthor()));
		}
		if (article.getDate() != null)
		{
			final String date = article.getDate();
			// FIXME
			final String year = date.substring(date.lastIndexOf(".") + 1);
			facets.add(new CategoryPath(Article.Facets.YEAR.toString(), year));
		}
		if (article.getKeywords() != null)
		{
			for (String kw : article.getKeywords())
			{
				facets.add(new CategoryPath(Article.Facets.KEYWORD.toString(),
						kw));
			}
		}

		return facets;
	}
}
