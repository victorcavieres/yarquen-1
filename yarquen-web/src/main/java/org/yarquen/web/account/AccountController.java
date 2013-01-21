package org.yarquen.web.account;

import javax.annotation.Resource;

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

	@Resource
	private AccountService accountService;

	@RequestMapping(method = RequestMethod.POST)
	public String save(Account account,
			BindingResult result, Model model) {

		if (result.hasErrors()) {
			return "account/register";
		}

		accountService.register(account);
		return "home";
	}
	
	@RequestMapping("register.html")
	public String register(Model model) {
		Account newAccount=new Account();
		model.addAttribute("account", newAccount);
		return "account/register";
	}
}