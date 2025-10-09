package com.demo.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class AuthController {

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        
        if (error != null) {
            model.addAttribute("errorMsg", "Invalid username or password!");
        }
        
        if (logout != null) {
            model.addAttribute("msg", "You have been logged out successfully.");
        }
        
        return "/web/views/auth/login";
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard(Model model, HttpServletRequest request) {
        // Get current authenticated user
        String username = request.getUserPrincipal().getName();
        model.addAttribute("username", username);
        return "/web/views/admin/dashboard";
    }
}