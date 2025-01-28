import redis.clients.jedis.JedisCluster
import redis.clients.jedis.HostAndPort

class RedisClusterMap(
    private val jedisCluster: JedisCluster
) : MutableMap<String, Int> {

    private inline fun forEachNode(action: (jedis: redis.clients.jedis.Jedis) -> Unit) {
        jedisCluster.clusterNodes.forEach { (nodeAddress, _) ->
            val host = nodeAddress.split(":")[0]
            val port = nodeAddress.split(":")[1].toInt()

            val nodeJedis = redis.clients.jedis.Jedis(host, port)
            nodeJedis.use { jedis -> action(jedis) } 
        }
    }

    override val size: Int
        get() {
            var count: Long = 0
            forEachNode { jedis ->
                count += jedis.dbSize()
            }
            return count.toInt()
        }

    override fun isEmpty(): Boolean = size == 0

    override fun containsKey(key: String): Boolean = jedisCluster.exists(key)

    override fun containsValue(value: Int): Boolean {
        forEachNode { jedis ->
            var cursor = "0"
            do {
                val scanResult = jedis.scan(cursor)
                val keys = scanResult.result
                keys.forEach { key ->
                    val type = jedis.type(key)
                    if (type != "string") {
                        return@forEach
                    }
                    if (jedis.get(key)?.toIntOrNull() == value) {
                        return true
                    }
                }
                cursor = scanResult.cursor
            } while (cursor != "0")
        }
        return false
    }

    override fun get(key: String): Int?{
        var value: Int? = null
        val type = jedisCluster.type(key)
        if (type == "string") {
            value = jedisCluster.get(key)?.toIntOrNull()
        }
        return value
    } 

    override fun put(key: String, value: Int): Int? {
        var oldValue: Int? = null
        val type = jedisCluster.type(key)
        if (type == "string") {
            oldValue = jedisCluster.get(key)?.toIntOrNull()
        }
        jedisCluster.set(key, value.toString())
        return oldValue
    }

    override fun remove(key: String): Int? {
        var oldValue: Int? = null
        val type = jedisCluster.type(key)
        if (type == "string") {
            oldValue = jedisCluster.get(key)?.toIntOrNull()
        }
        jedisCluster.del(key)
        return oldValue
    }

    override fun putAll(from: Map<out String, Int>) {
        from.forEach { (key, value) ->
            jedisCluster.set(key, value.toString())
        }
    }

    override fun clear() {
        forEachNode { jedis ->
            jedis.flushAll() // Clear all keys on the current node
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<String, Int>>
        get() {
            val entrySet = mutableSetOf<MutableMap.MutableEntry<String, Int>>()
            forEachNode { jedis ->
                var cursor = "0"
                do {
                    val scanResult = jedis.scan(cursor)
                    val keys = scanResult.result
                    keys.forEach { key ->
                        val type = jedis.type(key)
                        if (type != "string") {
                            return@forEach
                        }
                        val value = jedis.get(key)?.toIntOrNull()
                        if (value != null) {
                            entrySet.add(object : MutableMap.MutableEntry<String, Int> {
                                override val key: String = key
                                override val value: Int = value
                                override fun setValue(newValue: Int): Int {
                                    jedisCluster.set(key, newValue.toString())
                                    return value
                                }

                                override fun toString(): String {
                                    return "$key=$value"
                                }
                            })
                        }
                    }
                    cursor = scanResult.cursor
                } while (cursor != "0")
            }
            return entrySet
        }

    override val keys: MutableSet<String>
        get() {
            val keySet = mutableSetOf<String>()
            forEachNode { jedis ->
                var cursor = "0"
                do {
                    val scanResult = jedis.scan(cursor)
                    keySet.addAll(scanResult.result)
                    cursor = scanResult.cursor
                } while (cursor != "0")
            }
            return keySet
        }

    override val values: MutableCollection<Int>
        get() {
            val valueList = mutableListOf<Int>()
            forEachNode { jedis ->
                var cursor = "0"
                do {
                    val scanResult = jedis.scan(cursor)
                    val keys = scanResult.result
                    keys.forEach { key ->
                        val type = jedis.type(key)
                        if (type != "string") {
                            return@forEach
                        }
                        val value = jedis.get(key)?.toIntOrNull()
                        if (value != null) {
                            valueList.add(value)
                        }
                    }
                    cursor = scanResult.cursor
                } while (cursor != "0")
            }
            return valueList
        }
}