package ru.kazantsev.nsd.sdk.gradle_plugin.services.src

import org.gradle.api.Project
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcCodesDto
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcDtoRoot
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcFileDto
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcInfoRoot
import ru.kazantsev.nsd.sdk.gradle_plugin.client.nsd_connector.SdkApiConnector

/**
 * Сервис, который оркестрирует работу с исходниками NSD.
 */
class SrcService(private val project: Project) {


    private val logger = project.logger
    val srcFoldersService = SrcFoldersService(project)
    val srcStorageService = SrcStorageService(project)
    val srcArchiveService = SrcArchiveService(project)
    val srcChecksumService = SrcChecksumService()

    /**
     * Загружает исходникии с сервера и сохраняет их в локальные source sets.
     */
    fun fetchAndStore(connector: SdkApiConnector, scripts: List<String>, modules: List<String>): SrcDtoRoot {
        logger.lifecycle("Fetching src: scripts={}, modules={}", scripts.size, modules.size)
        val srcArchive = connector.getSrc(scripts, modules)
        logger.lifecycle("Unpacking src archive")
        val srcRoot = srcArchiveService.unpackSrcArchive(srcArchive)
        logger.lifecycle("Writing {} scripts to {}", srcRoot.scripts.size, srcFoldersService.scripts.getPath())
        srcRoot.scripts.forEach { srcFoldersService.scripts.writeSourceFile(it) }
        logger.lifecycle("Writing {} modules to {}", srcRoot.modules.size, srcFoldersService.modules.getPath())
        srcRoot.modules.forEach { srcFoldersService.modules.writeSourceFile(it) }
        logger.lifecycle("Updating {}", srcStorageService.getInfoFile())
        srcStorageService.updateInfoFile(srcRoot.scripts.map { it.info }, srcRoot.modules.map { it.info })
        return srcRoot
    }

    /**
     * Получает с сервера актуальную информацию о checksum'ах исходников.
     */
    fun getRemoteSrcInfo(connector: SdkApiConnector, scripts: List<String>, modules: List<String>): SrcInfoRoot {
        logger.lifecycle("Fetching src info: scripts={}, modules={}", scripts.size, modules.size)
        return connector.getSrcInfo(scripts, modules)
    }

    /**
     * Сравнивает удалённые и локальные метаданные исходников.
     */
    fun compareSrcInfo(remoteSrcInfo: SrcInfoRoot, localSrcInfo: SrcInfoRoot): SrcInfoRoot {
        return srcChecksumService.compareSrcInfo(remoteSrcInfo, localSrcInfo)
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
        return compareSrcInfo(remoteSrcInfo, localSrcInfo)
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
                val diff = srcChecksumService.compareAvailableSrcInfo(remoteSrcInfo, localSrcInfo)
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

        logger.lifecycle(
            "Pushing src: scripts={}, modules={}, force={}",
            requestedScripts.size,
            requestedModules.size,
            force
        )
        val srcArchive = srcArchiveService.buildSrcArchive(
            requestedScripts,
            requestedModules,
            srcFoldersService.scripts,
            srcFoldersService.modules
        )
        val pushedInfo = srcArchiveService.pushChecksumsToInfoRoot(connector.pushScripts(srcArchive))
        srcStorageService.updateInfoFile(pushedInfo.scripts, pushedInfo.modules)

        return pushedInfo
    }

}
