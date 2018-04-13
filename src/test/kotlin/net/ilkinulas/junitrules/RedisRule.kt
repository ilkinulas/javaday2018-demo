package net.ilkinulas.junitrules

import org.junit.rules.ExternalResource

class RedisRule : ExternalResource() {
    override fun before() {
        println("Starting redis server")
        //Start redis server
    }

    override fun after() {
        println("Stopping redis server")
        //Stop redis server
    }
}