package org.yarquen.account.impl;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yarquen.account.Account;
import org.yarquen.account.AccountRepository;
import org.yarquen.account.AccountService;
import org.yarquen.account.PasswordUtils;
import org.yarquen.validation.BeanValidationException;
import org.yarquen.validation.ValidationUtils;

/**
 * Account service
 * 
 * @author Jorge Riquelme Santana
 * @date 10/01/2013
 * @version $Id$
 * 
 */
@Service
public class AccountServiceImpl implements AccountService {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AccountServiceImpl.class);

	@Resource
	private AccountRepository accountRepository;
	@Resource
	private Validator validator;

	public Account authenticate(String username, String password) {
		final String hashedPassword = PasswordUtils.getHashedPassword(password);
		LOGGER.debug("authenticating {} with passwd {}", username,
				hashedPassword);
		return accountRepository.findByUsernameAndPassword(username,
				hashedPassword);
	}

	public Account register(Account account) {
		LOGGER.info("registering account {}", account.getUsername());
		final Set<String> violations = validate(account);
		if (violations != null) {
			throw new BeanValidationException(account, violations);
		} else {
			final String hashedPassword = PasswordUtils
					.getHashedPassword(account.getPassword());
			account.setPassword(hashedPassword);
			return accountRepository.save(account);
		}
	}

	private Set<String> validate(Account bean) {
		Set<String> messages = new HashSet<String>();
		final Set<ConstraintViolation<Account>> violations = validator
				.validate(bean, Default.class);
		// validate bean have unique username and email
		Account similarAccount = accountRepository.findByUsername(bean
				.getUsername());
		if (similarAccount != null
				&& !similarAccount.getId().equals(bean.getId())) {
			messages.add("username: " + bean.getUsername() + " already exists");
		}
		similarAccount = accountRepository.findByEmail(bean.getEmail());
		if (similarAccount != null
				&& !similarAccount.getId().equals(bean.getId())) {
			messages.add("email: " + bean.getEmail() + " already exists");
		}
		messages.addAll(ValidationUtils.getConstraintsMessages(violations));
		return !messages.isEmpty() ? messages : null;
	}

	public Account updateSkills(Account account) {
		LOGGER.info("registering account {}", account.getUsername());
		final Set<String> violations = validate(account);
		if (violations != null) {
			throw new BeanValidationException(account, violations);
		} else {
			return accountRepository.save(account);
		}
	}

}
