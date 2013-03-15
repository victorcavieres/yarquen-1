package org.yarquen.account;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.yarquen.account.impl.RoleServiceImpl.Permission;

@Document (collection="roles")
public class Role {
	@Id
	private String id;
	private List<Permission> permission;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<Permission> getPermission() {
		return permission;
	}
	public void setPermission(List<Permission> permission) {
		this.permission = permission;
	}
	
}
