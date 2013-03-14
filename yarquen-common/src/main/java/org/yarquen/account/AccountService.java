package org.yarquen.account;

/**
 * Account service
 * 
 * @author Jorge Riquelme Santana
 * @date 10/01/2013
 * @version $Id$
 * 
 */
public interface AccountService {
	Account register(Account account);

	Account updateSkills(Account account);

	Account authenticate(String username, String password);

	/**
	 * Sends a unique email for password reset process
	 * 
	 * @param email
	 *            Email of the user trying to reset his password
	 * @return true if the email existed or false if it didn't.
	 */
	boolean resetPasswordRequest(String email);

	/**
	 * Updates account with new password
	 * 
	 * @param account
	 *            account with new password
	 * @return 
	 */
	Account updatePassword(Account account);

}
