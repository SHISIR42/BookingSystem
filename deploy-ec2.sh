#!/bin/bash

# EC2 Deployment Script for Booking System
# This script deploys the application using docker-compose on EC2

set -e

echo "=========================================="
echo "EC2 Deployment Script - Booking System"
echo "=========================================="

# Configuration
APP_NAME="booking-system"
DOCKER_IMAGE="shisir27/booking-system:latest"
DEPLOY_DIR="/home/ec2-user/booking-system"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if running on EC2
if [ ! -f /sys/hypervisor/uuid ] || ! grep -q ec2 /sys/hypervisor/uuid 2>/dev/null; then
    echo -e "${YELLOW}Warning: This doesn't appear to be an EC2 instance${NC}"
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker is not installed!${NC}"
    echo "Installing Docker..."
    sudo yum update -y
    sudo yum install -y docker
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -a -G docker ec2-user
    echo -e "${GREEN}Docker installed successfully${NC}"
    echo -e "${YELLOW}Please log out and log back in for group changes to take effect${NC}"
    exit 0
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}Docker Compose is not installed!${NC}"
    echo "Installing Docker Compose..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo -e "${GREEN}Docker Compose installed successfully${NC}"
fi

# Create deployment directory
echo -e "${GREEN}Creating deployment directory...${NC}"
sudo mkdir -p $DEPLOY_DIR
sudo chown -R ec2-user:ec2-user $DEPLOY_DIR
cd $DEPLOY_DIR

# Check if .env file exists
if [ ! -f .env ]; then
    echo -e "${RED}Error: .env file not found!${NC}"
    echo "Please create a .env file with the following variables:"
    echo "  MYSQL_ROOT_PASSWORD=your_password"
    echo "  MYSQL_DATABASE=devops"
    echo "  SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/devops"
    echo "  SPRING_DATASOURCE_USERNAME=root"
    echo "  SPRING_DATASOURCE_PASSWORD=your_password"
    exit 1
fi

# Pull latest Docker image
echo -e "${GREEN}Pulling latest Docker image...${NC}"
docker pull $DOCKER_IMAGE

# Stop and remove existing containers
echo -e "${YELLOW}Stopping existing containers...${NC}"
docker-compose down || true

# Start services
echo -e "${GREEN}Starting services with docker-compose...${NC}"
docker-compose up -d

# Wait for application to be ready
echo -e "${YELLOW}Waiting for application to start...${NC}"
sleep 10

# Check if containers are running
if docker ps | grep -q "$APP_NAME"; then
    echo -e "${GREEN}=========================================="
    echo "Deployment Successful!"
    echo "==========================================${NC}"
    echo "Application is running on port 8080"
    echo "Database is running on port 3307"
    echo ""
    echo "Check status: docker-compose ps"
    echo "View logs: docker-compose logs -f"
    echo "Stop services: docker-compose down"
else
    echo -e "${RED}Deployment failed! Checking logs...${NC}"
    docker-compose logs
    exit 1
fi

# Show running containers
echo -e "${GREEN}Running containers:${NC}"
docker-compose ps

# Display access information
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 || echo "Unable to fetch")
echo ""
echo -e "${GREEN}Access your application at:${NC}"
echo "  http://$PUBLIC_IP:8080"
echo ""
echo -e "${YELLOW}Make sure security group allows:${NC}"
echo "  - Port 8080 (HTTP)"
echo "  - Port 22 (SSH)"
