package ru.kazantsev.nsd.sdk.gradle_plugin.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.gradle.api.Project
import ru.kazantsev.nsd.basic_api_connector.ConnectorParams
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcDto
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcDtoRoot
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcInfo
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcInfoRoot
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcType
import ru.kazantsev.nsd.sdk.gradle_plugin.client.nsd_connector.SdkApiConnector
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class SrcService(private val project: Project) {

    companion object {
        private const val SDK_DIR_PATH = ".nsd_sdk"
        private const val INFO_FILE_NAME = "info.json"
        private val PACKAGE_DECLARATION_REGEX =
            Regex("""(?m)^\s*package\s+([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*)\s*;?\s*$""")
        private val OBJECT_MAPPER = ObjectMapper().findAndRegisterModules()
    }

    private val logger = project.logger

    fun fetchAndStore(params: ConnectorParams, scripts: List<String>, modules: List<String>): SrcDtoRoot {
        logger.lifecycle("Fetching src: scripts={}, modules={}", scripts.size, modules.size)
        val connector = SdkApiConnector(params)
        return storeSrcArchive(connector.getSrc(scripts, modules))
    }

    fun getRemoteSrcInfo(params: ConnectorParams, scripts: List<String>, modules: List<String>): SrcInfoRoot {
        logger.lifecycle("Fetching src info: scripts={}, modules={}", scripts.size, modules.size)
        val connector = SdkApiConnector(params)
        return connector.getSrcInfo(scripts, modules)
    }

    fun compareSrcInfo(remoteSrcInfo: SrcInfoRoot, localSrcInfo: SrcInfoRoot): SrcInfoRoot {
        val changedSrcInfo = SrcInfoRoot()
        val localScriptsByCode = localSrcInfo.scripts.associateBy { it.code }
        val localModulesByCode = localSrcInfo.modules.associateBy { it.code }

        changedSrcInfo.scripts.addAll(
            remoteSrcInfo.scripts.filter { remoteInfo ->
                localScriptsByCode[remoteInfo.code]?.checksum != remoteInfo.checksum
            }
        )
        changedSrcInfo.modules.addAll(
            remoteSrcInfo.modules.filter { remoteInfo ->
                localModulesByCode[remoteInfo.code]?.checksum != remoteInfo.checksum
            }
        )

        return changedSrcInfo
    }

    fun compareRemoteSrcInfoWithLocal(params: ConnectorParams, scripts: List<String>, modules: List<String>): SrcInfoRoot {
        val remoteSrcInfo = getRemoteSrcInfo(params, scripts, modules)
        val localSrcInfo = readLocalSrcInfo()
        return compareSrcInfo(remoteSrcInfo, localSrcInfo)
    }

    fun storeSrcArchive(srcArchive: ByteArray): SrcDtoRoot {
        logger.lifecycle("Unpacking src archive")
        val srcRoot = unpackSrcArchive(srcArchive)
        val sourceSetsService = SourceSetsService(project)

        logger.lifecycle("Writing {} scripts to {}", srcRoot.scripts.size, sourceSetsService.scripts.getPath())
        srcRoot.scripts.forEach { writeSourceFile(sourceSetsService.scripts, it) }
        logger.lifecycle("Writing {} modules to {}", srcRoot.modules.size, sourceSetsService.modules.getPath())
        srcRoot.modules.forEach { writeSourceFile(sourceSetsService.modules, it) }
        logger.lifecycle("Updating {}", project.file("$SDK_DIR_PATH/$INFO_FILE_NAME"))
        updateInfoFile(srcRoot.scripts.map { it.info }, srcRoot.modules.map { it.info })

        return srcRoot
    }

    private fun unpackSrcArchive(srcArchive: ByteArray): SrcDtoRoot {
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

        val srcInfo = info ?: throw Exception("Не найден файл info.json")

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

    private fun readLocalSrcInfo(): SrcInfoRoot {
        val infoFile = project.file("$SDK_DIR_PATH/$INFO_FILE_NAME")
        if (!infoFile.exists() || infoFile.readText().isBlank()) {
            return SrcInfoRoot()
        }

        return OBJECT_MAPPER.readValue(infoFile, SrcInfoRoot::class.java)
    }

    private fun writeSourceFile(sourceRoot: SourceRoot, src: SrcDto) {
        val rootDirectory = sourceRoot.create()
        val packageDirectory = resolvePackageDirectory(rootDirectory, src.text)
        val sourceFile = packageDirectory.resolve("${src.info.code}.groovy")
        sourceFile.writeText(src.text)
    }

    private fun resolvePackageDirectory(rootDirectory: File, sourceText: String): File {
        val packageName = PACKAGE_DECLARATION_REGEX.find(sourceText)?.groupValues?.get(1) ?: return rootDirectory
        return rootDirectory.resolve(packageName.replace('.', File.separatorChar)).apply { mkdirs() }
    }

    private fun updateInfoFile(scripts: List<SrcInfo>, modules: List<SrcInfo>) {
        val sdkDir = project.file(SDK_DIR_PATH).apply { mkdirs() }
        val infoFile = sdkDir.resolve(INFO_FILE_NAME)
        if (!infoFile.exists()) {
            infoFile.createNewFile()
        }

        val rootObject = if (infoFile.readText().isBlank()) {
            OBJECT_MAPPER.createObjectNode()
        } else {
            OBJECT_MAPPER.readTree(infoFile) as ObjectNode
        }

        val updatedRoot = OBJECT_MAPPER.createObjectNode().apply {
            set<ArrayNode>("scripts", OBJECT_MAPPER.createArrayNode().apply {
                val byCode = linkedMapOf<String, JsonNode>()
                rootObject.get("scripts")?.forEach { element ->
                    val code = element.path("code").asText(null) ?: return@forEach
                    byCode[code] = element
                }
                scripts.forEach { info ->
                    byCode[info.code] = OBJECT_MAPPER.valueToTree(info)
                }
                byCode.values.forEach { add(it) }
            })
            set<ArrayNode>("modules", OBJECT_MAPPER.createArrayNode().apply {
                val byCode = linkedMapOf<String, JsonNode>()
                rootObject.get("modules")?.forEach { element ->
                    val code = element.path("code").asText(null) ?: return@forEach
                    byCode[code] = element
                }
                modules.forEach { info ->
                    byCode[info.code] = OBJECT_MAPPER.valueToTree(info)
                }
                byCode.values.forEach { add(it) }
            })
        }

        OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(infoFile, updatedRoot)
    }
}
