import org.junit.jupiter.api.*
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisCluster
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisClusterMapTest {

    private lateinit var jedisCluster: JedisCluster
    private lateinit var redisMap: RedisClusterMap

    @BeforeAll
    fun setup() {
        jedisCluster = JedisCluster(
            setOf(
                HostAndPort("127.0.0.1", 7000),
                HostAndPort("127.0.0.1", 7001),
                HostAndPort("127.0.0.1", 7002)
            )
        )
        redisMap = RedisClusterMap(jedisCluster)
    }

    @BeforeEach
    fun clearData() {
        redisMap.clear() 
    }

    @AfterAll
    fun teardown() {
        jedisCluster.close()
    }

    @Test
    fun `test add and get keys`() {
        redisMap["key1"] = 100
        redisMap["key2"] = 200

        assertEquals(100, redisMap["key1"])
        assertEquals(200, redisMap["key2"])
        assertEquals(2, redisMap.size)
        assertTrue("key1" in redisMap.keys)
        assertTrue("key2" in redisMap.keys)
    }

    @Test
    fun `test remove key`() {
        redisMap["key1"] = 100
        redisMap["key2"] = 200

        redisMap.remove("key2")
        assertNull(redisMap["key2"])
        assertEquals(1, redisMap.size)
        assertFalse("key2" in redisMap.keys)
    }

    @Test
    fun `test isEmpty`() {
        assertTrue(redisMap.isEmpty())
        redisMap["key1"] = 100
        assertFalse(redisMap.isEmpty())
    }

    @Test
    fun `test containsKey and containsValue`() {
        redisMap["key1"] = 100

        assertTrue(redisMap.containsKey("key1"))
        assertFalse(redisMap.containsKey("key2"))
        assertTrue(redisMap.containsValue(100))
        assertFalse(redisMap.containsValue(200))
    }
}