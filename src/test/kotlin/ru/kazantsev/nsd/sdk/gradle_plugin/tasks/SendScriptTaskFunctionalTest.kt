package ru.kazantsev.nsd.sdk.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsd.sdk.gradle_plugin.PluginFunctionalTestBase
import ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote.SendScriptTask
import java.nio.file.Files

class SendScriptTaskFunctionalTest : PluginFunctionalTestBase() {

    @Test
    fun showsSendScriptCliOptions() {
        writeConsumerProject()

        val result = runner("help", "--task", "send_script").build()

        assertTrue(result.output.contains("--inst"))
        assertTrue(result.output.contains("--file-path"))
        assertTrue(result.output.contains("--scheme"))
        assertTrue(result.output.contains("--host"))
        assertTrue(result.output.contains("--key"))
        assertTrue(result.output.contains("--ignore-ssl"))
    }

    @Test
    fun sendsConsoleScriptToRemoteServer() {
        writeConsumerProject()
        Files.createDirectories(testProjectDir.resolve("scripts"))
        val testString = "super useless test"
        Files.writeString(
            testProjectDir.resolve("scripts/sendScript.groovy"),
            "return \"$testString\""
        )

        val result = runner(
            "send_script",
            "--file-path", "scripts/sendScript.groovy",
            *remoteServerArgs()
        ).build()

        assertTrue(result.output.contains(SendScriptTask.RESULT_START))
        assertTrue(result.output.contains(testString))
    }
}
