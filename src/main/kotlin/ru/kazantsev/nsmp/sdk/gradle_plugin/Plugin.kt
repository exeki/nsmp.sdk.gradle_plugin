package ru.kazantsev.nsmp.sdk.gradle_plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import ru.kazantsev.nsmp.sdk.gradle_plugin.services.DependencyService
import ru.kazantsev.nsmp.sdk.gradle_plugin.services.SrcFoldersService

class Plugin : Plugin<Project> {


    override fun apply(project: Project) {
        project.pluginManager.apply("groovy")
        val srcFoldersService = SrcFoldersService(project.projectDir.toPath())
        val dependencyService = DependencyService(project)

        dependencyService.addRepositoriesToProject()
        dependencyService.addDependenciesToProject()
        configureSourceSets(project, srcFoldersService)
    }

    private fun configureSourceSets(project: Project, srcFoldersService: SrcFoldersService) {
        val sourceSetContainer = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSetContainer.maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME)
        srcFoldersService.roots.forEach {
            it.create()
            main.java.srcDir(it.getRelativePath())
        }
    }
}
