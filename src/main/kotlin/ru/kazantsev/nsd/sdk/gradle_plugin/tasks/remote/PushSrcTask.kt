package ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import ru.kazantsev.nsd.sdk.gradle_plugin.services.src.SrcService

abstract class PushSrcTask : RemoteNsdTask() {

    companion object {
        const val NAME = "push_src"
    }

    @get:Input
    @get:Optional
    @get:Option(option = "scripts", description = "Script codes to push (optional)")
    abstract val scripts: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "modules", description = "Module codes to push (optional)")
    abstract val modules: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "force", description = "Skip src checksum validation")
    abstract val force: Property<Boolean>

    init {
        description = "Pushes local sources to SMP. Use --scripts and --modules, for example: --scripts=a,b --modules=c,d"
    }

    @TaskAction
    fun action() {
        val srcService = SrcService(project)
        srcService.pushAndStore(
            createConnector(),
            parseCsvOption(scripts.orNull),
            parseCsvOption(modules.orNull),
            force.orNull == true
        )
    }
}
