package ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class SendScriptTask : RemoteNsdTask() {

    companion object {
        const val NAME = "send_script"
        const val RESULT_START = "------------SMP SCRIPT RESULT------------"
        const val RESULT_END = "-----------------------------------------"
    }

    @get:Input
    @get:Option(option = "file-path", description = "Path to the script file relative to the project directory")
    abstract val filePath: Property<String>

    init {
        description = "Sends the configured script to SMP"
    }

    @TaskAction
    fun action() {
        val file = File(project.projectDir, filePath.get())
        val message: String? = createConnector().execFile(file)
        if (message != null) {
            println(RESULT_START)
            println(message)
            println(RESULT_END)
        }
    }
}
