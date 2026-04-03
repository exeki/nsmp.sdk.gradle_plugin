package ru.kazantsev.nsd.sdk.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsd.sdk.gradle_plugin.PluginFunctionalTestBase

class PushSrcTaskFunctionalTest : PluginFunctionalTestBase() {

    @Test
    fun showsPushSrcCliOptions() {
        writeConsumerProject()

        val result = runner("help", "--task", "push_src").build()

        assertTrue(result.output.contains("--inst"))
        assertTrue(result.output.contains("--scripts"))
        assertTrue(result.output.contains("--modules"))
        assertTrue(result.output.contains("--force"))
        assertTrue(result.output.contains("--scheme"))
        assertTrue(result.output.contains("--host"))
        assertTrue(result.output.contains("--key"))
        assertTrue(result.output.contains("--ignore-ssl"))
    }
}
