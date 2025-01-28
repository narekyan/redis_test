import redis.clients.jedis.Jedis
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: java -cp jedis-5.1.3.jar:Main.jar MainKt <file-path>")
        return
    }

    val filePath = args[0]
    val file = File(filePath)
    if (!file.exists()) {
        println("Error: File not found at '$filePath'")
        return
    }

    // init redis
    val redis = Jedis("localhost", 6379)

    try {
        // clear
        val redisSetName = "unique_ips"
        redis.del(redisSetName)

        // count
        file.useLines { lines ->
            lines.forEach { ip ->
                redis.sadd(redisSetName, ip.trim())
            }
        }

        val uniqueCount = redis.scard(redisSetName)
        println("Unique IP addresses: $uniqueCount")
    } finally {
        redis.close()
    }
}