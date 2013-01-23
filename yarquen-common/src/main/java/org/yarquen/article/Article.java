package org.yarquen.article;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.yarquen.category.CategoryBranch;

/**
 * Article
 * 
 * @author Jorge Riquelme Santana
 * @date 22/11/2012
 * @version $Id$
 * 
 */
public class Article {
	public static enum Facets {
		AUTHOR("author"), KEYWORD("keyword"), YEAR("year");

		private String label;

		private Facets(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		};
	}

	public static enum Fields {
		ID("id"), PLAIN_TEXT("plainText"), TITLE("title"), URL("url");

		private String label;

		private Fields(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		};
	}

	@Size(min = 5, max = 100)
	private String author;
	@Valid
	private List<CategoryBranch> categories;
	private String date;
	@Id
	private String id;
	private List<String> keywords;
	@NotEmpty(groups = FullArticle.class)
	private String plainText;
	private String summary;
	@NotEmpty
	private String title;
	@NotEmpty
	private String url;

	public String getAuthor() {
		return author;
	}

	public List<CategoryBranch> getCategories() {
		return categories;
	}

	public String getDate() {
		return date;
	}

	public String getId() {
		return id;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public String getPlainText() {
		return plainText;
	}

	public String getSummary() {
		return summary;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public void setAuthor(String value) {
		this.author = value;
	}

	public void setCategories(List<CategoryBranch> categories) {
		this.categories = categories;
	}

	public void setDate(String value) {
		this.date = value;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setKeywords(List<String> value) {
		this.keywords = value;
	}

	public void setPlainText(String value) {
		this.plainText = value;
	}

	public void setSummary(String value) {
		this.summary = value;
	}

	public void setTitle(String value) {
		this.title = value;
	}

	public void setUrl(String value) {
		this.url = value;
	}
}
