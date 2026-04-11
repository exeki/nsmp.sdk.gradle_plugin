package ru.kazantsev.nsmp.sdk.gradle_plugin.services

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

/**
 * Сервис, который описывает и создаёт стандартные source root плагина.
 */
class SrcFoldersService(private val project: Project) {
    companion object {
        //const val GROOVY_SOURCE_SET_PATH: String = "src\\main\\groovy"
        const val MODULES_SOURCE_SET_PATH: String = "src\\main\\modules"
        const val SCRIPT_SOURCE_SET_PATH: String = "src\\main\\scripts"
    }

    val roots: Set<String> = setOf(SCRIPT_SOURCE_SET_PATH, MODULES_SOURCE_SET_PATH)

    fun configureSourceSets() {
        val sourceSetContainer = project.extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSetContainer.maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME)
        roots.forEach {
            main.java.srcDir(it)
        }
    }
}