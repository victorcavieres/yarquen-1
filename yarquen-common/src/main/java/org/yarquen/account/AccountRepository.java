package org.yarquen.account;

import org.springframework.data.repository.CrudRepository;

/**
 * Account repository
 * 
 * @author Jorge Riquelme Santana
 * @date 10/01/2013
 * @version $Id$
 * 
 */
public interface AccountRepository extends CrudRepository<Account, Long>
{
	Account findByUsernameAndPassword(String username, String password);
}
