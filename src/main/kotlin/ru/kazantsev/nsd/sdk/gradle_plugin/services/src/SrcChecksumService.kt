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
        val changedSrcInfo = SrcInfoRoot()
        val localScriptsByCode = localSrcInfo.scripts.associateBy { it.code }
        val localModulesByCode = localSrcInfo.modules.associateBy { it.code }

        changedSrcInfo.scripts.addAll(
            remoteSrcInfo.scripts.filter { remoteInfo ->
                val localInfo = localScriptsByCode[remoteInfo.code]
                localInfo == null || localInfo.checksum != remoteInfo.checksum
            }
        )
        changedSrcInfo.modules.addAll(
            remoteSrcInfo.modules.filter { remoteInfo ->
                val localInfo = localModulesByCode[remoteInfo.code]
                localInfo == null || localInfo.checksum != remoteInfo.checksum
            }
        )

        return changedSrcInfo
    }

    /**
     * Возвращает только те элементы, для которых локально уже есть checksum и он отличается.
     */
    fun compareAvailableSrcInfo(remoteSrcInfo: SrcInfoRoot, localSrcInfo: SrcInfoRoot): SrcInfoRoot {
        val changedSrcInfo = SrcInfoRoot()
        val localScriptsByCode = localSrcInfo.scripts.associateBy { it.code }
        val localModulesByCode = localSrcInfo.modules.associateBy { it.code }

        changedSrcInfo.scripts.addAll(
            remoteSrcInfo.scripts.filter { remoteInfo ->
                localScriptsByCode[remoteInfo.code]?.checksum?.let { it != remoteInfo.checksum } == true
            }
        )
        changedSrcInfo.modules.addAll(
            remoteSrcInfo.modules.filter { remoteInfo ->
                localModulesByCode[remoteInfo.code]?.checksum?.let { it != remoteInfo.checksum } == true
            }
        )

        return changedSrcInfo
    }
}
