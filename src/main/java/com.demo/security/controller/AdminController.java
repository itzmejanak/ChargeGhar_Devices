package com.demo.security.controller;

import com.demo.security.model.Admin;
import com.demo.security.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listAdmins(Model model, HttpServletRequest request) {
        List<Admin> admins = adminService.findAllAdmins();
        String username = request.getUserPrincipal().getName();
        model.addAttribute("admins", admins);
        model.addAttribute("username", username);
        return "/web/views/admin/admins";
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addAdminForm(Model model, HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        model.addAttribute("username", username);
        return "/web/views/admin/add_admin";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addAdmin(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          RedirectAttributes redirectAttributes) {
        
        // Validation
        if (username == null || username.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Username is required!");
            return "redirect:/admin/add";
        }
        
        if (password == null || password.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMsg", "Password must be at least 6 characters!");
            return "redirect:/admin/add";
        }
        
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Passwords do not match!");
            return "redirect:/admin/add";
        }
        
        // Check if username already exists
        if (adminService.findByUsername(username.trim()) != null) {
            redirectAttributes.addFlashAttribute("errorMsg", "Username already exists!");
            return "redirect:/admin/add";
        }
        
        try {
            adminService.createAdmin(username.trim(), password);
            redirectAttributes.addFlashAttribute("successMsg", "Admin created successfully!");
            return "redirect:/admin/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to create admin: " + e.getMessage());
            return "redirect:/admin/add";
        }
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String editAdminForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        Admin admin = adminService.findById(id);
        if (admin == null) {
            model.addAttribute("errorMsg", "Admin not found!");
            return "redirect:/admin/list";
        }
        
        String username = request.getUserPrincipal().getName();
        model.addAttribute("admin", admin);
        model.addAttribute("username", username);
        return "/web/views/admin/edit_admin";
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.POST)
    public String editAdmin(@PathVariable Long id,
                           @RequestParam String username,
                           @RequestParam(required = false) String password,
                           @RequestParam(required = false) String confirmPassword,
                           RedirectAttributes redirectAttributes) {
        
        Admin existingAdmin = adminService.findById(id);
        if (existingAdmin == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "Admin not found!");
            return "redirect:/admin/list";
        }
        
        // Validation
        if (username == null || username.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Username is required!");
            return "redirect:/admin/edit/" + id;
        }
        
        // Check if username is available (excluding current admin)
        if (!adminService.isUsernameAvailable(username.trim(), id)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Username already exists!");
            return "redirect:/admin/edit/" + id;
        }
        
        try {
            // Update username if changed
            if (!existingAdmin.getUsername().equals(username.trim())) {
                adminService.updateAdminUsername(id, username.trim());
            }
            
            // Update password if provided
            if (password != null && !password.trim().isEmpty()) {
                if (password.length() < 6) {
                    redirectAttributes.addFlashAttribute("errorMsg", "Password must be at least 6 characters!");
                    return "redirect:/admin/edit/" + id;
                }
                
                if (!password.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("errorMsg", "Passwords do not match!");
                    return "redirect:/admin/edit/" + id;
                }
                
                adminService.updateAdminPassword(id, password);
            }
            
            redirectAttributes.addFlashAttribute("successMsg", "Admin updated successfully!");
            return "redirect:/admin/list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to update admin: " + e.getMessage());
            return "redirect:/admin/edit/" + id;
        }
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public String deleteAdmin(@PathVariable Long id, 
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        
        Admin adminToDelete = adminService.findById(id);
        if (adminToDelete == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "Admin not found!");
            return "redirect:/admin/list";
        }
        
        // Prevent deleting yourself
        String currentUsername = request.getUserPrincipal().getName();
        if (adminToDelete.getUsername().equals(currentUsername)) {
            redirectAttributes.addFlashAttribute("errorMsg", "You cannot delete your own account!");
            return "redirect:/admin/list";
        }
        
        // Prevent deleting the last admin
        if (adminService.getAdminCount() <= 1) {
            redirectAttributes.addFlashAttribute("errorMsg", "Cannot delete the last admin!");
            return "redirect:/admin/list";
        }
        
        try {
            adminService.deleteAdmin(id);
            redirectAttributes.addFlashAttribute("successMsg", "Admin deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Failed to delete admin: " + e.getMessage());
        }
        
        return "redirect:/admin/list";
    }
}