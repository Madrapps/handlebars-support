package com.madrapps.handlebars.reference

import com.intellij.openapi.editor.VisualPosition
import com.madrapps.handlebars.BaseTestCase


class ReferenceTest : BaseTestCase() {

    fun testEachAnimals() = assertReference(1, 12, "var animals: List<Animal>? = null")

    fun testAnimalName() = assertReference(2, 22, "var animalName: String = \"\"")

    fun testAnimalNames() = assertReference(3, 22, "var animalNames: String = \"\"")

    fun testEachFoods() = assertReference(4, 16, "var foods: List<Food>? = null")

    fun testEachFoodName() = assertReference(5, 20, "val name: String")

    fun testEachFoodsType() = assertReference(6, 25, "val type: Type")

    fun testEachFoodsTypeName() = assertReference(6, 30, "val name: String")

    fun testWithType() = assertReference(7, 19, "val type: Type")

    fun testWithTypeThis() = assertReference(8, 29, "val type: Type")

    private fun assertReference(line: Int, column: Int, expected: String) {
        addReference("reference/handlebars.hbs", "Jungle")
        myFixture.configureByFiles("reference/handlebars.hbs", "reference/Jungle.kt")

        myFixture.editor.caretModel.moveToVisualPosition(VisualPosition(line - 1, column))
        val element = myFixture.file.findElementAt(myFixture.caretOffset)?.parent
        val resolve = element?.references?.get(0)?.resolve()
        assertEquals(expected, resolve?.text)
    }
}