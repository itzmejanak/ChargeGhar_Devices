# 📋 ChargeGhar Integration - Documentation Summary

## 🎯 Overview
This documentation set provides a complete implementation guide for integrating the Java-based IoT device management system with the Django-based user application (main.chargeghar.com).

---

## 📚 Documentation Files

### 1. **con_plan.md** - System Architecture & Strategy
**Purpose**: High-level system design and integration strategy

**Key Sections**:
- System architecture diagrams
- Security & authentication flow
- Data flow scenarios (device upload, powerbank return)
- Database mapping (device data → Django models)
- Integration points and hooks
- Configuration management
- Implementation phases (6 phases)
- Error handling strategy
- Success metrics & monitoring

**Use When**: 
- Understanding overall system design
- Planning implementation approach
- Reviewing security architecture
- Defining project scope

---

### 2. **con_req_res.md** - API Request/Response Specifications
**Purpose**: Detailed API endpoint documentation and data formats

**Key Sections**:
- Authentication APIs (login, refresh token)
- Station data sync endpoint (full sync & return events)
- Request/response examples (JSON format)
- Field mapping tables (device → JSON → Django)
- Status code mappings (0x01 → "OCCUPIED", etc.)
- Signature generation algorithm
- Retry strategy & error handling
- Testing checklists

**Use When**:
- Implementing API calls
- Debugging data format issues
- Understanding signature validation
- Writing integration tests

---

### 3. **con_code_java.md** - Java Implementation Code
**Purpose**: Complete Java code for IoT system side

**Key Components**:
1. **SignChargeGharMain.java** - HMAC signature utilities
2. **AuthTokenManager.java** - JWT token management singleton
3. **ChargeGharConnector.java** - Main integration class (440 lines)
4. **ApiController.java modifications** - Integration hooks
5. **config.properties updates** - Configuration entries
6. **Maven dependencies** - Required libraries
7. **Testing code** - Unit tests for signature validation

**File Structure**:
```
src/main/java/com.demo/
├── connector/ChargeGharConnector.java [NEW]
├── security/
│   ├── AuthTokenManager.java [NEW]
│   └── SignChargeGharMain.java [NEW]
└── controller/ApiController.java [MODIFY]
```

**Use When**:
- Implementing Java connector
- Setting up authentication
- Adding integration hooks to existing code
- Testing signature generation

---

### 4. **con_code_python.md** - Django/Python Implementation Code
**Purpose**: Complete Django code for user application side

**Key Components**:
1. **sign_chargeghar_main.py** - Signature validation utilities
2. **IoTSignatureValidationMiddleware** - Request validation middleware
3. **StationDataInternalView** - Internal API endpoint
4. **StationSyncService** - Business logic for data sync
5. **URL configuration** - Route setup
6. **Django settings** - Configuration & middleware
7. **Testing code** - Unit & integration tests

**File Structure**:
```
api/
├── stations/
│   ├── views/internal_views.py [NEW]
│   ├── services/
│   │   ├── station_sync_service.py [NEW]
│   │   └── utils/sign_chargeghar_main.py [NEW]
│   └── urls.py [MODIFY]
└── main_app/
    ├── middleware.py [NEW]
    └── settings.py [MODIFY]
```

**Use When**:
- Implementing Django endpoints
- Setting up signature validation
- Implementing data synchronization logic
- Writing API tests

---

## 🔄 Implementation Workflow

### Phase 1: Security Foundation (Day 1-2)
1. ✅ Read **con_plan.md** - Security Architecture section
2. ✅ Implement **SignChargeGharMain.java** from **con_code_java.md** §1
3. ✅ Implement **sign_chargeghar_main.py** from **con_code_python.md** §1
4. ✅ Test signature generation/validation (use test code from both files)
5. ✅ Verify both sides produce identical signatures

**Success Criteria**: Python and Java generate matching signatures for same input

---

### Phase 2: Authentication (Day 3)
1. ✅ Implement **AuthTokenManager.java** from **con_code_java.md** §2
2. ✅ Implement **connectChargeGharMain()** method from **con_code_java.md** §3
3. ✅ Create Django admin user (email: janak@powerbank.com)
4. ✅ Test login flow manually using curl (see **con_req_res.md** §1)
5. ✅ Verify token storage and retrieval

**Success Criteria**: Java successfully authenticates and stores JWT tokens

---

### Phase 3: Django Endpoints (Day 4-5)
1. ✅ Implement **IoTSignatureValidationMiddleware** from **con_code_python.md** §2
2. ✅ Implement **StationDataInternalView** from **con_code_python.md** §3
3. ✅ Implement **StationSyncService** from **con_code_python.md** §4
4. ✅ Add URL routes from **con_code_python.md** §5
5. ✅ Update settings from **con_code_python.md** §6
6. ✅ Test with Postman/curl (see **con_req_res.md** §3)

**Success Criteria**: Endpoint returns 200 OK for valid request with signature

---

### Phase 4: Java Connector (Day 6-7)
1. ✅ Implement **ChargeGharConnector.java** from **con_code_java.md** §3
2. ✅ Add configuration properties from **con_code_java.md** §5
3. ✅ Add Maven dependencies from **con_code_java.md** §6
4. ✅ Update **ApiController.java** from **con_code_java.md** §4
5. ✅ Build and deploy to Docker
6. ✅ Test device upload triggers data sync

**Success Criteria**: Device upload successfully syncs to Django, logs show "Data sent successfully"

---

### Phase 5: Testing & Validation (Day 8-9)
1. ✅ Run unit tests (Java: **con_code_java.md** §7, Python: **con_code_python.md** §7)
2. ✅ Test full sync scenario (empty station → all slots occupied)
3. ✅ Test return event scenario (active rental → completed)
4. ✅ Test error scenarios (invalid signature, expired token, network failure)
5. ✅ Load test with multiple devices
6. ✅ Verify database consistency

**Success Criteria**: All tests pass, no data loss, 95%+ sync success rate

---

### Phase 6: Production Deployment (Day 10)
1. ✅ Deploy Java code to api.chargeghar.com
2. ✅ Deploy Django code to main.chargeghar.com
3. ✅ Update production config files (secure credentials)
4. ✅ Set up monitoring & alerting
5. ✅ Monitor first 24 hours of operation
6. ✅ Document any issues and resolutions

**Success Criteria**: System runs stable for 24 hours with <1% error rate

---

## 🔍 Quick Reference Guide

### Where to Find...

| What You Need | File | Section |
|---------------|------|---------|
| Overall architecture diagram | con_plan.md | §1 System Overview |
| Security flow diagram | con_plan.md | §2 Security Architecture |
| Login API request format | con_req_res.md | §1 Admin Login |
| Full sync request format | con_req_res.md | §3 Station Data Upload |
| Return event request format | con_req_res.md | §4 PowerBank Return Event |
| Signature algorithm | con_req_res.md | §5 Signature Generation |
| Java signature code | con_code_java.md | §1 Signature Utility Class |
| Java connector code | con_code_java.md | §3 Main Connector Class |
| Python signature code | con_code_python.md | §1 Signature Utility |
| Python sync service code | con_code_python.md | §4 Station Sync Service |
| Configuration properties | con_code_java.md | §5 / con_code_python.md | §6 |
| Test cases | con_code_java.md | §7 / con_code_python.md | §7 |
| Deployment steps | con_code_java.md | §9 / con_code_python.md | §8 |
| Error handling strategy | con_plan.md | §7 Error Handling Strategy |
| Monitoring guide | con_code_java.md | §10 / con_code_python.md | §9 |

---

## 🚨 Common Issues & Solutions

### Issue: Signature Validation Failed
**Symptoms**: 403 Forbidden, "Signature mismatch"

**Debug Steps**:
1. Check secret keys match on both sides (Java config.properties vs Django settings.py)
2. Print payload and timestamp on both sides - must be identical
3. Verify no extra whitespace or encoding issues
4. Check timestamp is within 5-minute window

**Files to Check**:
- Java: `SignChargeGharMain.java` generateSignature()
- Python: `sign_chargeghar_main.py` validate_signature()
- See **con_req_res.md** §5 for algorithm details

---

### Issue: Authentication Failed
**Symptoms**: 401 Unauthorized, "Invalid credentials"

**Debug Steps**:
1. Verify Django admin user exists and is active
2. Check email/password in Java config.properties
3. Test login manually with curl (see **con_req_res.md** §1)
4. Check Django logs for login attempts

**Files to Check**:
- Java: `ChargeGharConnector.java` connectChargeGharMain()
- Django: User model, authentication backend
- See **con_plan.md** §2 for authentication flow

---

### Issue: Data Not Syncing
**Symptoms**: No database updates, no errors in logs

**Debug Steps**:
1. Check Java logs for "SENDING DEVICE DATA" messages
2. Verify ChargeGharConnector is autowired in ApiController
3. Check network connectivity (api.chargeghar.com → main.chargeghar.com)
4. Test endpoint manually with curl

**Files to Check**:
- Java: `ApiController.java` integration hooks
- Python: `StationDataInternalView.post()`
- See **con_code_java.md** §4 for hook locations

---

### Issue: Duplicate Data / Data Conflicts
**Symptoms**: Multiple records for same station/powerbank

**Debug Steps**:
1. Check unique constraints in Django models
2. Verify get_or_create logic in StationSyncService
3. Check for race conditions (multiple uploads at same time)
4. Review logs for concurrent requests

**Files to Check**:
- Python: `StationSyncService._sync_station()`, `_sync_powerbanks()`
- See **con_code_python.md** §4 for sync logic

---

## 📊 Testing Checklist

### Unit Tests
- [ ] Java signature generation (consistent output)
- [ ] Python signature validation (accepts valid, rejects invalid)
- [ ] Token manager (store, retrieve, expiry check)
- [ ] Data transformation (Powerbank object → JSON)

### Integration Tests
- [ ] End-to-end authentication flow
- [ ] Full station sync (empty → occupied)
- [ ] Partial station sync (update existing)
- [ ] Return event processing
- [ ] Signature validation middleware
- [ ] Token refresh on 401

### Load Tests
- [ ] 10 devices uploading simultaneously
- [ ] 100 requests per minute
- [ ] Network timeout handling
- [ ] Database connection pool under load

### Security Tests
- [ ] Invalid signature rejected (403)
- [ ] Expired timestamp rejected (403)
- [ ] Missing headers rejected (403)
- [ ] SQL injection attempts
- [ ] XSS in JSON payload

---

## 📝 Configuration Checklist

### Java (api.chargeghar.com)
- [ ] `chargeghar.main.baseUrl` set to production URL
- [ ] `chargeghar.main.email` set to admin email
- [ ] `chargeghar.main.password` set to secure password
- [ ] `chargeghar.main.signatureSecret` set (32+ chars)
- [ ] Maven dependencies added
- [ ] Docker rebuild completed

### Django (main.chargeghar.com)
- [ ] `IOT_SYSTEM_SIGNATURE_SECRET` matches Java secret
- [ ] `IOT_SYSTEM_ALLOWED_IPS` includes api.chargeghar.com IP
- [ ] Admin user created (janak@powerbank.com)
- [ ] Middleware added to MIDDLEWARE list
- [ ] URL route added
- [ ] Logging configured

---

## 🎓 Learning Path

### For Backend Developer New to Project
1. Start with **con_plan.md** - Read §1-3 (architecture overview)
2. Review **con_req_res.md** - Understand API contracts
3. Read **con_code_java.md** §1-3 - Java implementation details
4. Read **con_code_python.md** §1-4 - Django implementation details
5. Study test cases in both code files

### For DevOps Engineer
1. Read **con_plan.md** §6 (configuration) & §9 (deployment)
2. Review **con_code_java.md** §5 (config properties) & §9 (deployment)
3. Review **con_code_python.md** §6 (settings) & §8 (deployment)
4. Set up monitoring as per §10 in both code files

### For QA Engineer
1. Read **con_req_res.md** - All sections (API specs)
2. Review **con_plan.md** §7 (error handling)
3. Study test cases in **con_code_java.md** §7 & **con_code_python.md** §7
4. Create test scenarios based on data flow in **con_plan.md** §4

---

## 🔗 External Dependencies

### Java Libraries
- Jackson (JSON processing) - v2.15.2
- Apache HttpClient - v4.5.14
- Apache Commons Codec - v1.15

### Python Libraries
- Django REST Framework (API views)
- PyJWT (token handling - already in Django)
- Standard library (hmac, hashlib, base64)

### Infrastructure
- EMQX Cloud (MQTT broker) - qd081a20.ala.dedicated.aws.emqxcloud.com
- Redis (state management) - localhost:6379
- PostgreSQL (Django database)
- Docker & Docker Compose

---

## 📞 Support & Contact

### File Issues
- Authentication problems → Check **con_plan.md** §2 + **con_req_res.md** §1
- Signature problems → Check **con_req_res.md** §5
- Data sync problems → Check **con_plan.md** §4 + respective code files
- Deployment problems → Check §9 (Java) or §8 (Python) in code files

### Code References
- All code is documented with inline comments
- Each file has logging statements for debugging
- Test cases provide usage examples

---

## ✅ Final Checklist Before Production

### Security
- [ ] Signature secret is strong (32+ characters) and secure
- [ ] Admin password is strong and not default
- [ ] HTTPS enabled on both endpoints
- [ ] IP whitelisting configured (optional)
- [ ] Sensitive data not logged

### Configuration
- [ ] All config files updated for production
- [ ] Secrets stored in environment variables (not committed)
- [ ] Database connections tested
- [ ] Redis connection tested
- [ ] EMQX connection tested

### Testing
- [ ] All unit tests pass
- [ ] Integration tests pass
- [ ] Manual end-to-end test completed
- [ ] Load testing completed (if required)
- [ ] Rollback plan documented

### Monitoring
- [ ] Logging configured and tested
- [ ] Log rotation enabled
- [ ] Monitoring dashboard set up
- [ ] Alerts configured for failures
- [ ] Health check endpoint tested

### Documentation
- [ ] All four markdown files reviewed
- [ ] Deployment steps documented
- [ ] Troubleshooting guide available
- [ ] Team trained on new integration
- [ ] Runbook created for on-call

---

## 🎯 Success Metrics (After 1 Week)

- ✅ 95%+ successful data syncs
- ✅ <5 second average sync latency
- ✅ 0 data loss incidents
- ✅ 100% signature validation success rate
- ✅ <1% authentication failures
- ✅ 99.9% endpoint uptime
- ✅ All device uploads trigger successful syncs

---

## 📄 Document Version History

- **v1.0** - 2025-10-27 - Initial complete documentation set
  - con_plan.md - System architecture and strategy
  - con_req_res.md - API specifications
  - con_code_java.md - Java implementation
  - con_code_python.md - Django implementation
  - README (this file) - Documentation guide

---

**Note**: This integration bridges two critical systems. Follow the implementation phases carefully, test thoroughly at each stage, and maintain clear communication between Java and Django development teams.

Good luck with your implementation! 🚀
