package org.yarquen.account;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;

/**
 * Password Change
 * 
 * @author Choon-ho Yoon
 * @date 14/03/2013
 * @version $Id$
 * 
 */
public class PasswordChange {

	@Id
	private String id;

	@NotNull
	private Account account;

	@NotNull
	private String token;

	@NotNull
	private Date requestDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

}
