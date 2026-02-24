#!/bin/bash
set -e

host="$1"
port="$2"
timeout="${3:-60}"
shift 3
cmd="$@"

echo "Waiting for $host:$port... (timeout: ${timeout}s)"

start_time=$(date +%s)
while ! nc -z "$host" "$port" 2>/dev/null; do
  current_time=$(date +%s)
  elapsed=$((current_time - start_time))

  if [ $elapsed -gt $timeout ]; then
    echo "Timeout waiting for $host:$port"
    exit 1
  fi

  echo "Still waiting for $host:$port... (${elapsed}s)"
  sleep 2
done

echo "$host:$port is available!"
exec $cmd