package dev.ediz.maple.controller;


import dev.ediz.maple.event.OnRegistrationCompleteEvent;
import dev.ediz.maple.exception.UserAlreadyExistException;
import dev.ediz.maple.model.Account;
import dev.ediz.maple.model.Authority;
import dev.ediz.maple.model.VerificationToken;
import dev.ediz.maple.repository.VerificationTokenRepository;
import dev.ediz.maple.service.AccountService;
import dev.ediz.maple.validator.OnCreate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Controller
public class RegisterController {

    @Autowired
    private AccountService accountService;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    private MessageSource messages;


    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        Account account = new Account();
        model.addAttribute("account", account);
        return "register";
    }


    @PostMapping("/register")
    public String registerNewUser(@ModelAttribute("account") @Validated(OnCreate.class) Account account, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        try {
            account.setEnabled(false);
            Account registered = accountService.save(account);
            redirectAttributes.addFlashAttribute("message", "Verify your email to login");

            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), appUrl));

            return "redirect:/";
        } catch (UserAlreadyExistException ex) {
            model.addAttribute("message", ex.getMessage());
            return "register";
        } catch (RuntimeException ex) {
            model.addAttribute("message", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/registrationConfirm")
    public String confirmRegistration
            (WebRequest request, Model model, @RequestParam("token") String token) {

        Locale locale = request.getLocale();

        VerificationToken verificationToken = accountService.getVerificationToken(token);
        if (verificationToken == null) {
            String message = messages.getMessage("auth.message.invalidToken", null, locale);
            model.addAttribute("message", message);
            return "redirect:/badUser.html";
        }

        Account account = verificationToken.getAccount();
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            String messageValue = messages.getMessage("auth.message.expired", null, locale);
            model.addAttribute("message", messageValue);
            return "redirect:/badUser.html?lang=" + locale.getLanguage();
        }

        account.setEnabled(true);
        accountService.save(account);
        return "redirect:/login.html";
    }



}