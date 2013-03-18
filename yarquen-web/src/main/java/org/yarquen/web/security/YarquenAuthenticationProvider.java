package org.yarquen.web.security;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.yarquen.account.Account;
import org.yarquen.account.AccountService;
import org.yarquen.account.Role;
import org.yarquen.account.RoleService;
import org.yarquen.account.impl.RoleServiceImpl.Permission;

/**
 * Authentication provider for yarquen based in account service
 * 
 * @author maliq
 * 
 */
@Service
public class YarquenAuthenticationProvider implements AuthenticationProvider {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(YarquenAuthenticationProvider.class);

	@Resource
	private AccountService accountService;
	@Resource
	private RoleService roleService;
	

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		
		UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) authentication;
		String username = String.valueOf(auth.getPrincipal());
		String password = String.valueOf(auth.getCredentials());
		Account account = accountService.authenticate(username, password);
		if (account != null) {
			List<YarquenGrant> grants=new ArrayList<YarquenAuthenticationProvider.YarquenGrant>();
			for(String roleId:account.getRoleId()){
				Role role=roleService.findOne(roleId);
				grants.addAll(parse(role));
			}
			LOGGER.info("Authemticated user {} with grants:{}",account.getFirstName() + " " + account.getFamilyName(),grants);
			auth = new UsernamePasswordAuthenticationToken(
					account.getFirstName() + " " + account.getFamilyName(),
					authentication.getCredentials(), grants);
			auth.setDetails(account);
			return auth;
		} else
			return null;

	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
	
	private List<YarquenGrant> parse(Role role){
		List<YarquenGrant> grants=new ArrayList<YarquenAuthenticationProvider.YarquenGrant>();
		for(Permission permission:role.getPermission()){
			grants.add(new YarquenGrant(permission));
		}
		return grants;
	}
	
	
	class YarquenGrant implements GrantedAuthority{
		private static final long serialVersionUID = 1L;
		private Permission permission;

		public YarquenGrant(Permission permission) {
			this.permission=permission;
		}

		@Override
		public String getAuthority() {
			return permission.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((permission == null) ? 0 : permission.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			YarquenGrant other = (YarquenGrant) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (permission != other.permission)
				return false;
			return true;
		}

		private YarquenAuthenticationProvider getOuterType() {
			return YarquenAuthenticationProvider.this;
		}
		
		
		
	}

}
