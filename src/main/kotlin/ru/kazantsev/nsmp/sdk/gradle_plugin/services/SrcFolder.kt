package ru.kazantsev.nsmp.sdk.gradle_plugin.services

import java.io.File
import java.nio.file.Path

/**
 * Описывает один source set проекта и операции с ним.
 */
class SrcFolder(
    private val projectRootPath: Path,
    private val relativePath: String
) {

    init {
        create()
    }

    /**
     * Создаёт папку source root, если её ещё нет, и возвращает её путь.
     */
    fun create(): File {
        val root = getPath()
        root.mkdirs()
        return root
    }

    /**
     * Проверяет, существует ли source root.
     */
    fun exists(): Boolean {
        return getPath().exists()
    }

    /**
     * Возвращает абсолютный путь к source root.
     */
    fun getPath(): File {
        return projectRootPath.resolve(relativePath).toFile()
    }

    /**
     * Возвращает путь source root относительно проекта.
     */
    fun getRelativePath(): String {
        return relativePath
    }

}