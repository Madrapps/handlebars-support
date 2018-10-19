package com.madrapps.handlebars.completion

import com.dmarcotte.handlebars.psi.HbPsiElement
import com.dmarcotte.handlebars.psi.HbPsiFile
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.util.ProcessingContext
import com.madrapps.handlebars.HbElementResolver
import com.madrapps.handlebars.psiClass

class EachCompletionProvider {
    fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet, psiClassMap: Map<String, PsiClass?>) {
        println("Completion Params")
        val parent = parameters.position.parent
        if (parent is HbPsiElement) {
            val templateClass = (parameters.originalFile as? HbPsiFile)?.psiClass(psiClassMap)
            if (templateClass != null) {
                val resolver = HbElementResolver(templateClass)
                val classes = resolver.resolveForCompletion(parent)
                val lookupElements = getFields(classes).map {
                    LookupElementBuilder.create(it).withTypeText(it.type.presentableText)
                }
                result.addAllElements(lookupElements)
            }
        }
    }

    private fun getFields(classes: List<PsiClass>): List<PsiField> {
        val fields = mutableListOf<PsiField>()
        classes.forEach { psiClass ->
            psiClass.allFields.forEach { psiField ->
                fields.addIfNotPresent(psiField)
            }
        }
        return fields
    }

    private fun MutableList<PsiField>.addIfNotPresent(psiField: PsiField) {
        val name = psiField.name
        val alreadyHasFieldWithSameName = this.asSequence().map { it.name }.contains(name)
        if (!alreadyHasFieldWithSameName) add(psiField)
    }
}

class GeneralCompletionProvider {
    fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet, psiClassMap: Map<String, PsiClass?>) {
        EachCompletionProvider().addCompletions(parameters, context, result, psiClassMap)
    }
}