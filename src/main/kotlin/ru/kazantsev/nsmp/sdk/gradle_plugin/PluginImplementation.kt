package ru.kazantsev.nsmp.sdk.gradle_plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import ru.kazantsev.nsmp.sdk.gradle_plugin.services.DependencyService
import ru.kazantsev.nsmp.sdk.gradle_plugin.services.SrcFoldersService

class PluginImplementation : Plugin<Project> {


    override fun apply(project: Project) {
        project.pluginManager.apply("groovy")

        val srcFoldersService = SrcFoldersService(project)
        val dependencyService = DependencyService(project)

        dependencyService.addRepositoriesToProject()
        dependencyService.addDependenciesToProject()
        srcFoldersService.configureSourceSets()
    }
}
