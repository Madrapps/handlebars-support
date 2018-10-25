package com.madrapps.handlebars.completion

import com.intellij.codeInsight.completion.CompletionType
import com.madrapps.handlebars.BaseTestCase


class EachCompletion : BaseTestCase() {

    fun testEach() = assertCompletion(listOf("var animals: List<Animal>? = null"))

    private fun assertCompletion(expected: List<String>) {
        val testName = getTestName(false)
        addReference("completion/$testName.hbs", "Jungle")
        myFixture.configureByFiles("completion/$testName.hbs", "Jungle.kt")
        myFixture.complete(CompletionType.BASIC, 1)
        val texts = myFixture.lookupElements?.map { it.psiElement?.text }
        assertEquals(expected, texts)
    }
}