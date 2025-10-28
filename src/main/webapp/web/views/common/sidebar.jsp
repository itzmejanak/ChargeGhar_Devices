<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- Reusable Sidebar Component for JSP -->
<style>
    .app-sidebar {
        width: 72px;
        background: #1a1d21;
        display: flex;
        flex-direction: column;
        align-items: center;
        transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        z-index: 100;
        padding: 0;
        height: 100vh;
        border-right: 1px solid #2d3748;
    }

    .app-sidebar:hover {
        width: 260px;
    }

    .sidebar-brand {
        width: 48px;
        height: 48px;
        background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.8rem;
        cursor: pointer;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        margin: 16px 0;
        box-shadow: 0 4px 12px rgba(66, 153, 225, 0.3);
        flex-shrink: 0;
    }

    .sidebar-brand:hover {
        border-radius: 16px;
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(66, 153, 225, 0.4);
    }

    .sidebar-divider {
        width: 32px;
        height: 2px;
        background: #2d3748;
        border-radius: 2px;
        margin: 0 0 12px 0;
        transition: width 0.3s ease;
        flex-shrink: 0;
    }

    .app-sidebar:hover .sidebar-divider {
        width: 80%;
    }

    .sidebar-nav {
        flex: 1;
        width: 100%;
        overflow-y: auto;
        overflow-x: hidden;
        padding: 8px 12px;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
    }

    .sidebar-nav::-webkit-scrollbar {
        width: 4px;
    }

    .sidebar-nav::-webkit-scrollbar-track {
        background: transparent;
    }

    .sidebar-nav::-webkit-scrollbar-thumb {
        background: #2d3748;
        border-radius: 4px;
    }

    .nav-item {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 12px;
        color: #a0aec0;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        white-space: nowrap;
        overflow: hidden;
        background: transparent;
        position: relative;
        flex-shrink: 0;
        text-decoration: none;
    }

    .app-sidebar:hover .nav-item {
        width: 220px;
        padding: 0 16px 0 0;
    }

    .nav-item::before {
        content: '';
        position: absolute;
        left: 0;
        width: 0;
        height: 20px;
        background: #4299e1;
        border-radius: 0 4px 4px 0;
        transition: all 0.2s ease;
    }

    .nav-item:hover {
        background: #2d3748;
        color: #e2e8f0;
        border-radius: 12px;
    }

    .nav-item:hover::before {
        width: 4px;
    }

    .nav-item.active {
        background: #2d3748;
        color: #4299e1;
    }

    .nav-item.active::before {
        width: 4px;
    }

    .nav-icon {
        min-width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.4rem;
    }

    .nav-text {
        opacity: 0;
        font-weight: 500;
        font-size: 0.9rem;
        transition: opacity 0.3s ease 0.1s;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .app-sidebar:hover .nav-text {
        opacity: 1;
    }
</style>

<div class="app-sidebar">
    <div class="sidebar-brand">🚀</div>
    <div class="sidebar-divider"></div>

    <div class="sidebar-nav">
        <a href="<%=request.getContextPath()%>/test" class="nav-item">
            <span class="nav-icon">🔧</span>
            <span class="nav-text">API Test</span>
        </a>
        <a href="<%=request.getContextPath()%>/health" class="nav-item">
            <span class="nav-icon">💚</span>
            <span class="nav-text">Health Check</span>
        </a>
        <a href="<%=request.getContextPath()%>/index.html" class="nav-item">
            <span class="nav-icon">📊</span>
            <span class="nav-text">Dashboard</span>
        </a>
        <a href="<%=request.getContextPath()%>/listen.html" class="nav-item">
            <span class="nav-icon">📡</span>
            <span class="nav-text">MQTT Listener</span>
        </a>
        <a href="<%=request.getContextPath()%>/version.html" class="nav-item">
            <span class="nav-icon">🔄</span>
            <span class="nav-text">Version Manager</span>
        </a>
        <a href="<%=request.getContextPath()%>/show.html?deviceName=test" class="nav-item">
            <span class="nav-icon">🎮</span>
            <span class="nav-text">Device Control</span>
        </a>
        <a href="<%=request.getContextPath()%>/admin/panel" class="nav-item">
            <span class="nav-icon">👥</span>
            <span class="nav-text">Admin Management</span>
        </a>
        <a href="<%=request.getContextPath()%>/emqx/test/connection" class="nav-item">
            <span class="nav-icon">🧪</span>
            <span class="nav-text">Test EMQX API</span>
        </a>
    </div>
</div>

<script>
    // Function to set active nav item
    function setActiveNavItem(path) {
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active');
        });
        
        // Set active based on current page
        var currentPath = window.location.pathname;
        if (currentPath.endsWith('/') || currentPath.includes('index')) {
            // This is the main landing page, no specific active item
        }
    }
    
    // Set active on page load
    document.addEventListener('DOMContentLoaded', function() {
        setActiveNavItem(window.location.pathname);
    });
</script>