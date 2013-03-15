package org.yarquen.account;

/**
 * Service to manage Roles
 * @author maliq
 * @date 14/03/2013
 * @version $Id$
 *
 */
public interface RoleService {

	Role findOne(String id);

	Iterable<Role> findAll();

	Role save(Role role);

}
