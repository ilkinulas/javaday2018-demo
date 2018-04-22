package net.ilkinulas.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

class RedisQueue implements Queue, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(RedisQueue.class);
    private final String host;
    private final int port;
    private final String queueName;

    private JedisPool pool;

    RedisQueue(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.queueName = name;
    }

    void connect() {
        pool = new JedisPool(new JedisPoolConfig(), host, port);
        logger.info("RedisQueue is ready. Connected to {}:{}", host, port);
    }

    @Override
    public void close() {
        pool.close();
    }

    @Override
    public String poll() {
        try (Jedis redis = pool.getResource()) {
            return redis.lpop(queueName);
        }
    }
    
    @Override
    public long size() {
        try (Jedis redis = pool.getResource()) {
            return redis.llen(queueName);
        }
    }

    @Override
    public long add(String s) {
        try (Jedis redis = pool.getResource()) {
            return redis.rpush(queueName, s);
        }
    }

    @Override
    public long add(List<String> l) {
        try (Jedis redis = pool.getResource()) {
            return redis.rpush(queueName, l.toArray(new String[l.size()]));
        }
    }

    @Override
    public List<String> poll(int count) {
        try (Jedis redis = pool.getResource()) {
            Transaction tx = redis.multi();
            Response<List<String>> response = tx.lrange(queueName, 0, count - 1);
            tx.ltrim(queueName, count, -1);
            tx.exec();
            return response.get();
        }
    }

    @Override
    public void clear() {
        try (Jedis redis = pool.getResource()) {
            redis.del(queueName);
        }
    }

    public List<String> poplua(long count) throws Exception {
        String script = readFromClassPath("lpopn.lua");
        try (Jedis redis = pool.getResource()) {
            Object result = redis.eval(script, 1, queueName, String.valueOf(count));
            if (result == null) {
                return Collections.emptyList();
            }
            return (List<String>) result;
        }
    }

    private String readFromClassPath(final String fileName) throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader()
                        .getResource(fileName)
                        .toURI())));
    }
}
