package org.yarquen.web.account;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.yarquen.account.Account;
import org.yarquen.account.AccountRepository;
import org.yarquen.account.AccountService;

@Controller
@RequestMapping("/account")
public class AccountController {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AccountController.class);

	@Resource
	private AccountService accountService;
	@Resource
	private AccountRepository accountRepository;

	@RequestMapping(method = RequestMethod.POST)
	public String save(@Valid Account account, BindingResult result, Model model) {

		LOGGER.debug("saving user: {} \n {}", account, result);

		if (result.hasErrors()) {
			return "account/register";
		}

		accountService.register(account);
		model.addAttribute("message", "account created successfully");
		return "message";
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String update(@Valid Account account, BindingResult result,
			Model model) {

		LOGGER.debug("updating user: {} \n {}", account, result);

		if (result.hasErrors()) {
			return "account/edit";
		}

		accountService.register(account);
		model.addAttribute("message", "account updated successfully");
		return "message";
	}

	@RequestMapping("register.html")
	public String register(Account newAccount, Model model) {
		model.addAttribute("account", newAccount);
		return "account/register";
	}

	@RequestMapping("current.html")
	public String register(Model model) {
		Account userDetails = (Account) SecurityContextHolder.getContext()
				.getAuthentication().getDetails();
		LOGGER.debug("userDetail: {}", userDetails);
		Account account = accountRepository.findOne(userDetails.getId());
		model.addAttribute("account", account);
		return "account/show";
	}

	@RequestMapping(value = "/edit/{accountId}", method = RequestMethod.GET)
	public String edit(@PathVariable("accountId") String accountId, Model model) {
		LOGGER.debug("accountId to edit: {}", accountId);
		Account account = accountRepository.findOne(accountId);
		if (account != null) {
			model.addAttribute("account", account);
			return "account/edit";
		} else
			return "error";

	}
}