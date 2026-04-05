package ru.kazantsev.nsd.sdk.gradle_plugin.services.src

import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcDto
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcFileDto
import java.io.File
import java.nio.file.Path

/**
 * Описывает один source set проекта и операции с ним.
 */
class SrcFolder(
    private val projectRootPath: Path,
    private val relativePath: String
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
        return projectRootPath.resolve(relativePath).toFile()
    }

    /**
     * Возвращает путь source root относительно проекта.
     */
    fun getRelativePath(): String {
        return relativePath
    }

    /**
     * Найти файлы исходников по кодам в source set (независимо от вложенности по папкам).
     * @param srcCodes список кодов исходников
     */
    fun findSourceFiles(srcCodes: List<String>): List<SrcFileDto> {
        val allFiles = getPath().walkTopDown().filter { it.isFile }.toList()
        return srcCodes.map { srcCode ->
            val matches = allFiles.filter { it.name == "$srcCode.groovy" }

            val file = when (matches.size) {
                0 -> throw IllegalStateException("Source file $srcCode not found in ${getPath()}")
                1 -> matches.single()
                else -> throw IllegalStateException("Several files with code $srcCode found in ${getPath()}")
            }

            SrcFileDto(srcCode, file)
        }
    }

    /**
     * Получить все файлы исходников из папки.
     */
    fun getAllSourceFiles(): List<SrcFileDto> {
        val rootDirectory = getPath()
        if (!exists()) return emptyList()

        val groovyFiles = rootDirectory.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".groovy") }
            .sortedBy { rootDirectory.toPath().relativize(it.toPath()).toString() }
            .toList()

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
     * Записать новый файл исходника.
     * @param src ДТО файла
     */
    fun writeSourceFile(src: SrcDto) {
        val packageDirectory = resolvePackageDirectory(src.text)
        val sourceFile = packageDirectory.resolve("${src.info.code}.groovy")
        sourceFile.writeText(src.text)
    }

    /**
     * Определить package исходника, чтобы сохранить его в корректной папке.
     * @param sourceText текст файла, там будем искать package
     */
    private fun resolvePackageDirectory(sourceText: String): File {
        if (relativePath.contains("src\\main\\resources")) return create()
        val packageName = PACKAGE_DECLARATION_REGEX.find(sourceText)?.groupValues?.get(1) ?: return create()
        return create().resolve(packageName.replace('.', File.separatorChar)).apply { mkdirs() }
    }
}
