package ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import ru.kazantsev.nsd.sdk.gradle_plugin.services.src.SrcService

abstract class FetchSrcTask : RemoteNsdTask() {

    companion object {
        const val NAME = "fetch_src"
    }

    @get:Input
    @get:Optional
    @get:Option(option = "scripts", description = "Script codes to fetch")
    abstract val scripts: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "modules", description = "Module codes to fetch")
    abstract val modules: Property<String>

    init {
        description = "Fetches sources from SMP and stores them in project source sets. Use --scripts and --modules, for example: --scripts=a,b --modules=c,d"
    }

    @TaskAction
    fun action() {
        val requestedScripts = parseCsvOption(scripts.orNull)
        val requestedModules = parseCsvOption(modules.orNull)
        requireRequestedSources(requestedScripts, requestedModules)

        val srcService = SrcService(project.projectDir.toPath())
        srcService.fetchAndStore(
            createConnector(),
            requestedScripts,
            requestedModules
        )
    }
}
