package ru.kazantsev.nsd.sdk.gradle_plugin.services

import org.gradle.api.Project
import java.io.File

class SourceRoot(
    private val project: Project,
    private val path: String
) {
    fun create(): File {
        val root = getPath()
        root.mkdirs()
        return root
    }

    fun exists(): Boolean {
        return getPath().exists()
    }

    fun getPath(): File {
        return project.file(path)
    }

    fun getRelativePath(): String {
        return path
    }
}
