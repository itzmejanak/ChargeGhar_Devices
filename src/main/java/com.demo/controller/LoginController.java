package com.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class LoginController {
    
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public void loginPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Forward to login.jsp
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
}
