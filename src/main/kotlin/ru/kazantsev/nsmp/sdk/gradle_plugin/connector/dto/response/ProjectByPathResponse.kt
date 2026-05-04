package ru.kazantsev.nsmp.sdk.gradle_plugin.connector.dto.response

import kotlinx.serialization.Serializable
import ru.kazantsev.nsmp.sdk.gradle_plugin.connector.dto.ProjectInfoDto

@Serializable
data class ProjectByPathResponse(
    val project: ProjectInfoDto,
)
