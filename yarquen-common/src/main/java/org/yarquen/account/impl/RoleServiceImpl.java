package org.yarquen.account.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.yarquen.account.Role;
import org.yarquen.account.RoleRepository;
import org.yarquen.account.RoleService;

/**
 * Default implementation to RoleService, based in Spring data mongodb backend.
 * 
 * @author maliq
 * @date 14/03/2013
 * @version $Id$
 * 
 */

@Service
public class RoleServiceImpl implements RoleService {

	public static enum Roles {
		BASIC, ADMIN;
	}

	public static enum Permission {
		PERM_READ_USER,PERM_WRITE_USER, PERM_WRITE_ARTICLE, PERM_WRITE_CATEGORY;
	}

	@Resource
	private RoleRepository roleRepository;

	@Override
	public Role findOne(String id) {
		return roleRepository.findOne(id);
	}

	@Override
	public Iterable<Role> findAll() {
		return roleRepository.findAll();
	}

	@Override
	public Role save(Role role) {
		return roleRepository.save(role);
	}
	

}
