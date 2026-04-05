package ru.kazantsev.nsd.sdk.gradle_plugin.services.src

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcInfo
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcInfoRoot
import ru.kazantsev.nsd.sdk.gradle_plugin.services.Utilities
import java.io.File
import java.nio.file.Path

/**
 * Сервис для локального хранилища метаданных `src` в `.smp_sdk/src_info.json`.
 */
class SrcStorageService(private val projectRootPath: Path) {
    companion object {
        private const val SDK_DIR_PATH = ".smp_sdk"
        private const val INFO_FILE_NAME = "src_info.json"
    }

    private val infoFilePath: File
        get() = projectRootPath.resolve(SDK_DIR_PATH).resolve(INFO_FILE_NAME).toFile()

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

        val srcInfo = Utilities.objectMapper.readValue(infoFilePath, SrcInfoRoot::class.java)
        if (scriptsFilter.isEmpty() && modulesFilter.isEmpty()) {
            return srcInfo
        }

        return SrcInfoRoot(
            scripts = srcInfo.scripts.filter { scriptsFilter.isEmpty() || it.code in scriptsFilter },
            modules = srcInfo.modules.filter { modulesFilter.isEmpty() || it.code in modulesFilter }
        )
    }

    /**
     * Обновляет локальный файл метаданных, объединяя старые и новые записи по коду.
     */
    fun updateInfoFile(scripts: List<SrcInfo>, modules: List<SrcInfo>) {
        val sdkDir = projectRootPath.resolve(SDK_DIR_PATH).toFile().apply { mkdirs() }
        val currentInfoFile = sdkDir.resolve(INFO_FILE_NAME)
        if (!currentInfoFile.exists()) {
            currentInfoFile.createNewFile()
        }

        val rootObject = if (currentInfoFile.readText().isBlank()) {
            Utilities.objectMapper.createObjectNode()
        } else {
            Utilities.objectMapper.readTree(currentInfoFile) as ObjectNode
        }

        val updatedRoot = Utilities.objectMapper.createObjectNode().apply {
            set<ArrayNode>("scripts", mergeEntries(rootObject.get("scripts"), scripts))
            set<ArrayNode>("modules", mergeEntries(rootObject.get("modules"), modules))
        }

        Utilities.objectMapper.writerWithDefaultPrettyPrinter().writeValue(currentInfoFile, updatedRoot)
    }

    private fun mergeEntries(existingEntries: JsonNode?, incomingEntries: List<SrcInfo>): ArrayNode {
        val result = Utilities.objectMapper.createArrayNode()
        val byCode = linkedMapOf<String, JsonNode>()

        existingEntries?.forEach { element ->
            val code = element.path("code").asText(null) ?: return@forEach
            byCode[code] = element
        }

        incomingEntries.forEach { info ->
            byCode[info.code] = Utilities.objectMapper.valueToTree(info)
        }

        byCode.values.forEach { result.add(it) }
        return result
    }
}
