package net.ilkinulas.junitrules

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class TestDurationRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val start = System.currentTimeMillis()
                try {
                    base.evaluate()
                } finally {
                    val duration = System.currentTimeMillis() - start
                    println("${description.displayName} took $duration ms")
                }
            }
        }
    }
}
