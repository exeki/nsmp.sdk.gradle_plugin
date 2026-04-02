package ru.kazantsev.nsd.sdk.gradle_plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import ru.kazantsev.nsd.sdk.gradle_plugin.services.DependencyService
import ru.kazantsev.nsd.sdk.gradle_plugin.services.SourceSetsService
import ru.kazantsev.nsd.sdk.gradle_plugin.tasks.CreateConsoleFileTask
import ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote.CheckSrcTask
import ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote.FetchSrcTask
import ru.kazantsev.nsd.sdk.gradle_plugin.tasks.remote.SendScriptTask

class Plugin : Plugin<Project> {

    companion object {
        private const val DEFAULT_CONSOLE_FILE_PATH = "src/main/groovy/console.groovy"
    }

    override fun apply(project: Project) {
        project.pluginManager.apply("groovy")

        val extension = project.extensions.create("sdk", Extension::class.java)
        val sourceSetsService = SourceSetsService(project)
        val dependencyService = DependencyService(project)

        dependencyService.addRepositoriesToProject()
        dependencyService.addDevDependenciesToProject()
        sourceSetsService.configureSourceSets()

        val createConsoleFile = project.tasks.register(
            CreateConsoleFileTask.NAME,
            CreateConsoleFileTask::class.java
        ) {
            it.consoleFilePath.convention(
                project.provider { extension.sendFilePath ?: DEFAULT_CONSOLE_FILE_PATH }
            )
        }

        project.tasks.register(
            SendScriptTask.NAME,
            SendScriptTask::class.java
        ) {
            it.filePath.convention(extension.sendFilePath)
            it.installationId.convention(project.provider {
                extension.installation?.connectorParams?.installationId
            })
            it.scheme.convention(project.provider {
                extension.installation?.connectorParams?.scheme
            })
            it.host.convention(project.provider {
                extension.installation?.connectorParams?.host
            })
            it.accessKey.convention(project.provider {
                extension.installation?.connectorParams?.accessKey
            })
            it.ignoreSsl.convention(project.provider {
                extension.installation?.connectorParams?.isIgnoringSSL
            })
        }

        project.tasks.register(
            FetchSrcTask.NAME,
            FetchSrcTask::class.java
        ) {
            it.scripts.convention(emptyList())
            it.modules.convention(emptyList())
            it.installationId.convention(project.provider {
                extension.installation?.connectorParams?.installationId
            })
            it.scheme.convention(project.provider {
                extension.installation?.connectorParams?.scheme
            })
            it.host.convention(project.provider {
                extension.installation?.connectorParams?.host
            })
            it.accessKey.convention(project.provider {
                extension.installation?.connectorParams?.accessKey
            })
            it.ignoreSsl.convention(project.provider {
                extension.installation?.connectorParams?.isIgnoringSSL
            })
        }

        project.tasks.register(
            CheckSrcTask.NAME,
            CheckSrcTask::class.java
        ) {
            it.scripts.convention(emptyList())
            it.modules.convention(emptyList())
            it.installationId.convention(project.provider {
                extension.installation?.connectorParams?.installationId
            })
            it.scheme.convention(project.provider {
                extension.installation?.connectorParams?.scheme
            })
            it.host.convention(project.provider {
                extension.installation?.connectorParams?.host
            })
            it.accessKey.convention(project.provider {
                extension.installation?.connectorParams?.accessKey
            })
            it.ignoreSsl.convention(project.provider {
                extension.installation?.connectorParams?.isIgnoringSSL
            })
        }
    }
}
