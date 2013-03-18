package org.yarquen.web.account;

import java.beans.PropertyEditorSupport;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.yarquen.account.Account;
import org.yarquen.account.AccountRepository;
import org.yarquen.account.AccountService;
import org.yarquen.account.PasswordChange;
import org.yarquen.account.PasswordChangeRepository;
import org.yarquen.account.Role;
import org.yarquen.account.RoleService;
import org.yarquen.category.CategoryBranch;
import org.yarquen.category.CategoryService;
import org.yarquen.validation.BeanValidationException;
import org.yarquen.web.enricher.CategoryTreeBuilder;
import org.yarquen.account.Role;
import org.yarquen.account.RoleService;

@Controller
@RequestMapping("/account")
public class AccountController {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AccountController.class);

	private static final int EXPIRATION_MINUTES = 15;

	@Resource
	private AccountService accountService;
	@Resource
	private RoleService roleService;
	@Resource
	private AccountRepository accountRepository;
	@Resource
	private PasswordChangeRepository passwordChangeRepository;
	@Resource
	private CategoryTreeBuilder categoryTreeBuilder;

	@Resource
	private CategoryService categoryService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(CategoryBranch.class,
				new PropertyEditorSupport() {
					@Override
					public void setAsText(String branch) {
						try {
							LOGGER.trace(
									"converting {} to a CategoryBranch object",
									branch);
							final CategoryBranch categoryBranch = new CategoryBranch();
							final StringTokenizer tokenizer = new StringTokenizer(
									branch, ".");
							while (tokenizer.hasMoreTokens()) {
								final String code = tokenizer.nextToken();
								categoryBranch.addSubCategory(code, null);
							}
							// fill names
							categoryService
									.completeCategoryBranchNodeNames(categoryBranch);
							setValue(categoryBranch);
						} catch (RuntimeException e) {
							LOGGER.error(":(", e);
							throw e;
						}
					}
				});
	}
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String list(Model model) {
		LOGGER.debug("retrieval all account");
		Iterable<Account> accountList = accountRepository.findAll();
		model.addAttribute("accountList", accountList);
		return "account/list";

	}

	@RequestMapping(method = RequestMethod.POST)
	public String save(@Valid Account account, BindingResult result, Model model) {

		LOGGER.debug("saving user: {} \n {}", account, result);

		if (result.hasErrors()) {
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/register";
		}

		try {
			accountService.register(account);
		} catch (BeanValidationException e) {
			ObjectError error = new ObjectError("account", e.getMessage());
			result.addError(error);
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/register";
		}

		model.addAttribute("message", "account created successfully");
		return "message";
	}

	@RequestMapping(value = "/forgotPassword", params = "email", method = RequestMethod.GET)
	public String forgotPassword(
			@RequestParam(value = "email", required = true) String email,
			Model model) {
		LOGGER.debug("sending email to: [{}]", email);
		accountService.resetPasswordRequest(email);
		model.addAttribute("message",
				"Verification email sent successfully. Check your inbox.");
		return "message";
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String update(@Valid Account account, BindingResult result,
			Model model) {

		LOGGER.debug("updating user: {} \n {}", account, result);

		if (result.hasErrors()) {
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/edit";
		}

		try {
			Account accountWithSkill = accountRepository.findOne(account
					.getId());
			account.setSkills(accountWithSkill.getSkills());
			accountService.register(account);
		} catch (BeanValidationException e) {
			ObjectError error = new ObjectError("account", e.getMessage());
			result.addError(error);
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/edit";
		}
		model.addAttribute("message", "account updated successfully");
		return "message";
	}

	@RequestMapping("register.html")
	public String register(Account newAccount, Model model) {
		model.addAttribute("account", newAccount);
		return "account/register";
	}

	@RequestMapping("forgotPassword.html")
	public String passwordChangeRequest() {
		return "account/forgotPassword";
	}

	@RequestMapping("current.html")
	public String showCurrent(Model model) {
		Account userDetails = (Account) SecurityContextHolder.getContext()
				.getAuthentication().getDetails();
		LOGGER.debug("userDetail: {}", userDetails);
		Account account = accountRepository.findOne(userDetails.getId());
		model.addAttribute("account", account);
		return "account/show";
	}
	
	@RequestMapping("/show/{accountId}")
	public String showAccount(@PathVariable("accountId") String accountId,Model model) {
		Account account = accountRepository.findOne(accountId);
		LOGGER.debug("userDetail: {}", accountId);
		model.addAttribute("account", account);
		return "account/show";
	}

	@RequestMapping(value = "/edit/{accountId}", method = RequestMethod.GET)
	public String edit(@PathVariable("accountId") String accountId, Model model) {
		LOGGER.debug("accountId to edit: {}", accountId);
		Account account = accountRepository.findOne(accountId);
		
		if (account != null) {
			Iterable<Role> roles = roleService.findAll();
			model.addAttribute("roles",roles);
			model.addAttribute("account", account);
			return "account/edit";
		} else
			return "error";
	}

	@RequestMapping(value = "/passwordReset/{token}", method = RequestMethod.GET)
	public String passwordReset(@PathVariable("token") String token, Model model) {
		LOGGER.debug("password reset request from token: {}", token);
		final PasswordChange passwordChange = passwordChangeRepository
				.findByToken(token);

		// Expired Time for unique link request 15 minutes
		final Calendar expired = Calendar.getInstance(TimeZone
				.getTimeZone("UTC"));
		expired.add(Calendar.MINUTE, -EXPIRATION_MINUTES);
		final Date expiredDate = expired.getTime();
		LOGGER.debug("request date: {} - expiration date: {}",
				passwordChange.getRequestDate(), expiredDate);
		if (passwordChange != null
				&& passwordChange.getRequestDate().after(expiredDate)) {
			model.addAttribute("account", passwordChange.getAccount());

			return "account/passwordChange";
		} else {
			LOGGER.error("password change link expired");
			model.addAttribute("message",
					"the link for your password change has expired.");
			return "message";
		}
	}

	@RequestMapping(value = "/passwordChange", method = RequestMethod.POST)
	public String passwordChange(@Valid Account account, BindingResult result,
			Model model) {
		LOGGER.debug("changing password for account: {}", account.getUsername());
		try {
			accountService.updatePassword(account);
		} catch (BeanValidationException e) {
			ObjectError error = new ObjectError("account", e.getMessage());
			result.addError(error);
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/passwordChange";
		}
		model.addAttribute("message",
				"your password has been updated, please login.");
		return "message";

	}

	@RequestMapping(value = "/setupSkills/{accountId}", method = RequestMethod.GET)
	public String setupSkills(@PathVariable("accountId") String accountId,
			Model model) {
		LOGGER.debug("setuping skills for accountId {}", accountId);
		Account account = accountRepository.findOne(accountId);
		if (account != null) {
			// categories
			final List<Map<String, Object>> categoryTree = categoryTreeBuilder
					.buildTree();
			model.addAttribute("categories", categoryTree);
			model.addAttribute("account", account);
			return "account/editSkills";
		} else
			return "error";

	}

	@RequestMapping(value = "/skills/", method = RequestMethod.POST)
	public String updateSkills(@Valid Account account, BindingResult result,
			Model model) {
		LOGGER.debug("skills for accountId {} : {}", account.getId(),
				account.getSkills());
		final List<Map<String, Object>> categoryTree = categoryTreeBuilder
				.buildTree();
		model.addAttribute("categories", categoryTree);
		model.addAttribute("account", account);
		if (result.hasErrors()) {
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/editSkills";
		}
		try {
			Account accountWithoutSkill = accountRepository.findOne(account
					.getId());
			accountWithoutSkill.setSkills(account.getSkills());
			accountService.updateSkills(accountWithoutSkill);
		} catch (BeanValidationException e) {
			ObjectError error = new ObjectError("account", e.getMessage());
			result.addError(error);
			LOGGER.trace("errors!: {}", result.getAllErrors());
			return "account/editSkills";
		}
		model.addAttribute("account", account);
		return "account/show";

	}
}
