#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# setup-aws.sh
# One-time AWS infrastructure setup for Small Banking App
# Run once after creating your AWS account
#
# Requirements:
#   - AWS CLI configured with credentials
#   - Docker installed
#   - jq installed
#
# Usage:
#   chmod +x setup-aws.sh
#   DB_PASSWORD=yourpassword JWT_SECRET=yoursecret MONGO_URI=yourmongouri ./setup-aws.sh
# ─────────────────────────────────────────────────────────────────────────────

set -euo pipefail

AWS_REGION="us-east-2"
ACCOUNT_ID="721717798149"
CLUSTER="smallbank-cluster"
ECR_BACKEND="$ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/smallbank-backend"
ECR_FRONTEND="$ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/smallbank-frontend"

# Required env vars
DB_URL="${DB_URL:?DB_URL is required}"
DB_PASSWORD="${DB_PASSWORD:?DB_PASSWORD is required}"
MONGO_URI="${MONGO_URI:?MONGO_URI is required}"
JWT_SECRET="${JWT_SECRET:?JWT_SECRET is required}"

echo "──────────────────────────────────────────"
echo " Small Banking App — AWS Setup"
echo " Region:  $AWS_REGION"
echo " Account: $ACCOUNT_ID"
echo "──────────────────────────────────────────"

# ── 1. Create ECS Task Execution Role ────────────────────────────────────────
echo "→ Creating ECS Task Execution Role..."
aws iam create-role \
  --role-name ecsTaskExecutionRole \
  --assume-role-policy-document '{
    "Version":"2012-10-17",
    "Statement":[{
      "Effect":"Allow",
      "Principal":{"Service":"ecs-tasks.amazonaws.com"},
      "Action":"sts:AssumeRole"
    }]
  }' 2>/dev/null || echo "  Role already exists, skipping."

aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy 2>/dev/null || true

aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/AmazonSSMReadOnlyAccess 2>/dev/null || true

echo "  ✅ ECS Task Execution Role ready"

# ── 2. Store secrets in SSM Parameter Store ──────────────────────────────────
echo "→ Storing secrets in SSM Parameter Store..."

aws ssm put-parameter --name "/smallbank/DB_URL"      --value "$DB_URL"      --type SecureString --overwrite --region $AWS_REGION
aws ssm put-parameter --name "/smallbank/DB_PASSWORD"  --value "$DB_PASSWORD"  --type SecureString --overwrite --region $AWS_REGION
aws ssm put-parameter --name "/smallbank/MONGO_URI"    --value "$MONGO_URI"    --type SecureString --overwrite --region $AWS_REGION
aws ssm put-parameter --name "/smallbank/JWT_SECRET"   --value "$JWT_SECRET"   --type SecureString --overwrite --region $AWS_REGION

echo "  ✅ Secrets stored in SSM"

# ── 3. Create CloudWatch Log Groups ─────────────────────────────────────────
echo "→ Creating CloudWatch Log Groups..."
aws logs create-log-group --log-group-name /ecs/smallbank-backend  --region $AWS_REGION 2>/dev/null || true
aws logs create-log-group --log-group-name /ecs/smallbank-frontend --region $AWS_REGION 2>/dev/null || true
echo "  ✅ Log groups created"

# ── 4. Register Task Definitions ─────────────────────────────────────────────
echo "→ Registering ECS Task Definitions..."
aws ecs register-task-definition \
  --cli-input-json file://task-definition-backend.json \
  --region $AWS_REGION > /dev/null
aws ecs register-task-definition \
  --cli-input-json file://task-definition-frontend.json \
  --region $AWS_REGION > /dev/null
echo "  ✅ Task definitions registered"

# ── 5. Create ECS Services ────────────────────────────────────────────────────
echo "→ Creating ECS Services..."

# Get default VPC and subnets
VPC_ID=$(aws ec2 describe-vpcs --filters Name=isDefault,Values=true \
  --query "Vpcs[0].VpcId" --output text --region $AWS_REGION)
SUBNET_IDS=$(aws ec2 describe-subnets --filters Name=vpc-id,Values=$VPC_ID \
  --query "Subnets[*].SubnetId" --output text --region $AWS_REGION | tr '\t' ',')

# Create security group for ECS
SG_ID=$(aws ec2 create-security-group \
  --group-name smallbank-ecs-sg \
  --description "Security group for SmallBank ECS services" \
  --vpc-id $VPC_ID \
  --region $AWS_REGION \
  --query "GroupId" --output text 2>/dev/null || \
  aws ec2 describe-security-groups \
  --filters Name=group-name,Values=smallbank-ecs-sg \
  --query "SecurityGroups[0].GroupId" --output text --region $AWS_REGION)

# Allow inbound on ports 8080 and 80
aws ec2 authorize-security-group-ingress --group-id $SG_ID \
  --protocol tcp --port 8080 --cidr 0.0.0.0/0 --region $AWS_REGION 2>/dev/null || true
aws ec2 authorize-security-group-ingress --group-id $SG_ID \
  --protocol tcp --port 80 --cidr 0.0.0.0/0 --region $AWS_REGION 2>/dev/null || true

# Backend service
aws ecs create-service \
  --cluster $CLUSTER \
  --service-name smallbank-backend-service \
  --task-definition smallbank-backend \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[$SUBNET_IDS],securityGroups=[$SG_ID],assignPublicIp=ENABLED}" \
  --region $AWS_REGION > /dev/null 2>/dev/null || echo "  Backend service already exists"

# Frontend service
aws ecs create-service \
  --cluster $CLUSTER \
  --service-name smallbank-frontend-service \
  --task-definition smallbank-frontend \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[$SUBNET_IDS],securityGroups=[$SG_ID],assignPublicIp=ENABLED}" \
  --region $AWS_REGION > /dev/null 2>/dev/null || echo "  Frontend service already exists"

echo "  ✅ ECS Services created"

echo ""
echo "──────────────────────────────────────────"
echo " ✅ AWS Setup complete!"
echo " Next: Run the GitHub Actions workflow to"
echo " build and push Docker images to ECR."
echo "──────────────────────────────────────────"
