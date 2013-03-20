package org.yarquen.web.enricher;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.yarquen.account.Skill;

/**
 * History of changes made to a version of an article
 * 
 * @author Choon-ho Yoon
 * @date 19/03/2013
 * @version $id$
 * 
 */
public class EnrichmentRecord {

	@Id
	private String id;

	@NotNull
	private String articleId;

	// It might have changed to empty
	private boolean changedAuthor;
	private String newAuthor;
	private String oldAuthor;

	private boolean changedDate;
	private String newDate;
	private String oldDate;

	private boolean changedSummary;
	private String newSummary;
	private String oldSummary;

	private List<String> addedKeywords;
	private List<String> removedKeywords;

	private List<Skill> addedProvidedSkills;
	private List<Skill> removedProvidedSkills;

	private List<Skill> addedRequiredSkills;
	private List<Skill> removedRequiredSkills;

	// If null, there was no update
	private String newTitle;
	private String oldTitle;

	private String newUrl;
	private String oldUrl;

	@NotNull
	private Date versionDate;

	@NotEmpty
	private String accountId;

	public String getOldAuthor() {
		return oldAuthor;
	}

	public void setOldAuthor(String oldAuthor) {
		this.oldAuthor = oldAuthor;
	}

	public String getOldDate() {
		return oldDate;
	}

	public void setOldDate(String oldDate) {
		this.oldDate = oldDate;
	}

	public String getOldSummary() {
		return oldSummary;
	}

	public void setOldSummary(String oldSummary) {
		this.oldSummary = oldSummary;
	}

	public String getOldTitle() {
		return oldTitle;
	}

	public void setOldTitle(String oldTitle) {
		this.oldTitle = oldTitle;
	}

	public String getOldUrl() {
		return oldUrl;
	}

	public void setOldUrl(String oldUrl) {
		this.oldUrl = oldUrl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}

	public boolean isChangedAuthor() {
		return changedAuthor;
	}

	public void setChangedAuthor(boolean changedAuthor) {
		this.changedAuthor = changedAuthor;
	}

	public String getNewAuthor() {
		return newAuthor;
	}

	public void setNewAuthor(String newAuthor) {
		this.newAuthor = newAuthor;
	}

	public List<String> getAddedKeywords() {
		return addedKeywords;
	}

	public void setAddedKeywords(List<String> addedKeywords) {
		this.addedKeywords = addedKeywords;
	}

	public List<String> getRemovedKeywords() {
		return removedKeywords;
	}

	public void setRemovedKeywords(List<String> removedKeywords) {
		this.removedKeywords = removedKeywords;
	}

	public List<Skill> getAddedProvidedSkills() {
		return addedProvidedSkills;
	}

	public void setAddedProvidedSkills(List<Skill> addedProvidedSkills) {
		this.addedProvidedSkills = addedProvidedSkills;
	}

	public List<Skill> getRemovedProvidedSkills() {
		return removedProvidedSkills;
	}

	public void setRemovedProvidedSkills(List<Skill> removedProvidedSkills) {
		this.removedProvidedSkills = removedProvidedSkills;
	}

	public List<Skill> getAddedRequiredSkills() {
		return addedRequiredSkills;
	}

	public void setAddedRequiredSkills(List<Skill> addedRequiredSkills) {
		this.addedRequiredSkills = addedRequiredSkills;
	}

	public List<Skill> getRemovedRequiredSkills() {
		return removedRequiredSkills;
	}

	public void setRemovedRequiredSkills(List<Skill> removedRequiredSkills) {
		this.removedRequiredSkills = removedRequiredSkills;
	}

	public String getNewTitle() {
		return newTitle;
	}

	public void setNewTitle(String newTitle) {
		this.newTitle = newTitle;
	}

	public String getNewUrl() {
		return newUrl;
	}

	public void setNewUrl(String newUrl) {
		this.newUrl = newUrl;
	}

	public Date getVersionDate() {
		return versionDate;
	}

	public void setVersionDate(Date versionDate) {
		this.versionDate = versionDate;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public boolean isChangedDate() {
		return changedDate;
	}

	public void setChangedDate(boolean changedDate) {
		this.changedDate = changedDate;
	}

	public String getNewDate() {
		return newDate;
	}

	public void setNewDate(String newDate) {
		this.newDate = newDate;
	}

	public boolean isChangedSummary() {
		return changedSummary;
	}

	public void setChangedSummary(boolean changedSummary) {
		this.changedSummary = changedSummary;
	}

	public String getNewSummary() {
		return newSummary;
	}

	public void setNewSummary(String newSummary) {
		this.newSummary = newSummary;
	}

}
