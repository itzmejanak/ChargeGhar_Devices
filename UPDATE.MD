### 🧩 **Phase 1 — Admin Login & Access Control (Prepared for Phase 2)**

**Goal:**
Secure the website so only authenticated admins can access it, while keeping the structure ready to extend with API key–based access later.

---

### ⚙️ **Logic**

1. **Database**

   * Use PostgreSQL.
   * Create `admins` table → `id`, `username`, `password` (hashed), `created_at`.

2. **Authentication & Authorization**

   * Use **Spring Security** for:

     * Admin login (username + password)
     * Logout
     * Session handling (Spring-managed)
   * Only logged-in admins can access the website and admin pages.
   * Non-logged-in users are redirected to the login page.

3. **Admin Management**

   * Logged-in admins can:

     * View all admins
     * Add new admins
   * Passwords stored securely using BCryptPasswordEncoder.

4. **Frontend**

   * Use JSP pages:

     * `login.jsp` — for login
     * `dashboard.jsp` — after login
     * `admins.jsp` — list of admins
     * `add_admin.jsp` — form to add new admin

5. **Scalability / Future Ready (for Phase 2)**

   * Structure the security config and controllers so:

     * Session-based access can coexist with upcoming API key–based access.
     * Web routes (UI) remain session-authenticated.
     * API routes can later use API key filters without code conflict.

---

✅ **End Result:**

* Website access restricted to logged-in admins.
* Admins can manage other admins.
* Secure, session-based authentication ready to integrate with **Phase 2 API key system** seamlessly.

---

### 🧩 **Phase 2 — Simplified API Key + Session Logic**

**Goal:**
Extend the Phase 1 admin login system by adding API key validation and secure session handling for UI-triggered API calls.

---

### ⚙️ **Logic**

1. **Database**

   * Use PostgreSQL.
   * Add `api_keys` table → `id`, `key`, `status`, `created_by`, `created_at`, `rotated_at`.

2. **Admin Functions**

   * Only logged-in admins (via Spring Security) can:

     * Generate new API keys
     * View existing keys
     * Rotate / deactivate keys

3. **API Key Validation**

   * Add a **filter/interceptor** that checks for `X-API-KEY` header.
   * Verify in DB → must be active to allow access.
   * Invalid or missing key → return `401 Unauthorized`.

4. **Session Handling**

   * All requests coming from the **web UI (JSP, AJAX, or internal APIs)** should rely on the **authenticated admin’s session**, not an API key.
   * Session management handled by Spring Security — if the session expires, redirect to login.
   * API key filter should **skip** validation for requests that already have a valid session.

5. **Access Scope**

   * For now, API keys have full API access (endpoint-level permissions deferred to later phase).

6. **Integration**

   * Keep Phase 1 admin login logic intact.
   * Add simple JSP page for API key management.
   * Maintain modular structure for easy scaling.

---

✅ **Result:**

* Web UI and admin actions are session-based (Spring Security).
* External API requests use API keys.
* Clean separation and secure handling for both.

---