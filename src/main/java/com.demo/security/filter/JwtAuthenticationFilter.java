package com.demo.security.filter;

import com.demo.security.JwtUtil;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT Authentication Filter
 * Intercepts all requests and validates JWT tokens
 */
public class JwtAuthenticationFilter implements Filter {
    private JwtUtil jwtUtil;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Get Spring beans from context
        ServletContext servletContext = filterConfig.getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        
        if (webApplicationContext != null) {
            this.jwtUtil = webApplicationContext.getBean(JwtUtil.class);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());
        
        // Check if this is a public URL (no authentication required)
        if (isPublicUrl(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check if this is a hardware/IoT API (no authentication required)
        if (isHardwareUrl(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check if this is a static resource
        if (isStaticResource(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Extract JWT token from request
        String token = extractToken(httpRequest);
        
        if (token != null && jwtUtil != null && jwtUtil.validateToken(token)) {
            // Token is valid - extract user information
            try {
                String username = jwtUtil.extractUsername(token);
                Integer userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);
                
                // Add user info to request attributes
                httpRequest.setAttribute("username", username);
                httpRequest.setAttribute("userId", userId);
                httpRequest.setAttribute("role", role);
                httpRequest.setAttribute("authenticated", true);
                
                // Continue with request
                chain.doFilter(request, response);
            } catch (Exception e) {
                redirectToLogin(httpRequest, httpResponse);
            }
        } else {
            // No token or invalid token - redirect to login
            redirectToLogin(httpRequest, httpResponse);
        }
    }

    /**
     * Extract JWT token from Authorization header, Cookie, or Query parameter
     */
    private String extractToken(HttpServletRequest request) {
        // 1. Check Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 2. Check Cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        // 3. Check Query parameter
        String tokenParam = request.getParameter("token");
        if (tokenParam != null) {
            return tokenParam;
        }
        
        return null;
    }

    /**
     * Check if URL is public (no authentication required)
     */
    private boolean isPublicUrl(String uri) {
        return uri.equals("/login") ||
               uri.equals("/login.jsp") ||
               uri.startsWith("/api/auth/login") ||
               uri.startsWith("/api/auth/logout") ||
               uri.equals("/health") ||
               uri.equals("/test");
    }

    /**
     * Check if URL is hardware/IoT API (no authentication required)
     */
    private boolean isHardwareUrl(String uri) {
        return uri.startsWith("/api/rentbox/") ||
               uri.startsWith("/api/iot/") ||
               uri.contains("/app/version/");
    }

    /**
     * Check if URL is static resource
     */
    private boolean isStaticResource(String uri) {
        return uri.startsWith("/web/") ||
               uri.endsWith(".css") ||
               uri.endsWith(".js") ||
               uri.endsWith(".png") ||
               uri.endsWith(".jpg") ||
               uri.endsWith(".jpeg") ||
               uri.endsWith(".gif") ||
               uri.endsWith(".ico") ||
               uri.endsWith(".svg") ||
               uri.endsWith(".woff") ||
               uri.endsWith(".woff2") ||
               uri.endsWith(".ttf") ||
               uri.endsWith(".eot");
    }

    /**
     * Redirect to login page
     */
    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contextPath = request.getContextPath();
        response.sendRedirect(contextPath + "/login");
    }

    @Override
    public void destroy() {
        // Cleanup resources if needed
    }
}
