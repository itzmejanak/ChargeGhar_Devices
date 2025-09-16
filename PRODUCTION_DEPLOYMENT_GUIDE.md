# 🚀 IoT Demo Production Deployment Guide

## 📋 Summary: Docker-Only Deployment (Recommended)

**Answer to your question:** **YES, Docker CLI is enough!** 
- ✅ No Java installation needed
- ✅ No Maven installation needed  
- ✅ No Tomcat installation needed
- ✅ No Redis installation needed
- ✅ Everything runs in containers

---

## 🎯 Deployment Strategy

### **Why Docker-Only Approach:**
1. **Your project is Docker-ready** - Multi-stage Dockerfile handles Maven build
2. **Zero dependency management** - All dependencies packaged in containers
3. **Production consistency** - Same environment as your local testing (90% success rate)
4. **Easy maintenance** - Single command deployment and updates

---

## 📦 What You Need on Server

**Only requirement:** Docker + Docker Compose
- Server: Ubuntu 24.04.3 LTS ✅
- Architecture: x86_64 ✅  
- RAM: Available ✅
- Storage: 386GB available ✅

---

## 🔧 Step-by-Step Deployment

### **Phase 1: Initial Server Setup (One-time)**

```bash
# 1. Connect to server
ssh root@213.210.21.113

# 2. Download and run server setup script
curl -O https://raw.githubusercontent.com/itzmejanak/ChargeGhar_Devices/main/deploy-server-setup.sh
chmod +x deploy-server-setup.sh
./deploy-server-setup.sh
```

### **Phase 2: Application Deployment**

```bash
# 1. Download and run deployment script
curl -O https://raw.githubusercontent.com/itzmejanak/ChargeGhar_Devices/main/deploy-production.sh
chmod +x deploy-production.sh
./deploy-production.sh
```

**That's it!** Your application will be running at:
- **Application:** http://213.210.21.113:8080
- **Health Check:** http://213.210.21.113:8080/health

---

## 🔄 Updates & Maintenance

### **Deploy New Version:**
```bash
cd /opt/iotdemo
./deploy-production.sh
```

### **View Logs:**
```bash
cd /opt/iotdemo
docker-compose -f docker-compose.prod.yml logs -f
```

### **Stop Application:**
```bash
cd /opt/iotdemo
docker-compose -f docker-compose.prod.yml down
```

### **Restart Application:**
```bash
cd /opt/iotdemo
docker-compose -f docker-compose.prod.yml up -d
```

---

## 📊 Monitoring Commands

```bash
# Check container status
docker-compose -f /opt/iotdemo/docker-compose.prod.yml ps

# Check resource usage
docker stats

# Check application health
curl http://localhost:8080/health

# View real-time logs
docker-compose -f /opt/iotdemo/docker-compose.prod.yml logs -f app
```

---

## 🌐 Domain Setup (Tomorrow)

When you get your domain:

1. **Update DNS:** Point domain to `213.210.21.113`
2. **Setup Nginx reverse proxy** (optional for SSL)
3. **Configure SSL certificate** with Let's Encrypt

---

## 🚨 Troubleshooting

### **Common Issues:**

1. **Port 8080 in use:**
   ```bash
   netstat -tulpn | grep :8080
   # Kill process if needed
   ```

2. **Docker permission issues:**
   ```bash
   sudo usermod -aG docker $USER
   # Logout and login again
   ```

3. **Container fails to start:**
   ```bash
   docker-compose -f /opt/iotdemo/docker-compose.prod.yml logs app
   ```

---

## ✅ Deployment Checklist

- [ ] Server access confirmed (SSH working)
- [ ] Docker installation script ready
- [ ] Production deployment script ready
- [ ] Repository access confirmed
- [ ] EMQX Cloud credentials working
- [ ] Test deployment process
- [ ] Verify application health
- [ ] Plan domain configuration

---

## 🔐 Security Notes

- ✅ Containers run as non-root user
- ✅ Production Redis password
- ✅ Health checks configured
- ✅ Resource limits set
- 📋 **TODO:** Setup firewall rules (only 8080, 22, 80, 443)
- 📋 **TODO:** SSL certificate when domain is ready

---

**🎯 Next Action:** Run the server setup script to install Docker, then run the deployment script. Your application will be live in minutes!