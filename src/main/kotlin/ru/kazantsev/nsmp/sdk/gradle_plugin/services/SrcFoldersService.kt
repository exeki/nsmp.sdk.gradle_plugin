package ru.kazantsev.nsmp.sdk.gradle_plugin.services

import java.io.FileWriter
import java.nio.file.Path

/**
 * Сервис, который описывает и создаёт стандартные source root плагина.
 */
class SrcFoldersService(private val projectRootPath: Path) {
    companion object {
        const val SCRIPT_SOURCE_SET_PATH: String = "src\\main\\scripts"
        const val GROOVY_SOURCE_SET_PATH: String = "src\\main\\groovy"
        const val MODULES_SOURCE_SET_PATH: String = "src\\main\\modules"

    }

    val groovy = SrcFolder(projectRootPath, GROOVY_SOURCE_SET_PATH)
    val modules = SrcFolder(projectRootPath, MODULES_SOURCE_SET_PATH)
    val scripts = SrcFolder(projectRootPath, SCRIPT_SOURCE_SET_PATH)
    val roots: Set<SrcFolder> = setOf(groovy, modules, scripts)
}