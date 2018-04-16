package net.peakgames.redis;

import net.ilkinulas.redis.RedisQueue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNull;

public class RedisQueueTest {
    private static final int REDIS_PORT = 6379;
    private static final String TEST_QUEUE_NAME = "test-queue-1";

    private RedisQueue queue;

    @Rule
    public GenericContainer redisServer =
            new GenericContainer("redis:4.0.8-alpine").withExposedPorts(REDIS_PORT);
    //docker run -d --rm -p 6379 redis:4.0.8-alpine
    //docker stop $container_id

    @Before
    public void setup() {
        int port = redisServer.getMappedPort(REDIS_PORT);
        queue = new RedisQueue("localhost", port, TEST_QUEUE_NAME);
        queue.connect();
        queue.clear();
    }

    @Test
    public void test_poll_multiple_items() {
        for (int i=1; i<=100; i++) {queue.add(String.valueOf(i));}
        assertEquals(Arrays.asList("1"), queue.poll(1));
        assertEquals(Arrays.asList("2", "3", "4", "5", "6"), queue.poll(5));
        assertEquals(94, queue.size());

        List<String> l = queue.poll(90);
        assertEquals(90, l.size());
        assertEquals("7", l.get(0));
        assertEquals("96", l.get(l.size() - 1));
        assertEquals(4, queue.size());

        assertEquals(Arrays.asList("97", "98", "99", "100"), queue.poll(10));
        assertTrue(queue.poll(10).isEmpty());
    }

    @Test
    public void test_poll_single_item() {
        assertNull(queue.poll());
        String item = "TestContainers Rocks";
        queue.add(item);
        assertEquals(item, queue.poll());
    }

    @Test
    public void test_fetch_more_than_queue_size() {
        for (int i=1; i<=10; i++) {queue.add(String.valueOf(i));}
        assertEquals(10, queue.size());
        List<String> res = queue.poll(50);
        assertEquals(10, res.size());
        assertEquals(0, queue.size());
    }

    @Test
    public void test_add_multiple_items() {
        for (int i=1; i<=5; i++) {queue.add(String.valueOf(i));}
        assertEquals(5, queue.size());
        List<String> res = queue.poll(3);
        assertEquals(3, res.size());
        assertEquals(2, queue.size());

        queue.add(Arrays.asList("a", "b", "c"));
        res = queue.poll(3);
        assertEquals(Arrays.asList("4", "5", "a"), res);
    }

    @Test
    public void test_add_single_item() {
        queue.add("test1");
        queue.add("test2");
        assertEquals(2, queue.size());
        List<String> res = queue.poll(10);
        assertEquals(2, res.size());
        assertEquals(0, queue.size());
        assertEquals(Arrays.asList("test1", "test2"), res);
    }

    @Test
    public void test_pop_lua_script() throws  Exception {
        for (int i=1; i<=100; i++) {queue.add(String.valueOf(i));}
        assertEquals(Arrays.asList("1"), queue.poplua(1));
        assertEquals(Arrays.asList("2", "3", "4", "5", "6"), queue.poplua(5));
        assertEquals(94, queue.size());

        List<String> l = queue.poll(90);
        assertEquals(90, l.size());
        assertEquals("7", l.get(0));
        assertEquals("96", l.get(l.size() - 1));
        assertEquals(4, queue.size());

        assertEquals(Arrays.asList("97", "98", "99", "100"), queue.poplua(10));
        assertTrue(queue.poll(10).isEmpty());
    }

}
