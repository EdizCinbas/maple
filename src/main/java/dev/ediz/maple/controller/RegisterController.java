package dev.ediz.maple.controller;

import dev.ediz.maple.exception.UserAlreadyExistException;
import dev.ediz.maple.model.Account;
import dev.ediz.maple.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        model.addAttribute("account", new Account());
        return "register";
    }

    @PostMapping("/register")
    public String registerNewUser(@ModelAttribute("account") @Valid Account account, Model model, RedirectAttributes redirectAttributes) {
        try {
            account.setEnabled(true);
            accountService.save(account);
            redirectAttributes.addFlashAttribute("message", "Account created. You can now log in.");
            return "redirect:/login";
        } catch (UserAlreadyExistException ex) {
            model.addAttribute("message", ex.getMessage());
            return "register";
        } catch (RuntimeException ex) {
            model.addAttribute("message", ex.getMessage());
            return "register";
        }
    }

}