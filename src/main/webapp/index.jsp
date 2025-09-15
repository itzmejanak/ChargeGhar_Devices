<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html>

    <head>
        <meta charset="UTF-8">
        <title>IoT Demo - EMQX Migration</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                margin: 40px;
                background: #f5f5f5;
            }

            .container {
                max-width: 800px;
                margin: 0 auto;
                background: white;
                padding: 30px;
                border-radius: 8px;
                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            }

            h1 {
                color: #2c3e50;
                text-align: center;
            }

            .status {
                background: #e8f5e8;
                padding: 15px;
                border-radius: 5px;
                margin: 20px 0;
            }

            .nav-links {
                display: flex;
                gap: 15px;
                justify-content: center;
                margin: 30px 0;
            }

            .nav-links a {
                padding: 10px 20px;
                background: #3498db;
                color: white;
                text-decoration: none;
                border-radius: 5px;
            }

            .nav-links a:hover {
                background: #2980b9;
            }

            .info {
                background: #f8f9fa;
                padding: 15px;
                border-left: 4px solid #3498db;
                margin: 20px 0;
            }
        </style>
    </head>

    <body>
        <div class="container">
            <h1>🎉 IoT Demo Application</h1>
            <div class="status">
                <h3>✅ EMQX Migration Complete!</h3>
                <p>Successfully migrated from Alibaba Cloud to EMQX Cloud</p>
            </div>

            <div class="info">
                <h4>Application Features:</h4>
                <ul>
                    <li>Real-time MQTT messaging with EMQX Cloud</li>
                    <li>Device management and monitoring</li>
                    <li>Power bank rental system</li>
                    <li>Version management</li>
                </ul>
            </div>

            <div class="nav-links">
                <a href="test">Test API</a>
                <a href="health">Health Check</a>
                <a href="index.html">Dashboard</a>
                <a href="listen.html">MQTT Listener</a>
                <a href="version.html">Version Manager</a>
            </div>

            <div class="info">
                <h4>API Endpoints:</h4>
                <ul>
                    <li><a href="test">Test Endpoint</a> - Verify deployment is working</li>
                    <li><a href="health">Health Check</a> - Application status</li>
                    <li><a href="index.html">Main Dashboard</a> - Device overview and status</li>
                    <li><a href="listen.html">MQTT Listener</a> - Real-time message monitoring</li>
                    <li><a href="version.html">Version Manager</a> - App version control</li>
                    <li><a href="show.html?deviceName=test">Device Control</a> - Device interaction</li>
                </ul>
            </div>

            <div style="text-align: center; margin-top: 30px; color: #7f8c8d;">
                <p>IoT Demo Application - Powered by EMQX Cloud</p>
            </div>
        </div>
    </body>

    </html>