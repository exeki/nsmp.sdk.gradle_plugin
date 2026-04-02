package ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import ru.kazantsev.nsd.sdk.gradle_plugin.services.SrcService

abstract class CheckSrcTask : RemoteNsdTask() {

    companion object {
        const val NAME = "check_src"
    }

    @get:Input
    @get:Option(option = "scripts", description = "Script codes to check")
    abstract val scripts: ListProperty<String>

    @get:Input
    @get:Option(option = "modules", description = "Module codes to check")
    abstract val modules: ListProperty<String>

    init {
        description = "Checks remote src info against local checksums"
    }

    @TaskAction
    fun action() {
        val srcService = SrcService(project)
        val diff = srcService.compareRemoteSrcInfoWithLocal(
            createConnectorParams(),
            scripts.getOrElse(emptyList()),
            modules.getOrElse(emptyList())
        )

        if (diff.scripts.isEmpty() && diff.modules.isEmpty()) {
            logger.lifecycle("No src checksum differences found")
            return
        }

        diff.scripts.forEach { logger.lifecycle("Changed script: {}", it.code) }
        diff.modules.forEach { logger.lifecycle("Changed module: {}", it.code) }
    }
}
