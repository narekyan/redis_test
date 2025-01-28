# Redis and collections
This project implements a Map<String, Int> and List<Int> backed by Redis. The `RedisClusterMap` behaves like a regular Java Map, but stores its data in Redis using the Redis hash data structure. The `RedisClusterIntList` behaves like a regular Java List, but stores its data in Redis using the Redis hash data structure.

## Features
- Stores key-value pairs and int values in Redis.
- Supports common Map and List operations:
    - put, get, add, remove, containsKey, containsValue, size, isEmpty, clear.
- Have added one unit test
- Have redis configs to start redis cluster in `redis-cluster` folder

- Note: i didnt know do we need to use `set` or `hset`, this is something we can discuss, because both ways have thier pros and cons, current implemeantion uses `set`.
- Also i could use generics to have one interface for handle multiple types, but it would take more time to test.
- Also i wanted to use gradle to make 3rd party libs managemenat more dynamic, but for this test task that was not that vital.

## How to Run
- You have to have redis installed and started
- Start instances of redis
    - `redis-server redis-cluster/7000/redis.conf`
    - `redis-server redis-cluster/7001/redis.conf`
    - `redis-server redis-cluster/7002/redis.conf`
- Create the cluster
    - `redis-cli --cluster create 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 --cluster-replicas 0`
- Compile (already compiled)
    - `kotlinc Main.kt RedisClusterMap.kt RedisClusterIntList.kt -cp "$(cat classpath.txt)" -include-runtime -d Main.jar`
- Run 
    - `java -cp "Main.jar:libs/*" MainKt`
