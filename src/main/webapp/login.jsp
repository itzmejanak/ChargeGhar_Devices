<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <title>Login - IOT Device Hub</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            height: 100vh;
            margin: 0;
            background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
            overflow: hidden;
        }

        .container {
            display: flex;
            width: 100%;
            height: 100vh;
            background: white;
            overflow: hidden;
        }

        .left-section {
            flex: 0 0 550px;
            padding: 40px 90px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            background: transparent;
            margin: auto;
        }

        .right-section {
            flex: 1;
            background: linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%);
            position: relative;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
            min-width: 0;
        }

        .right-section::before {
            content: '';
            position: absolute;
            width: 400px;
            height: 400px;
            background: rgba(255, 255, 255, 0.1);
            border-radius: 50%;
            top: -100px;
            right: -100px;
        }

        .right-section::after {
            content: '';
            position: absolute;
            width: 300px;
            height: 300px;
            background: rgba(255, 255, 255, 0.1);
            border-radius: 50%;
            bottom: -80px;
            left: -80px;
        }

        .image-container {
            position: absolute;
            width: 100%;
            height: 100%;
            z-index: 2;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .device-illustration {
            width: 100%;
            height: 100%;
            max-width: 600px;
            max-height: 600px;
        }

        .lightning-icon {
            position: absolute;
            width: 60px;
            height: 60px;
            background: white;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
            left: -30px;
            top: 50%;
            transform: translateY(-50%);
            z-index: 3;
            font-size: 30px;
        }

        h1 {
            font-size: 42px;
            font-weight: 700;
            margin-bottom: 10px;
            color: #1a1a1a;
        }

        .subtitle {
            color: #666;
            margin-bottom: 40px;
            font-size: 14px;
        }

        .message {
            display: none;
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
            animation: slideIn 0.3s ease;
        }

        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(-10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .error-message {
            background: #fee;
            color: #c33;
            border: 1px solid #fcc;
        }

        .success-message {
            background: #efe;
            color: #3c3;
            border: 1px solid #cfc;
        }

        .input-group {
            margin-bottom: 20px;
        }

        .input-wrapper {
            position: relative;
            display: flex;
            align-items: center;
            background: #f0f0ff;
            border-radius: 12px;
            padding: 16px 20px;
            transition: all 0.3s ease;
            border: 2px solid transparent;
        }

        .input-wrapper:focus-within {
            background: #e8e8ff;
            border-color: #7c3aed;
            box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.1);
        }

        .input-wrapper svg {
            width: 20px;
            height: 20px;
            margin-right: 12px;
            color: #666;
            flex-shrink: 0;
        }

        input {
            flex: 1;
            border: none;
            background: transparent;
            outline: none;
            font-size: 15px;
            color: #333;
        }

        input::placeholder {
            color: #999;
        }

        .login-btn {
            width: 100%;
            padding: 16px;
            background: #7c3aed;
            color: white;
            border: none;
            border-radius: 12px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            margin-top: 30px;
            transition: all 0.3s ease;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
        }

        .login-btn:hover:not(:disabled) {
            background: #6d28d9;
            transform: translateY(-2px);
            box-shadow: 0 10px 25px rgba(124, 58, 237, 0.3);
        }

        .login-btn:disabled {
            opacity: 0.7;
            cursor: not-allowed;
        }

        .loading-spinner {
            display: none;
            width: 16px;
            height: 16px;
            border: 2px solid rgba(255, 255, 255, 0.3);
            border-top-color: white;
            border-radius: 50%;
            animation: spin 0.8s linear infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        .footer-text {
            text-align: center;
            margin-top: 30px;
            color: #999;
            font-size: 13px;
        }

        @media (max-width: 968px) {
            .container {
                flex-direction: column;
            }

            .right-section {
                display: none;
            }

            .left-section {
                padding: 40px 30px;
            }

            h1 {
                font-size: 32px;
            }
        }

        @media (max-width: 480px) {
            .left-section {
                padding: 30px 20px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="left-section">
            <h1>LOGIN</h1>
            <p class="subtitle">IOT Device Hub - Secure Device Management Platform</p>
            
            <div id="errorMessage" class="message error-message"></div>
            <div id="successMessage" class="message success-message"></div>

            <form id="loginForm">
                <div class="input-group">
                    <div class="input-wrapper">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                            <circle cx="12" cy="7" r="4"></circle>
                        </svg>
                        <input type="text" id="username" name="username" placeholder="Username" required autocomplete="username">
                    </div>
                </div>

                <div class="input-group">
                    <div class="input-wrapper">
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                        </svg>
                        <input type="password" id="password" name="password" placeholder="Password" required autocomplete="current-password">
                    </div>
                </div>

                <button type="submit" class="login-btn" id="loginBtn">
                    <div class="loading-spinner" id="loadingSpinner"></div>
                    <span id="loginBtnText">Login Now</span>
                </button>
            </form>

            <div class="footer-text">
                Powered by IOT Device Management System
            </div>
        </div>

        <div class="right-section">
            <div class="lightning-icon">⚡</div>
            <div class="image-container">
                <svg class="device-illustration" viewBox="0 0 400 400" xmlns="http://www.w3.org/2000/svg">
                    <!-- Animated IoT Device Network -->
                    <defs>
                        <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" style="stop-color:#fff;stop-opacity:0.3" />
                            <stop offset="100%" style="stop-color:#fff;stop-opacity:0.1" />
                        </linearGradient>
                        <filter id="glow">
                            <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                            <feMerge>
                                <feMergeNode in="coloredBlur"/>
                                <feMergeNode in="SourceGraphic"/>
                            </feMerge>
                        </filter>
                    </defs>
                    
                    <!-- Background animated circles -->
                    <circle cx="200" cy="200" r="150" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="2">
                        <animate attributeName="r" values="150;170;150" dur="4s" repeatCount="indefinite"/>
                        <animate attributeName="opacity" values="0.1;0.3;0.1" dur="4s" repeatCount="indefinite"/>
                    </circle>
                    <circle cx="200" cy="200" r="120" fill="none" stroke="rgba(255,255,255,0.15)" stroke-width="2">
                        <animate attributeName="r" values="120;140;120" dur="3s" repeatCount="indefinite"/>
                        <animate attributeName="opacity" values="0.15;0.4;0.15" dur="3s" repeatCount="indefinite"/>
                    </circle>
                    
                    <!-- Central Hub with glow -->
                    <circle cx="200" cy="200" r="50" fill="rgba(255,255,255,0.25)" filter="url(#glow)">
                        <animate attributeName="r" values="50;55;50" dur="2s" repeatCount="indefinite"/>
                    </circle>
                    <circle cx="200" cy="200" r="40" fill="rgba(255,255,255,0.4)"/>
                    
                    <!-- Rocket in center -->
                    <g transform="translate(200, 200)">
                        <path d="M-15,-20 L0,-35 L15,-20 L10,-10 L10,0 L15,5 L5,10 L0,5 L-5,10 L-15,5 L-10,0 L-10,-10 Z" 
                              fill="#7c3aed" opacity="0.8"/>
                        <circle cx="0" cy="-15" r="3" fill="#fbbf24"/>
                        <path d="M-8,0 L-12,10 L-8,8 Z" fill="#f87171" opacity="0.6">
                            <animateTransform attributeName="transform" type="scale" 
                                values="1,1;1.2,1.3;1,1" dur="0.3s" repeatCount="indefinite"/>
                        </path>
                        <path d="M8,0 L12,10 L8,8 Z" fill="#f87171" opacity="0.6">
                            <animateTransform attributeName="transform" type="scale" 
                                values="1,1;1.2,1.3;1,1" dur="0.3s" repeatCount="indefinite"/>
                        </path>
                    </g>
                    
                    <!-- Device Nodes with Icons -->
                    
                    <!-- Top - Phone -->
                    <g transform="translate(200, 80)">
                        <circle r="30" fill="rgba(255,255,255,0.3)" filter="url(#glow)"/>
                        <circle r="22" fill="rgba(255,255,255,0.5)"/>
                        <rect x="-10" y="-15" width="20" height="28" rx="3" fill="#7c3aed" opacity="0.7"/>
                        <rect x="-8" y="-12" width="16" height="22" rx="1" fill="#fff" opacity="0.9"/>
                        <circle cx="0" cy="12" r="2" fill="#7c3aed" opacity="0.5"/>
                        <animate attributeName="opacity" values="0.8;1;0.8" dur="3s" repeatCount="indefinite"/>
                    </g>
                    <line x1="200" y1="110" x2="200" y2="150" stroke="rgba(255,255,255,0.5)" stroke-width="2" stroke-dasharray="5,5">
                        <animate attributeName="stroke-dashoffset" from="0" to="10" dur="1s" repeatCount="indefinite"/>
                    </line>
                    
                    <!-- Right - Light Bulb -->
                    <g transform="translate(320, 200)">
                        <circle r="30" fill="rgba(255,255,255,0.3)" filter="url(#glow)"/>
                        <circle r="22" fill="rgba(255,255,255,0.5)"/>
                        <circle cx="0" cy="-3" r="8" fill="#fbbf24" opacity="0.8">
                            <animate attributeName="opacity" values="0.6;1;0.6" dur="2s" repeatCount="indefinite"/>
                        </circle>
                        <path d="M-6,5 L-4,12 L4,12 L6,5" fill="#7c3aed" opacity="0.6"/>
                        <rect x="-2" y="12" width="4" height="3" fill="#7c3aed" opacity="0.5"/>
                        <line x1="-12" y1="-3" x2="-15" y2="-3" stroke="#fbbf24" stroke-width="2" opacity="0.6">
                            <animate attributeName="opacity" values="0.3;0.8;0.3" dur="2s" repeatCount="indefinite"/>
                        </line>
                        <line x1="12" y1="-3" x2="15" y2="-3" stroke="#fbbf24" stroke-width="2" opacity="0.6">
                            <animate attributeName="opacity" values="0.3;0.8;0.3" dur="2s" begin="0.5s" repeatCount="indefinite"/>
                        </line>
                    </g>
                    <line x1="270" y1="200" x2="250" y2="200" stroke="rgba(255,255,255,0.5)" stroke-width="2" stroke-dasharray="5,5">
                        <animate attributeName="stroke-dashoffset" from="0" to="10" dur="1s" repeatCount="indefinite"/>
                    </line>
                    
                    <!-- Bottom - Lock -->
                    <g transform="translate(200, 320)">
                        <circle r="30" fill="rgba(255,255,255,0.3)" filter="url(#glow)"/>
                        <circle r="22" fill="rgba(255,255,255,0.5)"/>
                        <rect x="-10" y="2" width="20" height="14" rx="2" fill="#7c3aed" opacity="0.7"/>
                        <path d="M-7,-8 Q-7,-13 -3,-13 L3,-13 Q7,-13 7,-8 L7,2 L-7,2 Z" 
                              fill="none" stroke="#7c3aed" stroke-width="3" opacity="0.7"/>
                        <circle cx="0" cy="9" r="2.5" fill="#fff" opacity="0.8"/>
                        <rect x="-1" y="11" width="2" height="4" fill="#fff" opacity="0.8"/>
                    </g>
                    <line x1="200" y1="290" x2="200" y2="250" stroke="rgba(255,255,255,0.5)" stroke-width="2" stroke-dasharray="5,5">
                        <animate attributeName="stroke-dashoffset" from="0" to="10" dur="1s" repeatCount="indefinite"/>
                    </line>
                    
                    <!-- Left - Antenna/Signal -->
                    <g transform="translate(80, 200)">
                        <circle r="30" fill="rgba(255,255,255,0.3)" filter="url(#glow)"/>
                        <circle r="22" fill="rgba(255,255,255,0.5)"/>
                        <rect x="-2" y="-8" width="4" height="20" rx="2" fill="#7c3aed" opacity="0.7"/>
                        <circle cx="0" cy="-12" r="4" fill="#7c3aed" opacity="0.7"/>
                        <path d="M-10,-8 Q-10,-12 -6,-12" fill="none" stroke="#7c3aed" stroke-width="2" opacity="0.5">
                            <animate attributeName="opacity" values="0.2;0.7;0.2" dur="2s" repeatCount="indefinite"/>
                        </path>
                        <path d="M10,-8 Q10,-12 6,-12" fill="none" stroke="#7c3aed" stroke-width="2" opacity="0.5">
                            <animate attributeName="opacity" values="0.2;0.7;0.2" dur="2s" begin="0.5s" repeatCount="indefinite"/>
                        </path>
                        <path d="M-14,-4 Q-14,-10 -8,-10" fill="none" stroke="#7c3aed" stroke-width="2" opacity="0.3">
                            <animate attributeName="opacity" values="0.1;0.5;0.1" dur="2s" begin="1s" repeatCount="indefinite"/>
                        </path>
                        <path d="M14,-4 Q14,-10 8,-10" fill="none" stroke="#7c3aed" stroke-width="2" opacity="0.3">
                            <animate attributeName="opacity" values="0.1;0.5;0.1" dur="2s" begin="1.5s" repeatCount="indefinite"/>
                        </path>
                    </g>
                    <line x1="130" y1="200" x2="150" y2="200" stroke="rgba(255,255,255,0.5)" stroke-width="2" stroke-dasharray="5,5">
                        <animate attributeName="stroke-dashoffset" from="0" to="10" dur="1s" repeatCount="indefinite"/>
                    </line>
                    
                    <!-- Data packets traveling -->
                    <circle r="3" fill="#fbbf24" opacity="0.8">
                        <animateMotion dur="3s" repeatCount="indefinite"
                            path="M200,110 L200,150"/>
                        <animate attributeName="opacity" values="0;0.8;0.8;0" dur="3s" repeatCount="indefinite"/>
                    </circle>
                    <circle r="3" fill="#fbbf24" opacity="0.8">
                        <animateMotion dur="3s" repeatCount="indefinite"
                            path="M270,200 L250,200"/>
                        <animate attributeName="opacity" values="0;0.8;0.8;0" dur="3s" repeatCount="indefinite"/>
                    </circle>
                    <circle r="3" fill="#fbbf24" opacity="0.8">
                        <animateMotion dur="3s" repeatCount="indefinite"
                            path="M200,290 L200,250"/>
                        <animate attributeName="opacity" values="0;0.8;0.8;0" dur="3s" repeatCount="indefinite"/>
                    </circle>
                    <circle r="3" fill="#fbbf24" opacity="0.8">
                        <animateMotion dur="3s" repeatCount="indefinite"
                            path="M130,200 L150,200"/>
                        <animate attributeName="opacity" values="0;0.8;0.8;0" dur="3s" repeatCount="indefinite"/>
                    </circle>
                </svg>
            </div>
        </div>
    </div>

    <script>
        const form = document.getElementById('loginForm');
        const loginBtn = document.getElementById('loginBtn');
        const loginBtnText = document.getElementById('loginBtnText');
        const loadingSpinner = document.getElementById('loadingSpinner');
        const errorMessage = document.getElementById('errorMessage');
        const successMessage = document.getElementById('successMessage');

        function showMessage(element, message) {
            element.textContent = message;
            element.style.display = 'block';
            setTimeout(() => {
                element.style.display = 'none';
            }, 5000);
        }

        function setLoading(isLoading) {
            loginBtn.disabled = isLoading;
            if (isLoading) {
                loadingSpinner.style.display = 'inline-block';
                loginBtnText.textContent = 'Signing In...';
            } else {
                loadingSpinner.style.display = 'none';
                loginBtnText.textContent = 'Login Now';
            }
        }

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;

            if (!username || !password) {
                showMessage(errorMessage, 'Please enter both username and password');
                return;
            }

            errorMessage.style.display = 'none';
            successMessage.style.display = 'none';
            setLoading(true);

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password })
                });

                const data = await response.json();

                if (data.success) {
                    localStorage.setItem('jwt_token', data.token);
                    localStorage.setItem('user', JSON.stringify(data.user));
                    successMessage.textContent = '✅ Login successful! Redirecting to dashboard...';
                    successMessage.style.display = 'block';

                    setTimeout(() => {
                        window.location.href = '/';
                    }, 1000);
                } else {
                    showMessage(errorMessage, data.message || 'Invalid credentials. Please try again.');
                    setLoading(false);
                }
            } catch (error) {
                console.error('Login error:', error);
                showMessage(errorMessage, 'Connection error. Please check your network and try again.');
                setLoading(false);
            }
        });

        // Auto-redirect if already logged in
        window.addEventListener('DOMContentLoaded', () => {
            const token = localStorage.getItem('jwt_token');
            if (token) {
                window.location.href = '/';
            }
        });

        // Add input validation feedback
        const inputs = document.querySelectorAll('input');
        inputs.forEach(input => {
            input.addEventListener('blur', () => {
                const wrapper = input.closest('.input-wrapper');
                if (input.value.trim() === '') {
                    wrapper.style.borderColor = '#ef4444';
                } else {
                    wrapper.style.borderColor = 'transparent';
                }
            });

            input.addEventListener('input', () => {
                const wrapper = input.closest('.input-wrapper');
                wrapper.style.borderColor = 'transparent';
                errorMessage.style.display = 'none';
            });
        });
    </script>
</body>
</html>