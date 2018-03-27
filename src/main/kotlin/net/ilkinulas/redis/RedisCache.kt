package net.ilkinulas.redis

import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.exceptions.JedisConnectionException

interface CacheInterface {
    fun fetch(count: Long): List<String>
    fun pop(): String?
    fun close()
    fun length(): Long
    fun put(s: String): Long
    fun put(l: List<String>): Long
}

private val logger = LoggerFactory.getLogger(RedisCache::class.java)

class RedisCache(
        host: String = "localhost",
        port: Int = 6379,
        private val queue: String) : CacheInterface {

    private var pool: JedisPool

    init {
        val poolConfig = JedisPoolConfig()
        pool = JedisPool(poolConfig, host, port)
        try {
            pool.resource.use {
                it.ping()
            }
        } catch (e: JedisConnectionException) {
            logger.error("Can not connect to redis on $host:$port")
            throw e
        }
    }

    override fun close() {
        pool.close()
    }

    override fun length() = pool.resource.use { it.llen(queue) }

    override fun fetch(count: Long): List<String> {
        pool.resource.use {
            val tx = it.multi()
            val response = tx.lrange(queue, 0, count - 1)
            tx.ltrim(queue, count, -1)
            tx.exec()
            return response.get()
        }
    }

    override fun pop(): String? {
        pool.resource.use {
            return it.lpop(queue)
        }
    }

    override fun put(l: List<String>): Long {
        pool.resource.use {
            return it.rpush(queue, *l.toTypedArray())
        }
    }

    override fun put(s: String): Long {
        pool.resource.use {
            return it.rpush(queue, s)
        }
    }

    fun deleteQueue(qname: String) {
        pool.resource.use {
            it.del(qname)
        }
    }
}