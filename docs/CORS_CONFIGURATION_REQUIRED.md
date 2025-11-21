# ⚠️ CRITICAL: CORS Configuration Required for Django Integration

## Problem

Your Java API at `api.chargeghar.com` will **BLOCK** requests from Django at `main.chargeghar.com` due to:

1. ✅ **Authentication:** JWT required (already handled by DeviceAPIService)
2. ❌ **CORS:** Not configured (will block browser requests)

---

## Solution: Add CORS Support to Java Spring API

### Option 1: Using Spring Filter (Recommended)

#### Step 1: Create `CorsFilter.java`

Create file: `src/main/java/com.demo/security/filter/CorsFilter.java`

```java
package com.demo.security.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CORS Filter to allow requests from Django frontend
 */
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Get origin from request
        String origin = httpRequest.getHeader("Origin");
        
        // List of allowed origins
        String[] allowedOrigins = {
            "https://main.chargeghar.com",
            "http://main.chargeghar.com",
            "http://localhost:8000",  // For Django local development
            "http://localhost:3000",  // For React local development (if any)
            "http://127.0.0.1:8000"
        };
        
        // Check if origin is allowed
        boolean isAllowed = false;
        if (origin != null) {
            for (String allowedOrigin : allowedOrigins) {
                if (origin.equals(allowedOrigin)) {
                    isAllowed = true;
                    break;
                }
            }
        }
        
        // Set CORS headers if origin is allowed
        if (isAllowed) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
            httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            httpResponse.setHeader("Access-Control-Allow-Headers", 
                "Content-Type, Authorization, X-Requested-With, Accept");
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Max-Age", "3600");
        }
        
        // Handle preflight OPTIONS request
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        // Continue with the request
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}
```

#### Step 2: Register Filter in `web.xml`

Edit: `src/main/webapp/WEB-INF/web.xml`

Add this **BEFORE** the `JwtAuthenticationFilter`:

```xml
<!-- CORS Filter - Must be FIRST -->
<filter>
    <filter-name>corsFilter</filter-name>
    <filter-class>com.demo.security.filter.CorsFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>corsFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

**IMPORTANT:** Filter order matters! CORS must come BEFORE JWT:

```xml
<!-- CORRECT ORDER -->
1. corsFilter (CORS headers)
2. encodingFilter (UTF-8)
3. jwtAuthenticationFilter (JWT validation)
```

---

### Option 2: Using Spring Configuration Class

Create file: `src/main/java/com.demo/common/WebMvcConfig.java`

```java
package com.demo.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // Apply to all endpoints
                .allowedOrigins(
                    "https://main.chargeghar.com",
                    "http://main.chargeghar.com",
                    "http://localhost:8000",
                    "http://localhost:3000"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "X-Requested-With", "Accept")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

---

## Testing CORS Configuration

### Test 1: Check CORS Headers

```bash
# From terminal
curl -X OPTIONS https://api.chargeghar.com/check \
  -H "Origin: https://main.chargeghar.com" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Authorization, Content-Type" \
  -v
```

**Expected Response:**
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: https://main.chargeghar.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With, Accept
Access-Control-Allow-Credentials: true
```

### Test 2: Real Request with Auth

```bash
curl -X POST https://api.chargeghar.com/check?deviceName=CG001 \
  -H "Origin: https://main.chargeghar.com" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -v
```

**Expected Response:**
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: https://main.chargeghar.com
Content-Type: application/json

{
  "code": 1,
  "msg": "success",
  "data": {...},
  "time": 1699123456789
}
```

---

## Django DeviceAPIService - No Changes Needed ✅

Your `DeviceAPIService` already handles:
- ✅ JWT authentication with auto-login
- ✅ `Authorization: Bearer <token>` header
- ✅ Auto-retry on 401
- ✅ Proper request/response handling

**Once you add CORS to Java API, Django service will work immediately!**

---

## Production Deployment Checklist

### Java API (`api.chargeghar.com`)

- [ ] Add `CorsFilter.java` with allowed origins
- [ ] Register filter in `web.xml` **BEFORE** JwtAuthenticationFilter
- [ ] Rebuild: `mvn clean package`
- [ ] Redeploy WAR file to Tomcat/server
- [ ] Test CORS headers with curl
- [ ] Test OPTIONS preflight request

### Django API (`main.chargeghar.com`)

- [ ] Update `settings.py` with production Java API URL
- [ ] Set `DEVICE_API['BASE_URL'] = 'https://api.chargeghar.com'`
- [ ] Configure auth credentials in `.env`
- [ ] Test `DeviceAPIService` from Django shell
- [ ] Test from frontend if using browser requests

---

## Security Notes

### Allowed Origins
- ✅ **Production:** Only `https://main.chargeghar.com`
- ✅ **Development:** Add `http://localhost:8000` for testing
- ❌ **Never use:** `Access-Control-Allow-Origin: *` (security risk)

### Authentication
- ✅ JWT tokens required for all device endpoints
- ✅ Use `Authorization: Bearer <token>` header (not cookies for CORS)
- ✅ Token expires in 24 hours (auto-refresh on 401)

### HTTPS
- ✅ Use HTTPS in production: `https://api.chargeghar.com`
- ✅ Use HTTPS in production: `https://main.chargeghar.com`
- ❌ Mixed content (HTTP + HTTPS) will be blocked by browsers

---

## Quick Fix Summary

**Current State:**
- ❌ Django → Java API: **BLOCKED** (no CORS)
- ✅ JWT Auth: Already implemented in Django service

**Action Required:**
1. Add `CorsFilter.java` to Java API
2. Register in `web.xml` before JWT filter
3. Rebuild and redeploy Java API
4. Test with curl
5. Test Django service

**Estimated Time:** 15-30 minutes

---

## Need Help?

Test CORS configuration:
```bash
# Check if CORS is working
curl -X OPTIONS https://api.chargeghar.com/send \
  -H "Origin: https://main.chargeghar.com" \
  -v | grep "Access-Control"
```

If you see `Access-Control-Allow-Origin` header, CORS is configured correctly! ✅
