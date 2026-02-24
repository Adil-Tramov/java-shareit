#!/bin/bash
set -e

host="$1"
port="$2"
shift 2
cmd="$@"

echo "Waiting for $host:$port..."

if command -v nc &> /dev/null; then
    until nc -z "$host" "$port"; do
        >&2 echo "Waiting for $host:$port... (sleep 2)"
        sleep 2
    done
else

    until echo > /dev/tcp/"$host"/"$port" 2>/dev/null; do
        >&2 echo "Waiting for $host:$port... (sleep 2)"
        sleep 2
    done
fi

>&2 echo "$host:$port is available"
exec $cmd