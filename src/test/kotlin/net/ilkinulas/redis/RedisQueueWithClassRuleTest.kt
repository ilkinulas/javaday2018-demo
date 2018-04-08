package net.ilkinulas.redis

import org.junit.Before
import org.junit.BeforeClass
import org.junit.ClassRule

class RedisQueueWithClassRuleTest {
    companion object {
        private const val REDIS_PORT = 6379
        private const val TEST_QUEUE_NAME = "test-queue-1"

        @ClassRule
        @JvmField
        val redisServer = KGenericContainer("redis:4.0.8-alpine").withExposedPorts(REDIS_PORT)

        private lateinit var queue: RedisQueue

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            val port = redisServer.getMappedPort(REDIS_PORT)
            queue = RedisQueue(host = "localhost", port = port, queue = TEST_QUEUE_NAME)
        }
    }

    @Before
    fun setUp() {
        queue.clear()
    }

    //COPY tests from RedisQueueTest
}