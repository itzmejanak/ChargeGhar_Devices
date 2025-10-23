<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IoT Dashboard - EMQX</title>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #36393f;
            height: 100vh;
            display: flex;
            color: #dcddde;
            overflow: hidden;
        }

        .sidebar {
            width: 72px;
            background: #202225;
            display: flex;
            flex-direction: column;
            align-items: center;
            transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            z-index: 100;
            padding: 0;
            height: 100vh;
        }

        .sidebar:hover {
            width: 240px;
        }

        .sidebar:hover {
            width: 260px;
        }

        .server-icon {
            width: 48px;
            height: 48px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 2rem;
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            margin: 12px 0;
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
            flex-shrink: 0;
        }

        .server-icon:hover {
            border-radius: 20px;
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
        }

        .divider {
            width: 32px;
            height: 2px;
            background: #2f3136;
            border-radius: 2px;
            margin: 0 0 8px 0;
            transition: width 0.3s ease;
            flex-shrink: 0;
        }

        .sidebar:hover .divider {
            width: 80%;
        }

        .channels {
            flex: 1;
            width: 100%;
            overflow-y: auto;
            overflow-x: hidden;
            padding: 8px 10px;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 2px;
        }

        .channels::-webkit-scrollbar {
            width: 4px;
        }

        .channels::-webkit-scrollbar-track {
            background: transparent;
        }

        .channels::-webkit-scrollbar-thumb {
            background: #2f3136;
            border-radius: 4px;
        }

        .channel {
            width: 48px;
            height: 48px;
            border-radius: 24px;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 12px;
            color: #b9bbbe;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            white-space: nowrap;
            overflow: hidden;
            background: transparent;
            position: relative;
            flex-shrink: 0;
        }

        .sidebar:hover .channel {
            width: 220px;
            padding: 0 12px 0 0;
        }

        .channel::before {
            content: '';
            position: absolute;
            left: 0;
            width: 0;
            height: 20px;
            background: #fff;
            border-radius: 0 4px 4px 0;
            transition: all 0.2s ease;
        }

        .channel:hover {
            background: #36393f;
            color: #dcddde;
            border-radius: 24px;
        }

        .channel:hover::before {
            width: 8px;
        }

        .channel.active {
            background: transparent;
            color: #fff;
        }

        .channel.active::before {
            width: 8px;
        }

        .channel-icon {
            min-width: 48px;
            height: 48px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
        }

        .channel-name {
            opacity: 0;
            font-weight: 500;
            font-size: 0.95rem;
            transition: opacity 0.3s ease 0.1s;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .sidebar:hover .channel-name {
            opacity: 1;
        }

        .main-content {
            flex: 1;
            display: flex;
            flex-direction: column;
            background: #36393f;
        }

        .top-bar {
            height: 56px;
            background: #2f3136;
            border-bottom: 1px solid #202225;
            display: flex;
            align-items: center;
            padding: 0 20px;
            gap: 12px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
        }

        .top-bar-icon {
            font-size: 1.5rem;
        }

        .top-bar-title {
            font-weight: 600;
            color: #fff;
            font-size: 1.1rem;
        }

        .status-badge {
            margin-left: auto;
            display: flex;
            align-items: center;
            gap: 8px;
            background: #2f3136;
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 0.85rem;
            font-weight: 500;
        }

        .status-indicator {
            width: 10px;
            height: 10px;
            background: #3ba55d;
            border-radius: 50%;
            animation: pulse 2s infinite;
        }

        .content-area {
            flex: 1;
            overflow-y: auto;
            padding: 32px;
            background: #36393f;
        }

        .content-area::-webkit-scrollbar {
            width: 8px;
        }

        .content-area::-webkit-scrollbar-track {
            background: #2f3136;
        }

        .content-area::-webkit-scrollbar-thumb {
            background: #202225;
            border-radius: 4px;
        }

        .cards-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
            gap: 24px;
            max-width: 100%;
        }

        .card {
            background: #2f3136;
            border-radius: 12px;
            padding: 28px;
            border: 1px solid #202225;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            cursor: pointer;
            position: relative;
            overflow: hidden;
            height: 100%;
        }

        .card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 4px;
            height: 0;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            transition: height 0.3s ease;
        }

        .card:hover {
            background: #32353b;
            transform: translateY(-4px);
            box-shadow: 0 12px 24px rgba(0, 0, 0, 0.3);
            border-color: #404249;
        }

        .card:hover::before {
            height: 100%;
        }

        .card-header {
            display: flex;
            align-items: center;
            gap: 14px;
            margin-bottom: 14px;
        }

        .card-icon {
            font-size: 2rem;
        }

        .card-title {
            font-size: 1.15rem;
            font-weight: 600;
            color: #fff;
        }

        .card-badge {
            margin-left: auto;
            background: #3ba55d;
            color: white;
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 0.7rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .card-description {
            color: #b9bbbe;
            font-size: 0.92rem;
            line-height: 1.6;
        }

        a {
            text-decoration: none;
            color: inherit;
        }

        @keyframes pulse {
            0%, 100% {
                opacity: 1;
                transform: scale(1);
            }
            50% {
                opacity: 0.6;
                transform: scale(0.95);
            }
        }

        @media (max-width: 768px) {
            .cards-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <div class="sidebar">
        <div class="server-icon">🚀</div>
        <div class="divider"></div>
        
        <div class="channels">
            <a href="test">
                <div class="channel">
                    <span class="channel-icon">🔧</span>
                    <span class="channel-name">API Test</span>
                </div>
            </a>
            <a href="health">
                <div class="channel">
                    <span class="channel-icon">💚</span>
                    <span class="channel-name">Health Check</span>
                </div>
            </a>
            <a href="index.html">
                <div class="channel active">
                    <span class="channel-icon">📊</span>
                    <span class="channel-name">Dashboard</span>
                </div>
            </a>
            <a href="listen.html">
                <div class="channel">
                    <span class="channel-icon">📡</span>
                    <span class="channel-name">MQTT Listener</span>
                </div>
            </a>
            <a href="version.html">
                <div class="channel">
                    <span class="channel-icon">🔄</span>
                    <span class="channel-name">Version Manager</span>
                </div>
            </a>
            <a href="show.html?deviceName=test">
                <div class="channel">
                    <span class="channel-icon">🎮</span>
                    <span class="channel-name">Device Control</span>
                </div>
            </a>
        </div>
    </div>

    <div class="main-content">
        <div class="top-bar">
            <span class="top-bar-icon">📊</span>
            <span class="top-bar-title">Dashboard</span>
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
            </div>
        </div>
    </div>
</body>
</html>