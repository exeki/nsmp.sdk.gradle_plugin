package ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src

class SrcInfoRoot {
    val modules: MutableList<SrcInfo> = mutableListOf()
    val scripts: MutableList<SrcInfo> = mutableListOf()
}
