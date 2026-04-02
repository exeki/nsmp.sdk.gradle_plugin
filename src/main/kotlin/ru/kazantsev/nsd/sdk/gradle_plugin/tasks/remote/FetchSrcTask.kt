package ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import ru.kazantsev.nsd.sdk.gradle_plugin.services.SrcService

abstract class FetchSrcTask : RemoteNsdTask() {

    companion object {
        const val NAME = "fetch_src"
    }

    @get:Input
    @get:Option(option = "scripts", description = "Script codes to fetch")
    abstract val scripts: ListProperty<String>

    @get:Input
    @get:Option(option = "modules", description = "Module codes to fetch")
    abstract val modules: ListProperty<String>

    init {
        description = "Fetches scripts and modules from NSD and stores them in source roots"
    }

    @TaskAction
    fun action() {
        val srcService = SrcService(project)
        srcService.fetchAndStore(createConnectorParams(), scripts.getOrElse(emptyList()), modules.getOrElse(emptyList()))
    }
}
