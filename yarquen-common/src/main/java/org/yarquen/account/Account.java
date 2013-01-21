package org.yarquen.account;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;

/**
 * Account
 * 
 * @author Jorge Riquelme Santana
 * @date 10/01/2013
 * @version $Id$
 * 
 */
public class Account
{
	@Size(min = 1)
	private String additionalLastName;

	@NotNull
	@Size(min = 5)
	private String email;

	@NotNull
	@Size(min = 1)
	private String familyName;

	@NotNull
	@Size(min = 1)
	private String firstName;

	@Id
	private String id;

	@Size(min = 1)
	private String middleName;

	@NotNull
	@Size(min = 6)
	private String password;

	@NotNull
	@Size(min = 5, max = 50)
	private String username;

	public String getAdditionalLastName()
	{
		return additionalLastName;
	}

	public String getEmail()
	{
		return email;
	}

	public String getFamilyName()
	{
		return familyName;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public String getId()
	{
		return id;
	}

	public String getMiddleName()
	{
		return middleName;
	}

	public String getPassword()
	{
		return password;
	}

	public String getUsername()
	{
		return username;
	}

	public void setAdditionalLastName(String additionalLastName)
	{
		this.additionalLastName = additionalLastName;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public void setFamilyName(String familyName)
	{
		this.familyName = familyName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public void setMiddleName(String middleName)
	{
		this.middleName = middleName;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	@Override
	public String toString() {
		return "Account [additionalLastName=" + additionalLastName + ", email="
				+ email + ", familyName=" + familyName + ", firstName="
				+ firstName + ", id=" + id + ", middleName=" + middleName
				+ ", username=" + username + "]";
	}
	
	
}
