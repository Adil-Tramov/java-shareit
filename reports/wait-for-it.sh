# Вместо ожидания localhost:9090
# wait-for-it.sh localhost:9090 -t 60

wait-for-it.sh shareit-server:9090 -t 60
wait-for-it.sh shareit-gateway:8080 -t 30