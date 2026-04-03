package ru.kazantsev.nsd.sdk.gradle_plugin.services.src

import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcInfoRoot

/**
 * Сервис для сравнения checksum'ов локальных и удалённых исходников.
 */
class SrcChecksumService {

    /**
     * Возвращает только те элементы, checksum которых отличается или отсутствует локально.
     */
    fun compareSrcInfo(remoteSrcInfo: SrcInfoRoot, localSrcInfo: SrcInfoRoot): SrcInfoRoot {
        val localScriptsByCode = localSrcInfo.scripts.associateBy { it.code }
        val localModulesByCode = localSrcInfo.modules.associateBy { it.code }
        return SrcInfoRoot(
            scripts = remoteSrcInfo.scripts.filter { remoteInfo ->
                val localInfo = localScriptsByCode[remoteInfo.code]
                localInfo == null || localInfo.checksum != remoteInfo.checksum
            },
            modules = remoteSrcInfo.modules.filter { remoteInfo ->
                val localInfo = localModulesByCode[remoteInfo.code]
                localInfo == null || localInfo.checksum != remoteInfo.checksum
            }
        )
    }
}
