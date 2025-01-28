import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisCluster

fun main() {
    // init JedisCluster
    val jedisCluster = JedisCluster(
        setOf(
            HostAndPort("127.0.0.1", 7000),
            HostAndPort("127.0.0.1", 7001),
            HostAndPort("127.0.0.1", 7002)
        )
    )

    /////////// map <string, int>

    val redisMap = RedisClusterMap(jedisCluster)

    println()
    redisMap.clear()

    // Add keys
    redisMap["key1"] = 100
    redisMap["key2"] = 200

    // Get values
    println("Value for 'key1': ${redisMap["key1"]}")
    println("Value for 'key2': ${redisMap["key2"]}")

    // Check size and keys
    println("Map size: ${redisMap.size}")
    println("Keys: ${redisMap.keys}")

    // Update a key
    redisMap["key1"] = 300
    println("Updated value for 'key1': ${redisMap["key1"]}")

    // Remove a key
    redisMap.remove("key2")
    println("Map after removing 'key2': ${redisMap.entries}")

    /////////// list <int>
 
    val redisList = RedisClusterIntList(jedisCluster, "intList")

    println()

    // Add
    redisList.add(10)
    redisList.addAll(listOf(20, 30, 40))
    println("Int List: ${redisList.getAll()}") 
    // Remove at index
    redisList.removeAt(1)
    
    println("Int List after index remove: ${redisList.getAll()}") 

    println()

    jedisCluster.close()
}