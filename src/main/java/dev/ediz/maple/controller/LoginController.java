package dev.ediz.maple.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController implements ErrorController {

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/404-not-found")
    public String accessDenied() {
        return "404";
    }

    @GetMapping("/error")
    public String handleError() {
        return "404";
    }
}
