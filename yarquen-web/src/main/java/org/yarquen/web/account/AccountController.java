package org.yarquen.web.account;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.yarquen.account.Account;
import org.yarquen.account.AccountService;



@Controller
@RequestMapping("/account")
public class AccountController {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AccountController.class);

	@Resource
	private AccountService accountService;

	@RequestMapping(method = RequestMethod.POST)
	public String save(@Valid Account account,
			BindingResult result, Model model) {
		
		LOGGER.debug("saving user: {} \n {}",account,result);

		if (result.hasErrors()) {
			return "account/register";
		}

//		accountService.register(account);
		return "home";
	}
	
	@RequestMapping("register.html")
	public String register(Account newAccount,Model model) {
		model.addAttribute("account", newAccount);
		return "account/register";
	}
}