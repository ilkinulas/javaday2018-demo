import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import net.ilkinulas.redis.RedisCache
import org.junit.Before
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test
import org.testcontainers.containers.GenericContainer

class KGenericContainer(image: String) : GenericContainer<KGenericContainer>(image)

class RedisCacheTest {
    companion object {
        private const val REDIS_PORT = 6379
        private const val TEST_QUEUE_NAME = "test-queue-1"

        @ClassRule
        @JvmField
        val redisServer = KGenericContainer("redis:4.0.8-alpine").withExposedPorts(REDIS_PORT)


        private lateinit var redis: RedisCache

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            val port = redisServer.getMappedPort(REDIS_PORT)
            redis = RedisCache(host = "localhost", port = port, queue = TEST_QUEUE_NAME)
        }
    }

    @Before
    fun setUp() {
        redis.deleteQueue(TEST_QUEUE_NAME)
    }

    @Test
    fun testFetch() {
        redis.put((1..100).map { it.toString() })

        with(redis) {
            val l = fetch(1)
            TestCase.assertEquals(1, l.size)
            TestCase.assertEquals("1", l[0])
        }
        with(redis) {
            val l = fetch(5)
            TestCase.assertEquals(5, l.size)
            TestCase.assertEquals(listOf("2", "3", "4", "5", "6"), l)
            TestCase.assertEquals(94, length())
        }
        with(redis) {
            val l = fetch(90)
            TestCase.assertEquals(90, l.size)
            TestCase.assertEquals("7", l[0])
            TestCase.assertEquals("96", l[l.size - 1])
            TestCase.assertEquals(4, length())
        }
        with(redis) {
            val l = fetch(10)
            TestCase.assertEquals(listOf("97", "98", "99", "100"), l)
            TestCase.assertTrue(fetch(1).isEmpty())
        }
    }

    @Test
    fun testFetchMoreThanSize() {
        redis.put((1..10).map { "$it" })
        TestCase.assertEquals(10, redis.length())
        var res = redis.fetch(50)
        TestCase.assertEquals(10, res.size)
        TestCase.assertEquals(0, redis.length())
    }

    @Test
    fun testPutMultipleItems() {
        redis.put((1..5).map { "$it" })
        TestCase.assertEquals(5, redis.length())
        var res = redis.fetch(3)
        TestCase.assertEquals(3, res.size)
        TestCase.assertEquals(2, redis.length())

        redis.put(listOf("a", "b", "c"))
        res = redis.fetch(3)
        TestCase.assertEquals(listOf("4", "5", "a"), res)
    }

    @Test
    fun testPutSingleItem() {
        redis.put("test1")
        redis.put("test2")
        TestCase.assertEquals(2, redis.length())
        var res = redis.fetch(10)
        TestCase.assertEquals(2, res.size)
        TestCase.assertEquals(0, redis.length())
        assertEquals(listOf("test1", "test2"), res)
    }
}