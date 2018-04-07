package net.ilkinulas.redis

import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

interface Queue {
    fun poll(): String?
    fun poll(count: Long): List<String>
    fun size(): Long
    fun add(s: String): Long
    fun add(l: List<String>): Long
    fun clear()
}

private val logger = LoggerFactory.getLogger(RedisQueue::class.java)

class RedisQueue(
        host: String = "localhost",
        port: Int = 6379,
        private val queue: String) : Queue, AutoCloseable {

    private var pool: JedisPool

    init {
        val poolConfig = JedisPoolConfig()
        pool = JedisPool(poolConfig, host, port)
        logger.info("RedisQueue is ready. Connected to $host:$port")
    }

    override fun poll(): String? {
        pool.resource.use {
            return it.lpop(queue)
        }
    }

    override fun poll(count: Long): List<String> {
        pool.resource.use {
            val tx = it.multi()
            val response = tx.lrange(queue, 0, count - 1)
            tx.ltrim(queue, count, -1)
            tx.exec()
            return response.get()
        }
    }

    override fun size() = pool.resource.use { it.llen(queue) }

    override fun add(s: String): Long {
        pool.resource.use {
            return it.rpush(queue, s)
        }
    }

    override fun add(l: List<String>): Long {
        pool.resource.use {
            return it.rpush(queue, *l.toTypedArray())
        }
    }

    override fun clear() {
        pool.resource.use {
            it.del(queue)
        }
    }

    override fun close() {
        pool.close()
    }

}