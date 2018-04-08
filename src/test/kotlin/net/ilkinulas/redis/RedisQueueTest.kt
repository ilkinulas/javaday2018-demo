package net.ilkinulas.redis

import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RedisQueueTest {
    companion object {
        private const val REDIS_PORT = 6379
        private const val TEST_QUEUE_NAME = "test-queue-1"
    }

    private lateinit var queue: RedisQueue

    @Rule
    @JvmField
    val redisServer = KGenericContainer("redis:4.0.8-alpine").withExposedPorts(REDIS_PORT)
    //docker run --name redis-queue-test -d --rm -p 6379 redis:4.0.8-alpine
    //docker stop $container_id

    @Before
    fun setUp() {
        val port = redisServer.getMappedPort(REDIS_PORT)
        queue = RedisQueue(host = "localhost", port = port, queue = TEST_QUEUE_NAME)
        queue.clear()
    }

    @Test
    fun test_poll_multiple_items() {
        queue.add((1..100).map { it.toString() })

        TestCase.assertEquals(listOf("1"), queue.poll(1))

        with(queue) {
            TestCase.assertEquals(listOf("2", "3", "4", "5", "6"), poll(5))
            TestCase.assertEquals(94, size())
        }
        with(queue) {
            val l = poll(90)
            TestCase.assertEquals(90, l.size)
            TestCase.assertEquals("7", l[0])
            TestCase.assertEquals("96", l[l.size - 1])
            TestCase.assertEquals(4, size())
        }
        with(queue) {
            TestCase.assertEquals(listOf("97", "98", "99", "100"), poll(10))
            TestCase.assertTrue(poll(1).isEmpty())
        }
    }

    @Test
    fun test_poll_single_item() {
        assertNull(queue.poll())
        val item = "TestContainers Rocks"
        queue.add(item)
        assertEquals(item, queue.poll())
    }

    @Test
    fun test_fetch_more_than_queue_size() {
        queue.add((1..10).map { "$it" })
        TestCase.assertEquals(10, queue.size())
        var res = queue.poll(50)
        TestCase.assertEquals(10, res.size)
        TestCase.assertEquals(0, queue.size())
    }

    @Test
    fun test_add_multiple_items() {
        queue.add((1..5).map { "$it" })
        TestCase.assertEquals(5, queue.size())
        var res = queue.poll(3)
        TestCase.assertEquals(3, res.size)
        TestCase.assertEquals(2, queue.size())

        queue.add(listOf("a", "b", "c"))
        res = queue.poll(3)
        TestCase.assertEquals(listOf("4", "5", "a"), res)
    }

    @Test
    fun test_add_single_item() {
        queue.add("test1")
        queue.add("test2")
        TestCase.assertEquals(2, queue.size())
        var res = queue.poll(10)
        TestCase.assertEquals(2, res.size)
        TestCase.assertEquals(0, queue.size())
        assertEquals(listOf("test1", "test2"), res)
    }

    //@Test
    fun test_containers_without_rules() {
        KGenericContainer("redis:4.0.8-alpine").withExposedPorts(REDIS_PORT).use {
            it.start()
            val queue = RedisQueue("localhost", it.getMappedPort(REDIS_PORT), TEST_QUEUE_NAME)
            queue.clear()

            queue.add((1..10).map { it.toString() })
            assertEquals((1..8).map { it.toString() }, queue.poll(8))
            assertEquals(listOf("9", "10"), queue.poll(100))
        }
    }
}