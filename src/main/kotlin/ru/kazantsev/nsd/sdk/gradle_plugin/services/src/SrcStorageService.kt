package ru.kazantsev.nsd.sdk.gradle_plugin.services.src

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.gradle.api.Project
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcInfo
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcInfoRoot
import java.io.File

/**
 * Сервис для локального хранилища метаданных `src` в `.smp_sdk/src_info.json`.
 */
class SrcStorageService(private val project: Project) {
    companion object {
        private const val SDK_DIR_PATH = ".smp_sdk"
        private const val INFO_FILE_NAME = "src_info.json"
        private val OBJECT_MAPPER = ObjectMapper().findAndRegisterModules()
    }

    private val infoFilePath: File
        get() = project.file("$SDK_DIR_PATH/$INFO_FILE_NAME")

    /**
     * Возвращает файл локального хранилища метаданных.
     */
    fun getInfoFile(): File = infoFilePath

    /**
     * Читает локальную информацию об исходниках.
     *
     * При необходимости фильтрует данные по кодам scripts/modules.
     */
    fun readLocalSrcInfo(
        scriptsFilter: Collection<String> = emptyList(),
        modulesFilter: Collection<String> = emptyList()
    ): SrcInfoRoot {
        if (!infoFilePath.exists() || infoFilePath.readText().isBlank()) {
            return SrcInfoRoot()
        }

        val srcInfo = OBJECT_MAPPER.readValue(infoFilePath, SrcInfoRoot::class.java)
        if (scriptsFilter.isEmpty() && modulesFilter.isEmpty()) {
            return srcInfo
        }

        return SrcInfoRoot().apply {
            scripts.addAll(srcInfo.scripts.filter { scriptsFilter.isEmpty() || it.code in scriptsFilter })
            modules.addAll(srcInfo.modules.filter { modulesFilter.isEmpty() || it.code in modulesFilter })
        }
    }

    /**
     * Обновляет локальный файл метаданных, объединяя старые и новые записи по коду.
     */
    fun updateInfoFile(scripts: List<SrcInfo>, modules: List<SrcInfo>) {
        val sdkDir = project.file(SDK_DIR_PATH).apply { mkdirs() }
        val currentInfoFile = sdkDir.resolve(INFO_FILE_NAME)
        if (!currentInfoFile.exists()) {
            currentInfoFile.createNewFile()
        }

        val rootObject = if (currentInfoFile.readText().isBlank()) {
            OBJECT_MAPPER.createObjectNode()
        } else {
            OBJECT_MAPPER.readTree(currentInfoFile) as ObjectNode
        }

        val updatedRoot = OBJECT_MAPPER.createObjectNode().apply {
            set<ArrayNode>("scripts", mergeEntries(rootObject.get("scripts"), scripts))
            set<ArrayNode>("modules", mergeEntries(rootObject.get("modules"), modules))
        }

        OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(currentInfoFile, updatedRoot)
    }

    private fun mergeEntries(existingEntries: JsonNode?, incomingEntries: List<SrcInfo>): ArrayNode {
        val result = OBJECT_MAPPER.createArrayNode()
        val byCode = linkedMapOf<String, JsonNode>()

        existingEntries?.forEach { element ->
            val code = element.path("code").asText(null) ?: return@forEach
            byCode[code] = element
        }

        incomingEntries.forEach { info ->
            byCode[info.code] = OBJECT_MAPPER.valueToTree(info)
        }

        byCode.values.forEach { result.add(it) }
        return result
    }
}
