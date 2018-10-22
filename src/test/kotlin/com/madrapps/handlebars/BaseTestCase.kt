package com.madrapps.handlebars

import com.intellij.openapi.components.ServiceManager
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import com.madrapps.handlebars.persistence.PersistenceService
import java.io.File

open class BaseTestCase : LightCodeInsightFixtureTestCase() {

    protected fun addReference(hbsFile: String, pojoClass: String) {
        val psiMap = ServiceManager.getService(myFixture.project, PersistenceService::class.java).psiMap
        psiMap.clear()
        psiMap["/src/$hbsFile"] = pojoClass
    }

    override fun getTestDataPath(): String = File("src/test", "data").path
}