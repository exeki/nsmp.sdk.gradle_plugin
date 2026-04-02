package ru.kazantsev.nsd.sdk.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsd.sdk.gradle_plugin.PluginFunctionalTestBase
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
        Files.writeString(
            testProjectDir.resolve("scripts/sendScript.groovy"),
            "return \"test\""
        )

        val result = runner(
            "send_script",
            "--file-path", "scripts/sendScript.groovy",
            *remoteServerArgs()
        ).build()

        assertTrue(result.output.contains("NSD SCRIPT RESULT"))
    }
}
