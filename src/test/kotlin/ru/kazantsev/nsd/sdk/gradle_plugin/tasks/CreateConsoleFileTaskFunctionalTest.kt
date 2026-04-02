package ru.kazantsev.nsd.sdk.gradle_plugin.tasks

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.kazantsev.nsd.sdk.gradle_plugin.PluginFunctionalTestBase
import java.nio.file.Files

class CreateConsoleFileTaskFunctionalTest : PluginFunctionalTestBase() {

    @Test
    fun createsDefaultScript() {
        writeConsumerProject()

        runner("create_console_file").build()

        assertTrue(Files.exists(testProjectDir.resolve("src/main/groovy/console.groovy")))
    }
}
