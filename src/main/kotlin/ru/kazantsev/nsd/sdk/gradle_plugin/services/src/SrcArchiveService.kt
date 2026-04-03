package ru.kazantsev.nsd.sdk.gradle_plugin.services.src

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.Project
import ru.kazantsev.nsd.basic_api_connector.NsdDto
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcFileDto
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcDto
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcDtoRoot
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcInfoRoot
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
        private val OBJECT_MAPPER = ObjectMapper().findAndRegisterModules()
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
            writeSourcesToArchive(zipOutputStream, scriptsRoot, "src/main/groovy/ru/naumen/scripts", scripts)
            writeSourcesToArchive(zipOutputStream, modulesRoot, "src/main/groovy/ru/naumen/modules", modules)
        }

        return outputStream.toByteArray()
    }

    /**
     * Распаковывает архив `src` в DTO с текстами и метаданными исходников.
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
                        info = OBJECT_MAPPER.readValue(String(zis.readBytes(), Charsets.UTF_8), SrcInfoRoot::class.java)
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
                    text = scriptTexts[it.code] ?: throw Exception("Не найден текст скрипта ${it.code}")
                )
            },
            modules = srcInfo.modules.map {
                SrcDto(
                    info = it,
                    text = moduleTexts[it.code] ?: throw Exception("Не найден текст модуля ${it.code}")
                )
            }
        )
    }

    /**
     * Преобразует ответ сервера с checksum'ами в корневой DTO метаданных.
     */
    fun pushChecksumsToInfoRoot(pushChecksums: NsdDto.ScriptChecksums): SrcInfoRoot {
        return OBJECT_MAPPER.convertValue(
            mapOf(
                "scripts" to pushChecksums.scripts.map {
                    mapOf(
                        "checksum" to it.checksum,
                        "code" to it.code
                    )
                },
                "modules" to pushChecksums.modules.map {
                    mapOf(
                        "checksum" to it.checksum,
                        "code" to it.code
                    )
                }
            ),
            SrcInfoRoot::class.java
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
