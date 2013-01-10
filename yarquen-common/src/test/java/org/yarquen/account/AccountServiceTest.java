package org.yarquen.account;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * AccountService test
 * 
 * @author Jorge Riquelme Santana
 * @date 10/01/2013
 * @version $Id$
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/context.xml", "/account-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AccountServiceTest
{
	private static final String EMAIL = "jriquelme@totex.cl";
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AccountServiceTest.class);
	private static final String PASSWD = "123456";

	private Account account;
	@Resource
	private AccountRepository accountRepository;
	@Resource
	private AccountService accountService;

	@Before
	public void setup()
	{
		account = new Account();
		account.setEmail(EMAIL);
		account.setAdditionalLastName("Santana");
		account.setFamilyName("Riquelme");
		account.setFirstName("Jorge");
		account.setPassword(PASSWD);
		account.setUsername(EMAIL);
	}

	@Test
	public void t1Save()
	{
		final Account savedAccount = accountService.register(account);
		Assert.assertNotNull(savedAccount);
		LOGGER.info("account saved with id {}", savedAccount.getId());
	}

	@Test
	public void t2Authenticate()
	{
		final Account acc = accountService.authenticate(EMAIL, PASSWD);
		Assert.assertNotNull(acc);
		Assert.assertEquals(EMAIL, acc.getUsername());
	}

	@Test
	public void t3Remove()
	{
		final Account acc = accountService.authenticate(EMAIL, PASSWD);
		Assert.assertNotNull(acc);
		accountRepository.delete(acc);

		Assert.assertNull(accountService.authenticate(EMAIL, PASSWD));
	}
}
