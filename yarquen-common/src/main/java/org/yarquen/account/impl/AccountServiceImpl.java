package org.yarquen.account.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
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
	private MailSender mailSender;
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

	@Override
	public boolean resetPasswordRequest(String email) {
		LOGGER.info("sending email with steps to reset password to: [{}]",
				email);
		final Account account = accountRepository.findByEmail(email);
		LOGGER.debug("account: {}", account);
		if (account != null) {
			final SimpleMailMessage message = new SimpleMailMessage();
			// Testing
			message.setFrom("choonho.yoon.b@gmail.com");
			message.setTo(email);
			message.setSubject("Yarquen Account Recovery");
			message.setText(getMessageWithUrl(account));

			try {
				mailSender.send(message);
				LOGGER.debug("email sent successfully to {}", email);
				return true;
			} catch (MailException e) {
				LOGGER.error("error sending mail", e);
				return false;
			}
		}
		LOGGER.debug("email [{}] not registered", email);
		return false;
	}

	/**
	 * Generates a message with information to reset an account password
	 * 
	 * @param account
	 *            Account trying to reset his password
	 * @return Message formatted for an email
	 */
	private String getMessageWithUrl(Account account) {
		LOGGER.debug("generating mail message for account: [{}]",
				account.getUsername());
		final StringBuilder mailMessage = new StringBuilder();
		mailMessage.append("To initiate the password reset process for your ");
		mailMessage.append(account.getEmail());
		mailMessage.append(" Yarquen account, click the link below: \n");
		mailMessage.append(getUniqueUrlPasswordReset(account.getUsername()));
		mailMessage
				.append("\nIf clicking the link above doesn't work, please copy and paste the URL in a new browser window instead.");
		return mailMessage.toString();
	}

	/**
	 * Generates an URL encrypted with username and an expiration date
	 * 
	 * @param username
	 *            Username of the user trying to reset password
	 * @return URL encrypted
	 */
	private String getUniqueUrlPasswordReset(String username) {
		LOGGER.debug("generating url for account with username: [{}]", username);
		final Date now = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
				.getTime();

		// TODO finish later
		return now.toString();
	}

	@Override
	public Account updatePassword(Account account) {
		LOGGER.info("updating account {}", account.getUsername());
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

}
