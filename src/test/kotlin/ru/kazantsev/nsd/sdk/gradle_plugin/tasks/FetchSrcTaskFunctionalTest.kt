package ru.kazantsev.nsd.sdk.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsd.sdk.gradle_plugin.PluginFunctionalTestBase
import java.nio.file.Files

class FetchSrcTaskFunctionalTest : PluginFunctionalTestBase() {

    @Test
    fun createsScriptFileInScriptsSourceRootWithCliConnection() {
        writeConsumerProject()

        runner(
            "fetch_src",
            "--scripts=testScript1",
            "--modules=testModule1",
            *remoteServerArgs()
        ).build()

        assertTrue(Files.exists(testProjectDir.resolve("src/main/scripts/ru/kazantsev/demo/testScript1.groovy")))
    }

    @Test
    fun createsScriptFileInScriptsSourceRootFromBuildScriptConfiguration() {
        writeConsumerProject(
            """
            smpSdk {
                setInstallation( "$TEST_INSTALLATION_ID")
            }
            """.trimIndent()
        )

        runner(
            "fetch_src",
            "--scripts=testScript1",
            "--modules=testModule1"
        ).build()

        assertTrue(Files.exists(testProjectDir.resolve("src/main/scripts/ru/kazantsev/demo/testScript1.groovy")))
    }

}
