package ru.kazantsev.nsd.sdk.gradle_plugin.services.src

import org.gradle.api.Project
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcDto
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcFileDto
import java.io.File

/**
 * Описывает один source set проекта и операции c ним
 */
class SrcFolder(
    private val project: Project,
    private val path: String
) {

    companion object {
        private val PACKAGE_DECLARATION_REGEX =
            Regex("""(?m)^\s*package\s+([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*)\s*;?\s*$""")
    }

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
        return project.file(path)
    }

    /**
     * Возвращает путь source root относительно проекта
     */
    fun getRelativePath(): String {
        return path
    }

    /**
     * Найти файлы исходников по кодам в source set (независимо от вложенности по папкам)
     * @param srcCodes список кодов исходников
     */
    fun findSourceFiles(srcCodes: List<String>): List<SrcFileDto> {
        val allFiles = project.fileTree(getPath()).files
        return srcCodes.map { srcCode ->
            val matches = allFiles.filter {
                it.isFile && it.name == "$srcCode.groovy"
            }

            val file = when (matches.size) {
                0 -> throw IllegalStateException("Source file $srcCode not found in ${getPath()}")
                1 -> matches.single()
                else -> throw IllegalStateException("Several files with code $srcCode found in ${getPath()}")
            }

            SrcFileDto(srcCode, file)
        }
    }

    /**
     * Получить все файлы исходников из папки
     */
    fun getAllSourceFiles(): List<SrcFileDto> {
        val rootDirectory = getPath()
        if (!exists()) return emptyList()

        val groovyFiles = project.fileTree(path).files
            .filter { it.isFile && it.name.endsWith(".groovy") }
            .sortedBy { rootDirectory.toPath().relativize(it.toPath()).toString() }

        val groupedByCode = groovyFiles.groupBy { it.name.substringBeforeLast(".groovy") }
        val duplicatedCodes = groupedByCode.filterValues { it.size > 1 }.keys
        if (duplicatedCodes.isNotEmpty()) {
            throw IllegalStateException("Several files with code $duplicatedCodes found in ${getPath()}")
        }

        return groovyFiles.map { file ->
            SrcFileDto(file.name.substringBeforeLast(".groovy"), file)
        }
    }

    /**
     * Записать новый файл исходника
     * @param src - ДТО файла
     */
    fun writeSourceFile(src: SrcDto) {
        val packageDirectory = resolvePackageDirectory(src.text)
        val sourceFile = packageDirectory.resolve("${src.info.code}.groovy")
        sourceFile.writeText(src.text)
    }

    /**
     * Определить package исходника, что бы сохранить его в корректной папке
     * @param sourceText текст файла, там будем искать package
     */
    private fun resolvePackageDirectory(sourceText: String): File {
        if (path.contains("src\\main\\resources")) return create()
        val packageName = PACKAGE_DECLARATION_REGEX.find(sourceText)?.groupValues?.get(1) ?: return create()
        return create().resolve(packageName.replace('.', File.separatorChar)).apply { mkdirs() }
    }
}
