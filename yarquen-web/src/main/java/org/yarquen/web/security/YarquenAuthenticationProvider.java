package org.yarquen.web.security;

import javax.annotation.Resource;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.yarquen.account.Account;
import org.yarquen.account.AccountService;

/**
 * Authentication provider for yarquen based in account service
 * 
 * @author maliq
 * 
 */
@Service
public class YarquenAuthenticationProvider implements AuthenticationProvider {

	@Resource
	private AccountService accountService;

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) authentication;
		String username = String.valueOf(auth.getPrincipal());
		String password = String.valueOf(auth.getCredentials());
		Account account = accountService.authenticate(username, password);
		if (account != null) {
			auth = new UsernamePasswordAuthenticationToken(
					account.getFirstName() + " " + account.getFamilyName(),
					authentication.getCredentials(), null);
			auth.setDetails(account);
			return auth;
		} else
			return null;

	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
