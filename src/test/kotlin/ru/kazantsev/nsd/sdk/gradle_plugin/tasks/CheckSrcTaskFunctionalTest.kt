package ru.kazantsev.nsd.sdk.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsd.sdk.gradle_plugin.PluginFunctionalTestBase
import java.nio.file.Files

class CheckSrcTaskFunctionalTest : PluginFunctionalTestBase() {

    @Test
    fun showsCheckSrcCliOptions() {
        writeConsumerProject()

        val result = runner("help", "--task", "check_src").build()

        assertTrue(result.output.contains("--inst"))
        assertTrue(result.output.contains("--scripts"))
        assertTrue(result.output.contains("--modules"))
        assertTrue(result.output.contains("--scheme"))
        assertTrue(result.output.contains("--host"))
        assertTrue(result.output.contains("--key"))
        assertTrue(result.output.contains("--ignore-ssl"))
    }

    @Test
    fun reportsChangedSrcEntries() {
        writeConsumerProject()
        Files.createDirectories(testProjectDir.resolve(".nsd_sdk"))
        Files.writeString(
            testProjectDir.resolve(".nsd_sdk/info.json"),
            """
            {
              "scripts": [
                {
                  "checksum": "local-test-script-checksum",
                  "code": "testScript1",
                  "type": "script"
                }
              ],
              "modules": [
                {
                  "checksum": "local-test-module-checksum",
                  "code": "testModule1",
                  "type": "module"
                }
              ]
            }
            """.trimIndent()
        )

        val result = runner(
            "check_src",
            "--scripts=testScript1",
            "--modules=testModule1",
            *remoteServerArgs()
        ).build()

        assertTrue(result.output.contains("Changed script: testScript1"))
        assertTrue(result.output.contains("Changed module: testModule1"))
    }
}
