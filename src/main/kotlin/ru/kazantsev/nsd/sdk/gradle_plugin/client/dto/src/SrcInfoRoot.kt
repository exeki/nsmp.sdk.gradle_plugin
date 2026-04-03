package ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src

class SrcInfoRoot(
    val modules: List<SrcInfo>,
    val scripts: List<SrcInfo>
) {
    constructor() : this(listOf(), listOf())
}
