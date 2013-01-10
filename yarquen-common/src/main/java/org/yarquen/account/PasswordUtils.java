package org.yarquen.account;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

/**
 * Password utilities.
 * 
 * @author Jorge Riquelme Santana
 * @date 10/01/2013
 * @version $Id$
 * 
 */
public class PasswordUtils
{
	private PasswordUtils()
	{
	}

	/**
	 * Return a readable hashed (SHA-1) version of a clear password.
	 * 
	 * @param clearPassword
	 *            password to hash
	 * @return readables SHA-1 version of <code>clearPassword</code>
	 */
	public static String getHashedPassword(String clearPassword)
	{
		MessageDigest md = null;
		try
		{
			md = MessageDigest.getInstance("SHA-1");
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException("no SHA-1? ¬¬", e);
		}
		final byte[] passwdBytes = md.digest(clearPassword.getBytes());
		return new String(Hex.encodeHex(passwdBytes));
	}
}