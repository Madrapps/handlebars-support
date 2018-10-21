package com.madrapps.handlebars.findusages

import com.intellij.openapi.components.ServiceManager
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import com.intellij.usageView.UsageInfo
import com.madrapps.handlebars.persistence.PersistenceService
import java.io.File

class FindUsagesTest : LightCodeInsightFixtureTestCase() {

    fun testAnimals() = assertSize(2)
    fun testAnimalName() = assertSize(8)
    fun testAnimalNames() = assertSize(2)
    fun testFoods() = assertSize(2)
    fun testFoodName() = assertSize(8)
    fun testFoodType() = assertSize(3)
    fun testTypeName() = assertSize(5)

    private fun assertSize(expected: Int) {
        assertEquals(expected, testUsages().size)
    }

    private fun testUsages(): MutableCollection<UsageInfo> {
        val testName = getTestName(false)
        addReference("handlebar.hbs", "Jungle")
        myFixture.configureByFiles("find_usages/$testName.kt", "find_usages/handlebar.hbs")
        return myFixture.testFindUsages("find_usages/$testName.kt")
    }

    private fun addReference(hbsFile: String, pojoClass: String) {
        val psiMap = ServiceManager.getService(myFixture.project, PersistenceService::class.java).psiMap
        psiMap.clear()
        psiMap["/src/find_usages/$hbsFile"] = pojoClass
    }

    override fun getTestDataPath(): String = File("src/test", "data").path
}