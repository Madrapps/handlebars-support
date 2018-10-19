package com.madrapps.handlebars.persistence

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.MapAnnotation
import java.util.HashMap

@State(name = "HbsClassMap", storages = [Storage("hbsClassMapping.xml")])
class PersistenceService : PersistentStateComponent<PersistenceService> {

    @MapAnnotation var psiMap: HashMap<String, String?> = HashMap()

    override fun getState(): PersistenceService? {
        return this
    }

    override fun loadState(service: PersistenceService) {
        XmlSerializerUtil.copyBean(service, this);
    }

    companion object {
        fun getInstance(project: Project): PersistenceService {
            return ServiceManager.getService(project, PersistenceService::class.java)
        }
    }
}