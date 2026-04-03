package ru.kazantsev.nsd.sdk.gradle_plugin.services.src

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.Project
import ru.kazantsev.nsd.basic_api_connector.NsdDto
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcFileDto
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcDto
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcDtoRoot
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcInfoRoot
import ru.kazantsev.nsd.sdk.gradle_plugin.services.Utilities
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Сервис для работы с архивом `src`: упаковка, распаковка и преобразование checksum-ответа.
 */
class SrcArchiveService(private val project: Project) {
    companion object {
        private const val SRC_PUSH_ARCHIVE_ROOT = "src/main/groovy/ru/naumen"
    }

    private val logger = project.logger

    /**
     * Собирает zip-архив из локальных source root.
     */
    fun buildSrcArchive(
        scripts: List<SrcFileDto>,
        modules: List<SrcFileDto>,
        scriptsRoot: SrcFolder,
        modulesRoot: SrcFolder
    ): ByteArray {
        val outputStream = ByteArrayOutputStream()

        ZipOutputStream(outputStream).use { zipOutputStream ->
            writeSourcesToArchive(zipOutputStream, scriptsRoot, "$SRC_PUSH_ARCHIVE_ROOT/scripts", scripts)
            writeSourcesToArchive(zipOutputStream, modulesRoot, "$SRC_PUSH_ARCHIVE_ROOT/modules", modules)
        }

        return outputStream.toByteArray()
    }

    /**
     * Распаковывает архив с исходниками в DTO с текстами и метаданными исходников.
     */
    fun unpackSrcArchive(srcArchive: ByteArray): SrcDtoRoot {
        val scriptTexts = mutableMapOf<String, String>()
        val moduleTexts = mutableMapOf<String, String>()
        var info: SrcInfoRoot? = null

        ZipInputStream(ByteArrayInputStream(srcArchive)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                logger.lifecycle("Src archive entry: {}", entry.name)
                if (entry.isDirectory) {
                    entry = zis.nextEntry
                    continue
                }

                val normalizedEntryName = entry.name.replace('\\', '/')

                when {
                    normalizedEntryName.startsWith("modules/") -> {
                        val code = normalizedEntryName.substringAfterLast('/').substringBefore('.')
                        moduleTexts[code] = String(zis.readBytes(), Charsets.UTF_8)
                    }

                    normalizedEntryName.startsWith("scripts/") -> {
                        val code = normalizedEntryName.substringAfterLast('/').substringBefore('.')
                        scriptTexts[code] = String(zis.readBytes(), Charsets.UTF_8)
                    }

                    normalizedEntryName == "info.json" -> {
                        info = Utilities.objectMapper.readValue(
                            String(zis.readBytes(), Charsets.UTF_8),
                            SrcInfoRoot::class.java
                        )
                    }
                }
                entry = zis.nextEntry
            }
        }

        val srcInfo = info ?: throw Exception("File \"info.json\" not found")

        return SrcDtoRoot(
            scripts = srcInfo.scripts.map {
                SrcDto(
                    info = it,
                    text = scriptTexts[it.code] ?: throw Exception("Script text ${it.code} not found")
                )
            },
            modules = srcInfo.modules.map {
                SrcDto(
                    info = it,
                    text = moduleTexts[it.code] ?: throw Exception("Module test ${it.code} not found")
                )
            }
        )
    }

    private fun writeSourcesToArchive(
        zipOutputStream: ZipOutputStream,
        srcFolder: SrcFolder,
        archiveRoot: String,
        sources: List<SrcFileDto>
    ) {
        sources.forEach { source ->
            val relativePath = srcFolder.getPath().toPath().relativize(source.file.toPath()).toString()
                .replace(File.separatorChar, '/')
            val entryName = "$archiveRoot/$relativePath"

            zipOutputStream.putNextEntry(ZipEntry(entryName))
            source.file.inputStream().use { inputStream ->
                inputStream.copyTo(zipOutputStream)
            }
            zipOutputStream.closeEntry()
        }
    }
}
