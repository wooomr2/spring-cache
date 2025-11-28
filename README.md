docker run --name redis -d -p 6379:6379 redis:8.2.1
docker exec -it redis redis-cli
set mykey "Hello Redis"
keys *
get mykey
del mykey
get mykey

https://redis.io/docs/latest/commands/
https://redis.io/docs/latest/develop/data-types/