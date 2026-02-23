#!/bin/bash
set -e

echo "Docker is up"

sleep 10

./wait-for-it.sh postgres 5432 -t 60

./wait-for-it.sh server 9090 -t 120

./wait-for-it.sh gateway 8080 -t 60

echo "All services are up! Running tests..."
