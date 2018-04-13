package net.ilkinulas.redis

import net.ilkinulas.junitrules.RedisRule
import net.ilkinulas.junitrules.TestDurationRule
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test

class RuleDemoTest {

    companion object {
        @ClassRule
        @JvmField
        val testDuration = TestDurationRule()
    }

    @Rule
    @JvmField
    val redisRule = RedisRule()

    @Test
    fun test_1() {

    }

    @Test
    fun test_2() {
        Thread.sleep(1500)
    }

    @Test
    fun test_3() {

    }
}