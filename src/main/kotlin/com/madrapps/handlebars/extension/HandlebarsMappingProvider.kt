package com.madrapps.handlebars.extension

import com.intellij.openapi.extensions.ExtensionPointName

interface HandlebarsMappingProvider {

    /**
     * Provide the map of HbFile path (local to file system) to the Qualified name of the POJO class
     */
    fun addHbsMapping(): Map<String, String>

    companion object {
        val MAPPING_PROVIDER_EP_NAME = ExtensionPointName.create<HandlebarsMappingProvider>("com.madrapps.handlebars-support.handlebars.mappingProvider")
    }
}