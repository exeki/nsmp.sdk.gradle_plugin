package ru.kazantsev.nsd.sdk.gradle_plugin.client.nsd_connector


import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.entity.StringEntity
import ru.kazantsev.nsd.basic_api_connector.Connector
import ru.kazantsev.nsd.basic_api_connector.ConnectorParams
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.meta.MetaClassWrapperDto
import ru.kazantsev.nsd.sdk.gradle_plugin.client.dto.src.SrcInfoRoot

/**
 * РљРѕРЅРЅРµРєС‚РѕСЂ Рє NSD
 */
class SdkApiConnector(params: ConnectorParams) : Connector(params) {

    private val moduleBase: String = "modules.sdkController."
    private val paramsConst: String = "request,response,user"

    /**
     * РџРѕР»СѓС‡РёС‚СЊ РёРЅС„РѕСЂРјР°С†РёСЋ Рѕ РјРµС‚Р°РєР»Р°СЃСЃРµ
     * @param metaClassCode РєРѕРґ РјРµС‚Р°РєР»Р°СЃСЃР°, РёРЅС„Р° РїРѕ РєРѕС‚РѕСЂРѕРјСѓ РЅСѓР¶РЅР°
     * @return dto СЃ РёРЅС„РѕСЂРјР°С†РёРµР№
     */
    fun getMetaClassInfo(metaClassCode: String): MetaClassWrapperDto {
        val methodName = "getMetaClassInfo"
        val response = this.execGet(
            moduleBase + methodName,
            paramsConst,
            mapOf("meta" to metaClassCode)
        )
        val body: String = EntityUtils.toString(response.entity)
        return this.objectMapper.readValue(body, MetaClassWrapperDto::class.java)
    }

    fun getMetaClassBranchInfo(metaClassCode: String): List<MetaClassWrapperDto> {
        val methodName = "getMetaClassBranchInfo"
        val response = this.execGet(
            moduleBase + methodName,
            paramsConst,
            mapOf("meta" to metaClassCode)
        )
        val body: String = EntityUtils.toString(response.entity)
        return this.objectMapper.readValue(
            body,
            objectMapper.typeFactory.constructCollectionType(List::class.java, MetaClassWrapperDto::class.java)
        )
    }

    fun getMetaClassBranchesInfo(metaClassCodes: Collection<String>): List<MetaClassWrapperDto> {
        val methodName = "getMetaClassBranchesInfo"
        val response = this.execGet(
            moduleBase + methodName,
            paramsConst,
            mapOf("metas" to metaClassCodes.joinToString(","))
        )
        val body: String = EntityUtils.toString(response.entity)
        return this.objectMapper.readValue(
            body,
            objectMapper.typeFactory.constructCollectionType(List::class.java, MetaClassWrapperDto::class.java)
        )
    }

    fun getSrc(scripts: List<String>, modules: List<String>): ByteArray {
        val body = mapOf("scripts" to scripts, "modules" to modules)
        val httpEntity = StringEntity(objectMapper.writeValueAsString(body), ContentType.APPLICATION_JSON)
        val response = this.execPost(
            httpEntity,
            moduleBase + "getSrc",
            paramsConst,
            null,
        )
        response.use {
            return EntityUtils.toByteArray(response.entity)
        }
    }

    fun getSrcInfo(scripts: List<String>, modules: List<String>): SrcInfoRoot {
        val body = mapOf("scripts" to scripts, "modules" to modules)
        val httpEntity = StringEntity(objectMapper.writeValueAsString(body), ContentType.APPLICATION_JSON)
        val response = this.execPost(
            httpEntity,
            moduleBase + "getSrcInfo",
            paramsConst,
            null,
        )
        val bodyText = EntityUtils.toString(response.entity, Charsets.UTF_8)
        return objectMapper.readValue(bodyText, SrcInfoRoot::class.java)
    }
}
