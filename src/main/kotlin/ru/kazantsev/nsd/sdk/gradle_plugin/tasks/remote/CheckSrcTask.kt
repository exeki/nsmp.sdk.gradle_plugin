package ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcCodesDto
import ru.kazantsev.nsd.sdk.gradle_plugin.services.src.SrcService

abstract class CheckSrcTask : RemoteNsdTask() {

    companion object {
        const val NAME = "check_src"
    }

    @get:Input
    @get:Optional
    @get:Option(option = "scripts", description = "Script codes to check (optional)")
    abstract val scripts: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "modules", description = "Module codes to check  ")
    abstract val modules: Property<String>

    init {
        description = "Checks remote src checksums against local checksums. Use --scripts and --modules, for example: --scripts=a,b --modules=c,d"
    }

    @TaskAction
    fun action() {
        val requestedScripts = parseCsvOption(scripts.orNull)
        val requestedModules = parseCsvOption(modules.orNull)
        val srcService = SrcService(project.projectDir.toPath())

        val diff = srcService.compareRemoteSrcInfoWithLocal(
            createConnector(),
            requestedScripts,
            requestedModules
        )

        if (diff.scripts.isEmpty() && diff.modules.isEmpty()) {
            logger.lifecycle("No src checksum differences found")
            return
        }

        diff.scripts.forEach { logger.lifecycle("Changed script: {}", it.code) }
        diff.modules.forEach { logger.lifecycle("Changed module: {}", it.code) }
    }
}
