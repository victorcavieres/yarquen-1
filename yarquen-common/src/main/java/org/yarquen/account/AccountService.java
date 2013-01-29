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

}
