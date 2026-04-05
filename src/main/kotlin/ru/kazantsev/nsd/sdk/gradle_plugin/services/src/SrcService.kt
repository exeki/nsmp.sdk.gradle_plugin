package ru.kazantsev.nsd.sdk.gradle_plugin.services.src

import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcDtoRoot
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcFileDto
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcInfo
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcInfoRoot
import ru.kazantsev.nsd.sdk.gradle_plugin.client.nsd_connector.SdkApiConnector
import java.nio.file.Path

/**
 * Сервис, который оркестрирует работу с исходниками NSD.
 */
class SrcService(private val projectRootPath: Path) {
    val srcFoldersService = SrcFoldersService(projectRootPath)
    val srcStorageService = SrcStorageService(projectRootPath)
    val srcArchiveService = SrcArchiveService()
    val srcChecksumService = SrcChecksumService()

    /**
     * Загружает исходники с сервера и сохраняет их в локальные source sets.
     */
    fun fetchAndStore(connector: SdkApiConnector, scripts: List<String>, modules: List<String>): SrcDtoRoot {
        val srcArchive = connector.getSrc(scripts, modules)
        val srcRoot = srcArchiveService.unpackSrcArchive(srcArchive)
        srcRoot.scripts.forEach { srcFoldersService.scripts.writeSourceFile(it) }
        srcRoot.modules.forEach { srcFoldersService.modules.writeSourceFile(it) }
        srcStorageService.updateInfoFile(srcRoot.scripts.map { it.info }, srcRoot.modules.map { it.info })
        return srcRoot
    }

    /**
     * Получает с сервера актуальную информацию о checksum'ах исходников.
     */
    fun getRemoteSrcInfo(connector: SdkApiConnector, scripts: List<String>, modules: List<String>): SrcInfoRoot {
        return connector.getSrcInfo(scripts, modules)
    }

    /**
     * Получает checksum'и с сервера и сравнивает их с локальным хранилищем.
     */
    fun compareRemoteSrcInfoWithLocal(
        connector: SdkApiConnector,
        scripts: List<String>,
        modules: List<String>
    ): SrcInfoRoot {
        val effectiveScripts = scripts.ifEmpty { srcFoldersService.scripts.getAllSourceFiles().map { it.code } }
        val effectiveModules = modules.ifEmpty { srcFoldersService.modules.getAllSourceFiles().map { it.code } }
        val remoteSrcInfo = getRemoteSrcInfo(connector, effectiveScripts, effectiveModules)
        val localSrcInfo = srcStorageService.readLocalSrcInfo(scripts, modules)
        return srcChecksumService.compareSrcInfo(remoteSrcInfo, localSrcInfo)
    }

    /**
     * Собирает локальные исходники, проверяет их checksum'и и отправляет на сервер.
     */
    fun pushAndStore(
        connector: SdkApiConnector,
        scripts: List<String>,
        modules: List<String>,
        force: Boolean
    ): SrcInfoRoot {
        val requestedScripts: List<SrcFileDto>
        val requestedModules: List<SrcFileDto>
        if (scripts.isEmpty() && modules.isEmpty()) {
            requestedScripts = srcFoldersService.scripts.getAllSourceFiles()
            requestedModules = srcFoldersService.modules.getAllSourceFiles()
        } else {
            requestedScripts = srcFoldersService.scripts.findSourceFiles(scripts)
            requestedModules = srcFoldersService.modules.findSourceFiles(modules)
        }
        if (requestedScripts.isEmpty() && requestedModules.isEmpty()) {
            throw IllegalStateException("Не найдены исходники для выполнения загрузки на инсталляцию")
        }

        if (!force) {
            val requestedScriptCodes = requestedScripts.map { it.code }
            val requestedModuleCodes = requestedModules.map { it.code }
            val remoteSrcInfo = getRemoteSrcInfo(connector, requestedScriptCodes, requestedModuleCodes)
            val localSrcInfo = srcStorageService.readLocalSrcInfo(requestedScriptCodes, requestedModuleCodes)
            if (localSrcInfo.scripts.isNotEmpty() || localSrcInfo.modules.isNotEmpty()) {
                val diff = srcChecksumService.compareSrcInfo(remoteSrcInfo, localSrcInfo)
                if (diff.scripts.isNotEmpty() || diff.modules.isNotEmpty()) {
                    throw IllegalStateException(
                        buildString {
                            append("Src check failed. Changed scripts=")
                            append(diff.scripts.map { it.code })
                            append(", changed modules=")
                            append(diff.modules.map { it.code })
                        }
                    )
                }
            }
        }

        val srcArchive = srcArchiveService.buildSrcArchive(
            requestedScripts,
            requestedModules,
            srcFoldersService.scripts,
            srcFoldersService.modules
        )
        val pushedSourcesInfo = connector.pushScripts(srcArchive)
        val pushedInfo = SrcInfoRoot(
            scripts = pushedSourcesInfo.scripts.map { SrcInfo(it.checksum, it.code) }.toMutableList(),
            modules = pushedSourcesInfo.modules.map { SrcInfo(it.checksum, it.code) }.toMutableList()
        )
        srcStorageService.updateInfoFile(
            pushedInfo.scripts,
            pushedInfo.modules
        )
        return pushedInfo
    }
}
