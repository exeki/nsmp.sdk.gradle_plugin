package ru.kazantsev.nsd.sdk.gradle_plugin

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PluginFunctionalTest : PluginFunctionalTestBase() {

    @Test
    fun registersPluginTasksInConsumerProject() {
        writeConsumerProject()

        val result = runner("tasks", "--all").build()

        assertTrue(result.output.contains("create_console_file"))
        assertTrue(result.output.contains("fetch_src"))
        assertTrue(result.output.contains("check_src"))
        assertTrue(result.output.contains("push_src"))
        assertTrue(result.output.contains("send_script"))
    }

    @Test
    fun addsDevelopmentDependenciesAutomatically() {
        writeConsumerProject()

        val result = runner("printImplementationDeps").build()

        assertTrue(result.output.contains("ru.kazantsev.nsd:json_rpc_connector:1.+"))
        assertTrue(result.output.contains("ru.kazantsev.nsd.sdk:global_variables:1.+"))
    }
}
