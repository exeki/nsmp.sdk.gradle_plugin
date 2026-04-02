package ru.kazantsev.nsd.sdk.gradle_plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import ru.kazantsev.nsd.sdk.gradle_plugin.services.SourceSetsService

abstract class CreateConsoleFileTask : DefaultTask() {
    companion object {
        const val NAME = "create_console_file"
    }

    @get:Input
    abstract val consoleFilePath: Property<String>

    init {
        group = "nsd_sdk_local"
        description = "Creates the console script file and supporting source directories"
    }

    @TaskAction
    fun action() {
        SourceSetsService(project).createConsoleFile(consoleFilePath.get())
    }
}
