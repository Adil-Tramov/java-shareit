#!/bin/bash
set -e

host="$1"
port="$2"
shift 2
cmd="$@"

echo "Waiting for $host:$port..."

until nc -z "$host" "$port"; do
  >&2 echo "Waiting for $host:$port... (sleep 2)"
  sleep 2
done

>&2 echo "$host:$port is available"
exec $cmd