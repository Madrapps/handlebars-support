package com.madrapps.handlebars.findusages

import com.intellij.openapi.editor.VisualPosition
import com.madrapps.handlebars.BaseTestCase

class FindUsagesTest : BaseTestCase() {

    fun testAnimals() = assertSize(2, 12, 2)
    fun testAnimalName() = assertSize(5, 24, 8)
    fun testAnimalNames() = assertSize(6, 15, 2)
    fun testFoods() = assertSize(5, 49, 2)
    fun testFoodName() = assertSize(9, 23, 8)
    fun testFoodType() = assertSize(9, 41, 3)
    fun testTypeName() = assertSize(11, 23, 5)

    private fun assertSize(line: Int, column: Int, expected: Int) {
        addReference("find_usages/handlebar.hbs", "Jungle")
        myFixture.configureByFiles("Jungle.kt", "find_usages/handlebar.hbs")
        myFixture.editor.caretModel.moveToVisualPosition(VisualPosition(line - 1, column - 1))
        val testUsages = myFixture.testFindUsages("Jungle.kt")
        assertEquals(expected, testUsages.size)
    }
}