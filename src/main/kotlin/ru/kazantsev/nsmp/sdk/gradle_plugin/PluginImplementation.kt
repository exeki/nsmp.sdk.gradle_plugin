package ru.kazantsev.nsmp.sdk.gradle_plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import ru.kazantsev.nsmp.sdk.gradle_plugin.connector.IntellijPluginConnector
import ru.kazantsev.nsmp.sdk.gradle_plugin.services.DependencyService
import ru.kazantsev.nsmp.sdk.gradle_plugin.services.SrcFoldersService

@Suppress("unused")
class PluginImplementation : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("groovy")
        val extension = project.extensions.create("nsmpSdk", Extension::class.java)
        project.afterEvaluate {
            applyIntegrationWithIntellij(extension, project)
            applyDependencies(extension, project)
        }
    }

    fun applyDependencies(extension: Extension, project: Project) {
        val logger = project.logger
        if (!extension.dependencyConfigurationEnabled) return
        val dependencyService = DependencyService(project)
        dependencyService.addRepositoriesToProject()
        dependencyService.addDependenciesToProject()
        logger.lifecycle("Dependencies configured")
    }

    fun applyIntegrationWithIntellij(extension: Extension, project: Project) {
        if (!extension.intellijPluginIntegrationEnabled) return
        val logger = project.logger
        logger.info("Apply IntelliJ plugin")
        val connector = IntellijPluginConnector(project, extension.port)
        if (!connector.checkPluginStatus()) {
            logger.info("IntelliJ plugin is disabled")
            return
        } else logger.info("IntelliJ plugin is enabled")
        val projectInfoResponse = connector.getProjectByPath(project.projectDir.absolutePath)
        logger.info("Project found")
        if (projectInfoResponse.project.nsmpSdk == null) {
            logger.lifecycle("IntelliJ plugin is not activated in current project")
        } else {
            val srcFoldersService = SrcFoldersService(project, projectInfoResponse.project)
            srcFoldersService.configureSourceSets()
            logger.lifecycle("Source roots configured by intellij plugin configuration")
        }
    }
}
