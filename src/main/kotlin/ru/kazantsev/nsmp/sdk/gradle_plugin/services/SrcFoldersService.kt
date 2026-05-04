package ru.kazantsev.nsmp.sdk.gradle_plugin.services

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import ru.kazantsev.nsmp.sdk.gradle_plugin.connector.dto.ProjectInfoDto

/**
 * Сервис, который описывает и создаёт стандартные source root плагина.
 */
class SrcFoldersService(
    private val project: Project,
    val srcFoldersParamsDto: ProjectInfoDto
) {

    fun configureSourceSets() {
        val nsmpSdkData = srcFoldersParamsDto.nsmpSdk ?: return
        val sourceSetContainer = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSetContainer.maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME)
        main.java.srcDir(nsmpSdkData.srcFoldersParams.scriptsRelativePath)
        main.java.srcDir(nsmpSdkData.srcFoldersParams.modulesRelativePath)
        main.resources.srcDir(nsmpSdkData.srcFoldersParams.advImportsRelativePath)
    }
}