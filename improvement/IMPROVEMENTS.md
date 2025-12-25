# ChargeGhar Devices - Improvement Roadmap

**Analysis Date:** December 25, 2025  
**Document Version:** 1.0

This document outlines identified issues and recommended improvements categorized by priority level (HIGH, MEDIUM, LOW).

---

## 📊 Summary Dashboard

| Priority | Count | Status |
|----------|-------|--------|
| 🔴 **HIGH** | 6 | Immediate action required |
| 🟡 **MEDIUM** | 8 | Plan for next sprint |
| 🟢 **LOW** | 7 | Backlog items |

---

## 🔴 HIGH Priority Issues

> Critical issues that pose security risks, system stability concerns, or significant technical debt.

---

### HIGH-001: Outdated Spring Framework (4.3.30)

| Attribute | Details |
|-----------|---------|
| **Category** | Security, Technical Debt |
| **Affected Files** | `pom.xml` |
| **Current State** | Spring MVC 4.3.30.RELEASE (EOL since December 2020) |
| **Risk** | No security patches, known vulnerabilities |

**Problem:**
```xml
<spring.version>4.3.30.RELEASE</spring.version>
```

Spring Framework 4.x reached end of life and no longer receives security updates. Multiple CVEs have been identified in this version.

**Recommendation:**
- Migrate to Spring Boot 3.x (Spring Framework 6.x) for latest features and security
- At minimum, upgrade to Spring Framework 5.3.x (still receiving security patches)

**Migration Path:**
1. Audit all Spring XML configurations
2. Convert to Java-based configuration
3. Update deprecated APIs
4. Test thoroughly with MQTT and Redis integrations

**Effort:** High (2-3 sprints)

---

### HIGH-002: Java 8 Runtime (End of Extended Support)

| Attribute | Details |
|-----------|---------|
| **Category** | Security, Compatibility |
| **Affected Files** | `pom.xml`, `Dockerfile` |
| **Current State** | Java 8 (extended support ending 2030, but missing modern features) |
| **Risk** | Missing security improvements, performance enhancements |

**Problem:**
```xml
<maven.compiler.source>1.8</maven.compiler.source>
<maven.compiler.target>1.8</maven.compiler.target>
```

**Recommendation:**
- Migrate to Java 17 LTS (supported until 2029)
- Benefits: Records, Pattern Matching, Improved GC, Better Performance

**Effort:** Medium (1-2 sprints)

---

### HIGH-003: Hardcoded Credentials in Properties Files

| Attribute | Details |
|-----------|---------|
| **Category** | Security |
| **Affected Files** | `config.properties`, `db-config.properties` |
| **Current State** | Passwords stored in plain text in version control |
| **Risk** | Credential exposure if repository is compromised |

**Problem:**
```properties
# config.properties
mqtt.password=5060
chargeghar.main.password=5060
chargeghar.main.signatureSecret=ChargeGhar-SystemSecret-TrustKey2025!

# db-config.properties
db.password=your-password
```

**Recommendation:**
1. Use environment variables for all secrets
2. Implement HashiCorp Vault or AWS Secrets Manager
3. Use `.env` files locally (excluded from Git)
4. Update Docker Compose to inject secrets

**Example Fix:**
```properties
# config.properties
mqtt.password=${MQTT_PASSWORD}
chargeghar.main.password=${DJANGO_API_PASSWORD}
chargeghar.main.signatureSecret=${SIGNATURE_SECRET}
```

**Effort:** Low (1-2 days)

---

### HIGH-004: Signature Validation Disabled for Device Compatibility

| Attribute | Details |
|-----------|---------|
| **Category** | Security |
| **Affected Files** | `ApiController.java` |
| **Current State** | Signature mismatch does not reject request |
| **Risk** | API endpoints can be called without valid signatures |

**Problem:**
```java
// ApiController.java - Line 276-283
protected void checkSign(Object valid, String sign, String deviceId) throws Exception {
    String expectedSign = SignUtils.getSign(valid);
    if (!expectedSign.equals(sign)) {
        String device = deviceId != null ? " from device: " + deviceId : "";
        System.out.println("⚠️  Invalid signature" + device + " (Expected: " + expectedSign + ", Received: " + sign + ")");
        // Don't throw exception - allow processing for device compatibility
        // throw new Exception("ERROR SIGN");  // <-- DISABLED!
    }
}
```

**Recommendation:**
1. Enable strict signature validation in production
2. Use feature flag to toggle validation
3. Allow device registration to update signing keys
4. Log and alert on signature mismatches

**Example Fix:**
```java
@Value("${security.signature.strict:true}")
private boolean strictSignatureValidation;

protected void checkSign(Object valid, String sign, String deviceId) throws Exception {
    String expectedSign = SignUtils.getSign(valid);
    if (!expectedSign.equals(sign)) {
        logger.warn("Invalid signature from device: {}", deviceId);
        if (strictSignatureValidation) {
            throw new SecurityException("Invalid request signature");
        }
    }
}
```

**Effort:** Low (1 day)

---

### HIGH-005: No Request Rate Limiting

| Attribute | Details |
|-----------|---------|
| **Category** | Security, Stability |
| **Affected Files** | All controllers |
| **Current State** | No rate limiting on any endpoint |
| **Risk** | DDoS attacks, brute force attacks on login, API abuse |

**Problem:**
- `/api/auth/login` - No protection against brute force
- `/api/rentbox/upload/data` - No throttling of device uploads
- `/check`, `/popup` - No protection against command flooding

**Recommendation:**
1. Implement rate limiting middleware
2. Use Redis-based token bucket algorithm
3. Different limits for different endpoint types

**Example Implementation:**
```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        String key = "rate_limit:" + clientIp;
        
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }
        
        if (count > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(429);
            return false;
        }
        return true;
    }
}
```

**Effort:** Medium (3-5 days)

---

### HIGH-006: Missing Input Validation and Sanitization

| Attribute | Details |
|-----------|---------|
| **Category** | Security |
| **Affected Files** | All controllers accepting user/device input |
| **Current State** | Limited validation on input parameters |
| **Risk** | SQL injection, XSS, command injection |

**Problem:**
```java
// IndexController.java - deviceCreate accepts unsanitized input
@RequestMapping("/device/create")
public HttpResult deviceCreate(HttpServletRequest request, HttpServletResponse response, 
                               @RequestParam String deviceName, 
                               @RequestParam(required = false) String imei) {
    // No validation on deviceName or imei
    Device device = controllerHelper.getOrCreateDevice(deviceName);
}
```

**Recommendation:**
1. Add Bean Validation (JSR-303) annotations
2. Validate device names match expected patterns
3. Sanitize all string inputs
4. Add request body size limits

**Example Fix:**
```java
public class DeviceCreateRequest {
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9]{8,20}$", message = "Invalid device name format")
    private String deviceName;
    
    @Pattern(regexp = "^[0-9]{15}$", message = "Invalid IMEI format")
    private String imei;
}
```

**Effort:** Medium (3-5 days)

---

## 🟡 MEDIUM Priority Issues

> Issues that should be addressed in upcoming sprints but don't pose immediate risks.

---

### MEDIUM-001: Deprecated Redis Client (Jedis 2.9.0)

| Attribute | Details |
|-----------|---------|
| **Category** | Technical Debt |
| **Affected Files** | `pom.xml`, Redis configuration |
| **Current State** | Jedis 2.9.0 |
| **Risk** | Missing features, potential bugs |

**Recommendation:**
- Upgrade to Jedis 4.x or migrate to Lettuce (reactive support)
- Lettuce is the default client for Spring Data Redis 2.x+

**Effort:** Medium (1 sprint)

---

### MEDIUM-002: Manual Thread.sleep() Polling Pattern

| Attribute | Details |
|-----------|---------|
| **Category** | Performance, Code Quality |
| **Affected Files** | `DeviceCommandUtils.java` |
| **Current State** | Blocking poll with Thread.sleep(500) |
| **Risk** | Thread blocking, inefficient resource usage |

**Problem:**
```java
// DeviceCommandUtils.java - Line 93-107
for (int i = 0; i < overSecond * 2; i++) {
    Thread.sleep(500);  // Blocks thread!
    Object data = operations.get();
    if (data != null && data instanceof byte[]) {
        bytes = (byte[]) data;
        break;
    }
}
```

**Recommendation:**
- Use `CountDownLatch` or `CompletableFuture`
- Implement Redis Pub/Sub for immediate notification
- Use Spring's async support with `@Async`

**Effort:** Medium (3-5 days)

---

### MEDIUM-003: No Centralized Exception Handling

| Attribute | Details |
|-----------|---------|
| **Category** | Code Quality |
| **Affected Files** | All controllers |
| **Current State** | Try-catch in each method |
| **Risk** | Inconsistent error responses, code duplication |

**Problem:**
```java
// Pattern repeated in every controller method
try {
    // Business logic
} catch (Exception e) {
    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    httpResult.setCode(response.getStatus());
    httpResult.setMsg(e.toString());  // Leaks stack trace
}
```

**Recommendation:**
Implement `@ControllerAdvice` global exception handler:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(SecurityException.class)
    @ResponseBody
    public HttpResult handleSecurityException(SecurityException e) {
        return HttpResult.error(401, "Authentication failed");
    }
    
    @ExceptionHandler(DeviceNotFoundException.class)
    @ResponseBody
    public HttpResult handleDeviceNotFound(DeviceNotFoundException e) {
        return HttpResult.error(404, "Device not found: " + e.getDeviceId());
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public HttpResult handleGenericException(Exception e) {
        logger.error("Unexpected error", e);
        return HttpResult.error(500, "Internal server error");
    }
}
```

**Effort:** Low (2-3 days)

---

### MEDIUM-004: No Logging Framework Integration

| Attribute | Details |
|-----------|---------|
| **Category** | Observability |
| **Affected Files** | All Java files |
| **Current State** | Uses `System.out.println` and `System.err.println` |
| **Risk** | No log levels, no structured logging, no log aggregation |

**Problem:**
```java
System.out.println("✅ MQTT Subscriber connected to: " + broker);
System.err.println("❌ MQTT Subscriber connection failed!");
```

**Recommendation:**
- Implement SLF4J with Logback
- Add structured JSON logging for production
- Configure log levels per environment

**Effort:** Low (2 days)

---

### MEDIUM-005: Missing Health Check Endpoints

| Attribute | Details |
|-----------|---------|
| **Category** | Observability, Operations |
| **Affected Files** | N/A (new feature) |
| **Current State** | No standardized health checks |
| **Risk** | Difficult to monitor application health |

**Recommendation:**
Implement comprehensive health endpoint:

```java
@RestController
@RequestMapping("/actuator")
public class HealthController {
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("checks", Map.of(
            "redis", checkRedis(),
            "mysql", checkMySQL(),
            "mqtt", checkMQTT(),
            "emqx", checkEMQX()
        ));
        return health;
    }
}
```

**Effort:** Low (1-2 days)

---

### MEDIUM-006: No Unit Test Coverage

| Attribute | Details |
|-----------|---------|
| **Category** | Code Quality |
| **Affected Files** | `src/test/` |
| **Current State** | Tests skipped in Maven configuration |
| **Risk** | Regressions, difficult refactoring |

**Problem:**
```xml
<!-- pom.xml -->
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <skipTests>true</skipTests>  <!-- Tests disabled! -->
    </configuration>
</plugin>
```

**Recommendation:**
1. Enable tests in build
2. Add unit tests for critical services
3. Add integration tests for API endpoints
4. Target 70%+ code coverage

**Effort:** High (ongoing)

---

### MEDIUM-007: Database Connection Pool Tuning

| Attribute | Details |
|-----------|---------|
| **Category** | Performance |
| **Affected Files** | `db-config.properties`, `spring-code.xml` |
| **Current State** | Basic DBCP2 configuration |
| **Risk** | Connection leaks, pool exhaustion under load |

**Recommendation:**
1. Add connection validation query
2. Configure proper timeouts
3. Add metrics collection
4. Consider HikariCP (better performance)

**Effort:** Low (1 day)

---

### MEDIUM-008: MQTT Connection Resilience

| Attribute | Details |
|-----------|---------|
| **Category** | Reliability |
| **Affected Files** | `MqttPublisher.java`, `MqttSubscriber.java` |
| **Current State** | Basic reconnection with Paho auto-reconnect |
| **Risk** | Message loss during reconnection |

**Recommendation:**
1. Implement message queuing during disconnection
2. Add circuit breaker pattern
3. Configure QoS appropriately for message delivery guarantees
4. Add monitoring for connection status

**Effort:** Medium (1 sprint)

---

## 🟢 LOW Priority Issues

> Nice-to-have improvements for long-term maintenance.

---

### LOW-001: Spring Data Redis Version Mismatch

| Attribute | Details |
|-----------|---------|
| **Category** | Technical Debt |
| **Affected Files** | `pom.xml` |
| **Current State** | spring-data-redis 1.8.16 with Spring 4.3.30 |

**Recommendation:**
- Align versions with Spring upgrade
- Consider Spring Data Redis 3.x

**Effort:** Included in HIGH-001

---

### LOW-002: FreeMarker View Resolver Complexity

| Attribute | Details |
|-----------|---------|
| **Category** | Code Simplification |
| **Affected Files** | `spring-freemarker.xml`, JSP files |
| **Current State** | Mix of FreeMarker and JSP |

**Recommendation:**
- Consolidate to single view technology
- Consider JSP-free approach with REST + SPA frontend

**Effort:** Low (backlog)

---

### LOW-003: API Documentation Missing

| Attribute | Details |
|-----------|---------|
| **Category** | Documentation |
| **Affected Files** | All controllers |
| **Current State** | No Swagger/OpenAPI documentation |

**Recommendation:**
- Add Springfox or SpringDoc for Swagger UI
- Document all endpoints with examples

**Effort:** Low (2-3 days)

---

### LOW-004: Docker Image Optimization

| Attribute | Details |
|-----------|---------|
| **Category** | DevOps |
| **Affected Files** | `Dockerfile` |
| **Current State** | Single-stage build |

**Recommendation:**
- Implement multi-stage build
- Use distroless or Alpine base image
- Reduce image size

**Effort:** Low (1 day)

---

### LOW-005: Configuration Externalization

| Attribute | Details |
|-----------|---------|
| **Category** | Operations |
| **Affected Files** | Properties files |
| **Current State** | Properties files bundled in WAR |

**Recommendation:**
- Support external configuration directory
- Implement Spring Cloud Config (if using microservices)

**Effort:** Low (1-2 days)

---

### LOW-006: Code Style Consistency

| Attribute | Details |
|-----------|---------|
| **Category** | Code Quality |
| **Affected Files** | All Java files |
| **Current State** | Inconsistent formatting, mixed Chinese/English comments |

**Recommendation:**
- Add EditorConfig
- Configure Checkstyle
- Standardize on English for comments

**Effort:** Low (ongoing)

---

### LOW-007: Dependency Vulnerability Scanning

| Attribute | Details |
|-----------|---------|
| **Category** | Security |
| **Affected Files** | `pom.xml` |
| **Current State** | No automated vulnerability scanning |

**Recommendation:**
- Add OWASP Dependency-Check plugin
- Integrate with CI/CD pipeline
- Set up automated alerts for new CVEs

**Effort:** Low (1 day)

---

## 📅 Recommended Implementation Roadmap

### Phase 1: Security Hardening (Sprint 1-2)
- [ ] HIGH-003: Environment variables for secrets
- [ ] HIGH-004: Enable signature validation
- [ ] HIGH-005: Implement rate limiting
- [ ] HIGH-006: Add input validation

### Phase 2: Observability (Sprint 3)
- [ ] MEDIUM-003: Global exception handling
- [ ] MEDIUM-004: Logging framework
- [ ] MEDIUM-005: Health check endpoints
- [ ] LOW-007: Dependency vulnerability scanning

### Phase 3: Technical Debt (Sprint 4-6)
- [ ] HIGH-001: Spring Framework upgrade
- [ ] HIGH-002: Java 17 migration
- [ ] MEDIUM-001: Redis client upgrade
- [ ] MEDIUM-002: Async command handling

### Phase 4: Quality & Documentation (Sprint 7+)
- [ ] MEDIUM-006: Unit test coverage
- [ ] LOW-003: API documentation
- [ ] LOW-006: Code style standardization

---

## 📈 Metrics to Track

| Metric | Current | Target |
|--------|---------|--------|
| Security vulnerabilities | Unknown | 0 Critical, 0 High |
| Test coverage | ~0% | >70% |
| Build time | Unknown | <3 min |
| Docker image size | Unknown | <200MB |
| API response time (p95) | Unknown | <200ms |
| MQTT reconnection rate | Unknown | <1/day |

---

## ✅ Completion Checklist

Use this checklist to track progress:

```markdown
- [ ] Phase 1: Security Hardening
  - [ ] HIGH-003 completed
  - [ ] HIGH-004 completed
  - [ ] HIGH-005 completed
  - [ ] HIGH-006 completed
- [ ] Phase 2: Observability
  - [ ] MEDIUM-003 completed
  - [ ] MEDIUM-004 completed
  - [ ] MEDIUM-005 completed
- [ ] Phase 3: Technical Debt
  - [ ] HIGH-001 completed
  - [ ] HIGH-002 completed
- [ ] Phase 4: Quality
  - [ ] MEDIUM-006 completed
```

---

*Last Updated: December 25, 2025*
