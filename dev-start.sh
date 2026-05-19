#!/bin/bash

echo "Starting Shotlog Development Environment..."
echo

echo "Building the application..."
./mvnw clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "Build failed! Please check the errors above."
    exit 1
fi

echo
echo "Starting Docker containers..."
docker-compose -f docker-compose.dev.yml up --build

echo
echo "Development environment stopped."