# ====================================================================
# Dockerfile for Online Shopping and Order Management System
# ====================================================================

# Base image with stable LTS Node.js runtime
FROM node:20-alpine

# Set working directory inside container
WORKDIR /usr/src/app

# Copy dependency manifests first for cached layer installation
COPY package*.json ./

# Install production dependencies only
RUN npm install --omit=dev

# Copy application source code
COPY . .

# Expose HTTP port 3000
EXPOSE 3000

# Set production environment variables
ENV NODE_ENV=production
ENV PORT=3000
ENV HOST=0.0.0.0

# Start Express server
CMD ["node", "server.js"]
