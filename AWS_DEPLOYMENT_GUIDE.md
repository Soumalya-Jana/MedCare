# ☁️ AWS Deployment Guide — MedCare Hospital Management System

This guide outlines two proven methods to deploy the **MedCare Hospital Management System** on Amazon Web Services (AWS):

1. **Option A: AWS EC2 with Docker & Docker Compose** *(Recommended: Free Tier eligible, easiest setup, 1-click launch)*
2. **Option B: AWS EC2 + AWS RDS Managed MySQL** *(Enterprise architecture with managed database backups)*

---

## 🛠️ Pre-Deployment: Build the Production JAR
Before uploading to AWS, verify that the production JAR is compiled:
```bash
./mvnw clean package -DskipTests
```
This generates the standalone executable at:
`target/hospital-management-system-1.0.0.jar`

---

## 🚀 Option A: Deploy on AWS EC2 (Single VM + Docker Compose)

### Step 1: Launch an AWS EC2 Instance
1. Log in to the [AWS Management Console](https://aws.amazon.com/console/).
2. Navigate to **EC2** → **Launch Instance**.
3. Choose:
   - **Name**: `MedCare-HMS-Server`
   - **AMI**: `Ubuntu Server 24.04 LTS` or `22.04 LTS` (64-bit x86)
   - **Instance Type**: `t2.micro` or `t3.micro` (Free Tier eligible) or `t3.small` (Recommended for production: 2 GB RAM)
   - **Key Pair**: Create or select an existing `.pem` key pair (e.g., `medcare-key.pem`)
4. **Configure Network / Security Group**:
   Allow the following Inbound Rules:
   - **SSH (Port 22)**: Source `My IP` (for secure administration)
   - **HTTP (Port 80)**: Source `Anywhere (0.0.0.0/0)`
   - **HTTPS (Port 443)**: Source `Anywhere (0.0.0.0/0)`
   - **Custom TCP (Port 8080)** *(Optional)*: Source `Anywhere (0.0.0.0/0)`
5. Click **Launch Instance**.

---

### Step 2: Connect to Your EC2 Instance
On your local computer (PowerShell, Terminal, or Git Bash):
```bash
# Set appropriate permissions on your private key (Linux/macOS)
chmod 400 medcare-key.pem

# SSH into your EC2 instance (replace with your public IP)
ssh -i "medcare-key.pem" ubuntu@<YOUR-EC2-PUBLIC-IP>
```

---

### Step 3: Install Docker & Docker Compose on EC2
Run these commands inside your EC2 terminal:
```bash
# Update packages
sudo apt update && sudo apt upgrade -y

# Install Docker
sudo apt install -y docker.io docker-compose

# Add ubuntu user to docker group (so you don't need sudo)
sudo usermod -aG docker ubuntu
newgrp docker

# Verify Docker installation
docker --version
docker-compose --version
```

---

### Step 4: Transfer Project Files to EC2
From your local project folder (PowerShell or Terminal on your PC):
```bash
# Transfer JAR, Dockerfile, and docker-compose.yml
scp -i "medcare-key.pem" target/hospital-management-system-1.0.0.jar ubuntu@<YOUR-EC2-PUBLIC-IP>:~/target/
scp -i "medcare-key.pem" Dockerfile ubuntu@<YOUR-EC2-PUBLIC-IP>:~/
scp -i "medcare-key.pem" docker-compose.yml ubuntu@<YOUR-EC2-PUBLIC-IP>:~/
```
*(Alternatively, clone your Git repository directly on EC2 and run `./mvnw clean package -DskipTests`)*.

---

### Step 5: Start the Application with Docker Compose
On your EC2 terminal:
```bash
# Run in background
docker-compose up -d --build

# View real-time logs
docker-compose logs -f medcare-app
```

Once you see:
`Started HospitalManagementApplication in ... seconds`

Open your web browser and visit:
```
http://<YOUR-EC2-PUBLIC-IP>
```
Your Hospital Management System is now live!

---

## 🏛️ Option B: Deploying with AWS RDS (Managed MySQL)

For high-availability production environments:

### 1. Create AWS RDS MySQL Instance:
- Engine: **MySQL 8.0**
- Template: **Free Tier** (`db.t3.micro` or `db.t4g.micro`)
- DB Instance Identifier: `medcare-mysql-db`
- Master Username: `admin`
- Master Password: `<Choose-A-Secure-Password>`
- Initial Database Name: `hospital_db`
- In the RDS Security Group: Allow Inbound **MySQL/Aurora (Port 3306)** from your EC2 Security Group.

### 2. Run the JAR Directly as a Systemd Service on EC2:
Install Java 17/21 on EC2:
```bash
sudo apt update
sudo apt install -y openjdk-17-jre-headless
```

Create `/etc/systemd/system/medcare.service`:
```ini
[Unit]
Description=MedCare Hospital Management System
After=syslog.target network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu
ExecStart=/usr/bin/java -Xms256m -Xmx768m \
  -Dspring.datasource.url=jdbc:mysql://<RDS-ENDPOINT>:3306/hospital_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \
  -Dspring.datasource.username=admin \
  -Dspring.datasource.password=<YOUR-RDS-PASSWORD> \
  -Dspring.jpa.hibernate.ddl-auto=update \
  -jar /home/ubuntu/hospital-management-system-1.0.0.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start the service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable medcare
sudo systemctl start medcare
sudo systemctl status medcare
```

---

## 🔒 Optional: Add Free Domain & SSL (HTTPS) with Nginx + Certbot

1. Install Nginx:
   ```bash
   sudo apt install -y nginx certbot python3-certbot-nginx
   ```
2. Configure Nginx proxy (`/etc/nginx/sites-available/medcare`):
   ```nginx
   server {
       server_name yourdomain.com www.yourdomain.com;

       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
   }
   ```
3. Enable site:
   ```bash
   sudo ln -s /etc/nginx/sites-available/medcare /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl restart nginx
   ```
4. Obtain Free SSL:
   ```bash
   sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
   ```
