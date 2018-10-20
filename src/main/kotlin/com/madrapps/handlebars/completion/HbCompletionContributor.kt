package com.madrapps.handlebars.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType.BASIC
import com.intellij.patterns.PlatformPatterns.psiElement

class HbCompletionContributor : CompletionContributor() {
    init {
        println("INIT COMPLETION PROVIDER")
        extend(BASIC, psiElement().with(EachBlockPattern()), GeneralCompletionProvider())
        extend(BASIC, psiElement().with(GeneralBlockPattern()), GeneralCompletionProvider())
        extend(BASIC, psiElement().with(SimpleMustachePattern()), GeneralCompletionProvider())
    }
}