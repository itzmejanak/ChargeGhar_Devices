<!DOCTYPE html>
<html lang="en">

<head>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/web/resource/css/index.css">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IoT Dashboard - EMQX</title>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
        
</head>

<body>
    <jsp:include page="web/views/common/sidebar.jsp" />

    <div class="main-content">
        <div class="top-bar">
            <span class="top-bar-icon">�</span>
            <span class="top-bar-title">IoT Dashboard</span>
            <div class="status-badge">
                <div class="status-indicator"></div>
                <span>Online</span>
            </div>
        </div>

        <div class="content-area">
            <div class="cards-grid">
                <a href="test">
                    <div class="card">
                        <div class="card-header">
                            <span class="card-icon">🔧</span>
                            <span class="card-title">API Test</span>
                            <span class="card-badge">Live</span>
                        </div>
                        <p class="card-description">Verify deployment and test API endpoints</p>
                    </div>
                </a>

                <a href="health">
                    <div class="card">
                        <div class="card-header">
                            <span class="card-icon">💚</span>
                            <span class="card-title">Health Check</span>
                            <span class="card-badge">Live</span>
                        </div>
                        <p class="card-description">Monitor system status and performance</p>
                    </div>
                </a>

                <a href="index.html">
                    <div class="card">
                        <div class="card-header">
                            <span class="card-icon">📊</span>
                            <span class="card-title">Dashboard</span>
                        </div>
                        <p class="card-description">Device overview and real-time metrics</p>
                    </div>
                </a>

                <a href="listen.html">
                    <div class="card">
                        <div class="card-header">
                            <span class="card-icon">📡</span>
                            <span class="card-title">MQTT Listener</span>
                        </div>
                        <p class="card-description">Monitor live message streams</p>
                    </div>
                </a>

                <a href="version.html">
                    <div class="card">
                        <div class="card-header">
                            <span class="card-icon">🔄</span>
                            <span class="card-title">Version Manager</span>
                        </div>
                        <p class="card-description">Control app versions and updates</p>
                    </div>
                </a>

                <a href="show.html?deviceName=test">
                    <div class="card">
                        <div class="card-header">
                            <span class="card-icon">🎮</span>
                            <span class="card-title">Device Control</span>
                        </div>
                        <p class="card-description">Interact with connected devices</p>
                    </div>
                </a>

                <a href="admin/panel">
                    <div class="card">
                        <div class="card-header">
                            <span class="card-icon">👥</span>
                            <span class="card-title">Admin Management</span>
                            <span class="card-badge secure">Secure</span>
                        </div>
                        <p class="card-description">Manage admin users and system settings</p>
                    </div>
                </a>
            </div>
        </div>
    </div>
</body>

</html>