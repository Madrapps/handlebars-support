package com.madrapps.handlebars.reference

import com.intellij.openapi.editor.VisualPosition
import com.madrapps.handlebars.BaseTestCase


class ReferenceTest : BaseTestCase() {

    fun testEachAnimals() = assertSimpleReference(1, 12, "var animals: List<Animal>? = null")
    fun testAnimalName() = assertSimpleReference(2, 22, "var animalName: String = \"\"")
    fun testAnimalNames() = assertSimpleReference(3, 22, "var animalNames: String = \"\"")
    fun testEachFoods() = assertSimpleReference(4, 16, "var foods: List<Food>? = null")
    fun testEachFoodName() = assertSimpleReference(5, 20, "val name: String")
    fun testEachFoodsType() = assertSimpleReference(6, 25, "val type: Type")
    fun testEachFoodsTypeName() = assertSimpleReference(6, 30, "val name: String")
    fun testWithType() = assertSimpleReference(7, 19, "val type: Type")
    fun testWithTypeThis() = assertSimpleReference(8, 29, "val type: Type")
    fun testWithTypeThisName() = assertSimpleReference(8, 34, "val name: String")
    fun testWithTypeDot() = assertSimpleReference(9, 28, "var foods: List<Food>? = null")
    fun testWithTypeDotThis() = assertSimpleReference(9, 32, "var foods: List<Food>? = null")
    fun testWithTypeDotThisName() = assertSimpleReference(9, 37, "val name: String")
    fun testWithTypeDotName() = assertSimpleReference(10, 32, "val name: String")
    fun testWithTypeDotDot() = assertSimpleReference(11, 29, "var animals: List<Animal>? = null")
    fun testWithTypeDotDotName() = assertSimpleReference(11, 33, null)
    fun testWithTypeDotThisThis() = assertSimpleReference(12, 37, "var foods: List<Food>? = null")
    fun testWithTypeDotThisThisThis() = assertSimpleReference(12, 42, "var foods: List<Food>? = null")
    fun testWithTypeDotThisThisThisName() = assertSimpleReference(12, 47, "val name: String")
    fun testWithTypeDotAnimalName() = assertSimpleReference(13, 37, "var animalName: String = \"\"")
    fun testWithTypeDotDotAnimalName() = assertSimpleReference(14, 41, "var animalName: String = \"\"")
    fun testWithTypeThisAnimalName() = assertSimpleReference(15, 36, null)
    fun testWithTypeThisDot() = assertSimpleReference(16, 34, "var foods: List<Food>? = null")
    fun testWithTypeThisDotAnimalName() = assertSimpleReference(16, 42, "var animalName: String = \"\"")
    fun testWithTypeName() = assertSimpleReference(17, 29, "val name: String")

    fun testEachThis() = assertThisReference(1, 11,
            "class Jungle {\n" +
                    "    var animals: List<Animal>? = null\n" +
                    "}")

    fun testEachThisAnimals() = assertThisReference(1, 17, "var animals: List<Animal>? = null")
    fun testEachThisAnimalsThis() = assertThisReference(2, 18, "var animals: List<Animal>? = null")
    fun testThisAnimalName() = assertThisReference(2, 27, "var animalName: String = \"\"")
    fun testThisAnimalNames() = assertThisReference(3, 27, "var animalNames: String = \"\"")
    fun testEachEachThis() = assertThisReference(4, 15, "var animals: List<Animal>? = null")
    fun testEachThisFoods() = assertThisReference(4, 20, "var foods: List<Food>? = null")

    fun testEachThisFoodsThis() = assertThisReference(5, 20, "var foods: List<Food>? = null")
    fun testEachThisFoodsThisName() = assertThisReference(5, 25, "val name: String")

    fun testEachThisFoodsType() = assertThisReference(6, 30, "val type: Type")
    fun testEachThisFoodsTypeName() = assertThisReference(6, 35, "val name: String")

    fun testEachThisFoodsWithThis() = assertThisReference(7, 19, "var foods: List<Food>? = null")
    fun testEachThisFoodsWithThisType() = assertThisReference(7, 24, "val type: Type")

    fun testWithThisTypeThis() = assertThisReference(8, 29, "val type: Type")
    fun testWithThisTypeThisName() = assertThisReference(8, 34, "val name: String")
    fun testWithThisTypeDot() = assertThisReference(9, 28, "var foods: List<Food>? = null")
    fun testWithThisTypeDotThis() = assertThisReference(9, 32, "var foods: List<Food>? = null")
    fun testWithThisTypeDotThisName() = assertThisReference(9, 37, "val name: String")
    fun testWithThisTypeDotName() = assertThisReference(10, 32, "val name: String")
    fun testWithThisTypeDotDot() = assertThisReference(11, 29, "var animals: List<Animal>? = null")
    fun testWithThisTypeDotDotName() = assertThisReference(11, 33, null)
    fun testWithThisTypeDotThisThis() = assertThisReference(12, 37, "var foods: List<Food>? = null")
    fun testWithThisTypeDotThisThisThis() = assertThisReference(12, 42, "var foods: List<Food>? = null")
    fun testWithThisTypeDotThisThisThisName() = assertThisReference(12, 47, "val name: String")
    fun testWithThisTypeDotAnimalName() = assertThisReference(13, 37, "var animalName: String = \"\"")
    fun testWithThisTypeDotDotAnimalName() = assertThisReference(14, 41, "var animalName: String = \"\"")
    fun testWithThisTypeThisAnimalName() = assertThisReference(15, 36, null)
    fun testWithThisTypeThisDot() = assertThisReference(16, 34, "var foods: List<Food>? = null")
    fun testWithThisTypeThisDotAnimalName() = assertThisReference(16, 42, "var animalName: String = \"\"")
    fun testWithThisTypeName() = assertThisReference(17, 29, "val name: String")

    private fun assertSimpleReference(line: Int, column: Int, expected: String?) {
        assertReference("handlebars.hbs", line, column, expected)
    }

    private fun assertThisReference(line: Int, column: Int, expected: String?) {
        assertReference("handlebarsThis.hbs", line, column, expected)
    }

    private fun assertReference(hbsFileName: String, line: Int, column: Int, expected: String?) {
        addReference("reference/$hbsFileName", "Jungle")
        myFixture.configureByFiles("reference/$hbsFileName", "Jungle.kt")

        myFixture.editor.caretModel.moveToVisualPosition(VisualPosition(line - 1, column - 1))
        val element = myFixture.file.findElementAt(myFixture.caretOffset)?.parent
        val resolve = element?.references?.get(0)?.resolve()
        assertEquals(expected, resolve?.text)
    }
}