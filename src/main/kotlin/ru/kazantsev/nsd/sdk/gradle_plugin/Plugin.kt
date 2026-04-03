package ru.kazantsev.nsd.sdk.gradle_plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskProvider
import ru.kazantsev.nsd.sdk.gradle_plugin.services.DependencyService
import ru.kazantsev.nsd.sdk.gradle_plugin.services.src.SrcFoldersService
import ru.kazantsev.nsd.sdk.gradle_plugin.tasks.CreateConsoleFileTask
import ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote.CheckSrcTask
import ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote.FetchSrcTask
import ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote.RemoteNsdTask
import ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote.PushSrcTask
import ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote.SendScriptTask

class Plugin : Plugin<Project> {

    companion object {
        private const val DEFAULT_CONSOLE_FILE_PATH = "src/main/groovy/console.groovy"
    }

    override fun apply(project: Project) {
        project.pluginManager.apply("groovy")

        val extension = project.extensions.create("smpSdk", Extension::class.java)
        val providers = project.providers
        val srcFoldersService = SrcFoldersService(project)
        val dependencyService = DependencyService(project)

        dependencyService.addRepositoriesToProject()
        dependencyService.addDevDependenciesToProject()
        srcFoldersService.configureSourceSets()

        project.tasks.register(
            CreateConsoleFileTask.NAME,
            CreateConsoleFileTask::class.java
        ) {
            it.consoleFilePath.convention(
                providers.provider { extension.sendFilePath ?: DEFAULT_CONSOLE_FILE_PATH }
            )
        }

        project.tasks.register(
            SendScriptTask.NAME,
            SendScriptTask::class.java
        ).configureRemote(extension, providers) {
            filePath.convention(extension.sendFilePath)
        }

        project.tasks.register(
            FetchSrcTask.NAME,
            FetchSrcTask::class.java
        ).configureRemote(extension, providers) {
            scripts.convention("")
            modules.convention("")
        }

        project.tasks.register(
            CheckSrcTask.NAME,
            CheckSrcTask::class.java
        ).configureRemote(extension, providers) {
            scripts.convention("")
            modules.convention("")
        }

        project.tasks.register(
            PushSrcTask.NAME,
            PushSrcTask::class.java
        ).configureRemote(extension, providers) {
            //scripts.convention("")
            //modules.convention("")
            force.convention(false)
        }
    }
}

private fun <T : RemoteNsdTask> TaskProvider<T>.configureRemote(
    extension: Extension,
    providers: ProviderFactory,
    additional: T.() -> Unit = {}
): TaskProvider<T> {
    configure {
        it.doNotTrackState("This task must always run")
        it.connectorParamsProvider = providers.provider { extension.installation?.connectorParams }
        it.additional()
    }
    return this
}
