# EC2 Deployment Guide - Booking System

## Prerequisites

### 1. EC2 Instance Requirements
- **Instance Type**: t3.micro (2GB RAM) - minimal, t3.small recommended for better performance
- **OS**: Amazon Linux 2023 or Ubuntu 22.04
- **Storage**: At least 20GB
- **vCPUs**: 2
- **Memory**: 2GB (t3.micro) - requires JVM memory tuning

### 2. Security Group Configuration
Configure your EC2 security group to allow:

| Type | Protocol | Port Range | Source | Description |
|------|----------|------------|--------|-------------|
| SSH | TCP | 22 | Your IP | SSH access |
| Custom TCP | TCP | 8080 | 0.0.0.0/0 | Application |
| Custom TCP | TCP | 3307 | Your IP | MySQL (optional) |

## Step-by-Step Deployment

### Step 1: Launch EC2 Instance

1. Go to AWS EC2 Console
2. Click "Launch Instance"
3. Choose **t3.micro** instance type (or t3.small for better performance)
4. Select **Amazon Linux 2023** or **Ubuntu 22.04**
5. Configure storage: **20GB** minimum
6. Create/select security group with ports above
7. Create/download your key pair (e.g., `booking-system.pem`)
8. Launch instance

### Step 2: Connect to EC2

```bash
# Set permissions on your key file
chmod 400 booking-system.pem

# Connect to EC2
ssh -i booking-system.pem ec2-user@YOUR_EC2_PUBLIC_IP
```

Replace `YOUR_EC2_PUBLIC_IP` with your actual EC2 public IP address.

### Step 3: Initial EC2 Setup

```bash
# Update system
sudo yum update -y    # For Amazon Linux
# OR
sudo apt update && sudo apt upgrade -y    # For Ubuntu

# Install Docker
sudo yum install -y docker    # Amazon Linux
# OR
sudo apt install -y docker.io    # Ubuntu

# Start and enable Docker
sudo systemctl start docker
sudo systemctl enable docker

# Add user to docker group
sudo usermod -a -G docker ec2-user    # Amazon Linux
# OR
sudo usermod -a -G docker ubuntu    # Ubuntu

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify installations
docker --version
docker-compose --version

# Log out and back in for group changes
exit
```

### Step 4: Deploy Application

**Option A: Automated Deployment (Recommended)**

```bash
# Create deployment directory
mkdir -p ~/booking-system
cd ~/booking-system

# Download deployment files from GitHub
wget https://raw.githubusercontent.com/SHISIR42/BookingSystem/main/docker-compose.yml
wget https://raw.githubusercontent.com/SHISIR42/BookingSystem/main/deploy-ec2.sh
chmod +x deploy-ec2.sh

# Create .env file
cat > .env << EOF
MYSQL_ROOT_PASSWORD=YourSecurePassword123!
MYSQL_DATABASE=devops
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/devops
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=YourSecurePassword123!
EOF

# Run deployment script
./deploy-ec2.sh
```

**Option B: Manual Deployment**

```bash
# Create deployment directory
mkdir -p ~/booking-system
cd ~/booking-system

# Copy docker-compose.yml (use scp from local machine)
# From your local machine:
scp -i booking-system.pem docker-compose.yml ec2-user@YOUR_EC2_IP:~/booking-system/

# Back on EC2, create .env file
nano .env

# Add this content:
MYSQL_ROOT_PASSWORD=YourSecurePassword123!
MYSQL_DATABASE=devops
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/devops
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=YourSecurePassword123!

# Save and exit (Ctrl+X, Y, Enter)

# Pull and start services
docker pull shisir27/booking-system:latest
docker-compose up -d

# Check status
docker-compose ps
docker-compose logs -f
```

### Step 5: Verify Deployment

```bash
# Check running containers
docker-compose ps

# Should see:
# swe7303-mysql  (healthy)
# swe7303-app    (running)

# Check logs
docker-compose logs app
docker-compose logs mysql

# Test application
curl http://localhost:8080

# Get public IP
curl http://169.254.169.254/latest/meta-data/public-ipv4
```

### Step 6: Access Application

Open browser and navigate to:
```
http://YOUR_EC2_PUBLIC_IP:8080
```

## Useful Commands

```bash
# View logs
docker-compose logs -f              # All services
docker-compose logs -f app          # App only
docker-compose logs -f mysql        # MySQL only

# Restart services
docker-compose restart

# Stop services
docker-compose down

# Update application
docker pull shisir27/booking-system:latest
docker-compose up -d

# Check resource usage
docker stats

# Enter container shell
docker exec -it swe7303-app sh
docker exec -it swe7303-mysql mysql -uroot -p

# Clean up
docker-compose down -v              # Remove containers and volumes
docker system prune -a              # Clean all unused images
```

## Troubleshooting

### Application won't start
```bash
# Check logs
docker-compose logs app

# Common issues:
# 1. Database not ready - wait 30 seconds and check again
# 2. Port already in use - change port in docker-compose.yml
# 3. Memory issues - upgrade to t3.medium
```

### Can't connect from browser
```bash
# 1. Check security group allows port 8080
# 2. Verify containers are running
docker-compose ps

# 3. Check if app is listening
curl http://localhost:8080

# 4. Check EC2 public IP
curl http://169.254.169.254/latest/meta-data/public-ipv4
```

### MySQL connection errors
```bash
# Check MySQL is healthy
docker-compose ps

# Check MySQL logs
docker-compose logs mysql

# Verify .env file has correct credentials
cat .env

# Test MySQL connection
docker exec -it swe7303-mysql mysql -uroot -p
```

### Out of memory (t3.micro with 2GB RAM)
```bash
# Check memory usage
free -h
docker stats

# Solution 1: Add JVM memory limits to docker-compose.yml
# Edit docker-compose.yml, add under app service:
environment:
  JAVA_OPTS: "-Xmx512m -Xms256m"

# Solution 2: Add swap space
sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Solution 3: Upgrade to t3.small (2GB → 4GB RAM)
```

## CI/CD Integration

To integrate with Jenkins, update your Jenkinsfile deployment stage:

```groovy
stage('Deploy to EC2') {
    steps {
        script {
            // SSH to EC2 and deploy
            sh '''
                ssh -i /path/to/key.pem ec2-user@YOUR_EC2_IP << 'ENDSSH'
                cd ~/booking-system
                docker pull shisir27/booking-system:latest
                docker-compose up -d
                ENDSSH
            '''
        }
    }
}
```

## Cost Estimation

- **t3.micro**: ~$7.50/month (0.0104/hour) - Free tier eligible!
- **t3.small**: ~$15/month (0.0208/hour)
- **Storage (20GB)**: ~$2/month
- **Data Transfer**: Usually free tier covers it
- **Total (t3.micro)**: ~$9.50/month (FREE with AWS Free Tier for 12 months!)

## Security Best Practices

1. **Use strong passwords** in .env file
2. **Restrict security group** - only allow your IP for MySQL (port 3307)
3. **Keep system updated**: `sudo yum update -y`
4. **Use HTTPS** - set up SSL certificate with Let's Encrypt
5. **Regular backups** of MySQL volume
6. **Monitor logs** for suspicious activity

## Backup Database

```bash
# Backup
docker exec swe7303-mysql mysqldump -uroot -p$MYSQL_ROOT_PASSWORD devops > backup.sql

# Restore
docker exec -i swe7303-mysql mysql -uroot -p$MYSQL_ROOT_PASSWORD devops < backup.sql
```

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [AWS EC2 Documentation](https://docs.aws.amazon.com/ec2/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
