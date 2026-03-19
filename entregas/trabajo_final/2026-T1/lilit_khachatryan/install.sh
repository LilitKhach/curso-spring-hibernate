#!/bin/bash

# Installation script for Analizador Financiero (Linux/macOS)

echo "================================"
echo "Analizador Financiero Setup"
echo "================================"
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "[ERROR] Node.js is not installed!"
    echo "Please download and install Node.js from https://nodejs.org/"
    echo ""
    exit 1
fi

echo "[OK] Node.js found"
node --version
npm --version
echo ""

# Install frontend dependencies
echo "================================"
echo "Installing Frontend Dependencies..."
echo "================================"
cd frontend
npm install

if [ $? -ne 0 ]; then
    echo "[ERROR] Frontend installation failed!"
    exit 1
fi

echo ""
echo "================================"
echo "Installation Complete!"
echo "================================"
echo ""
echo "Next steps:"
echo ""
echo "Option 1 - Local Development:"
echo "  Terminal 1: mvn spring-boot:run"
echo "  Terminal 2: cd frontend && npm start"
echo ""
echo "Option 2 - Docker:"
echo "  docker-compose up -d"
echo ""
echo "Access the application at:"
echo "  http://localhost:3000"
echo ""

