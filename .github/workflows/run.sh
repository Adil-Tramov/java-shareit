#!/bin/bash
set -e

echo "Docker is up"

./wait-for-it.sh postgres 5432 -t 60

./wait-for-it.sh server 9090 -t 120
