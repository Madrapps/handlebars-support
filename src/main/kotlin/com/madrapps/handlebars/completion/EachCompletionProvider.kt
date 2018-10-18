package com.madrapps.handlebars.completion

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbParam
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.dmarcotte.handlebars.psi.HbPsiFile
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiClass
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.madrapps.handlebars.HbElementResolver
import com.madrapps.handlebars.psiClass

class EachCompletionProvider {
    fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet, psiClassMap: Map<String, PsiClass?>) {
        println("Completion Params")
        val position = PsiTreeUtil.getParentOfType(parameters.position, HbParam::class.java)
        val prevSiblingNode = position?.prevSibling?.prevSibling?.node
        if (prevSiblingNode?.elementType == HbTokenTypes.MUSTACHE_NAME && prevSiblingNode?.text == "each") {
            val parent = parameters.position.parent
            if (parent is HbPsiElement) {
                val templateClass = (parameters.originalFile as? HbPsiFile)?.psiClass(psiClassMap)
                if (templateClass != null) {
                    val resolver = HbElementResolver(templateClass)
                    val classes = resolver.resolveForCompletion(parent)
                    val fields = classes.flatMap { it.allFields.toList() }.map {
                        LookupElementBuilder.create(it)
                    }
                    result.addAllElements(fields)
                }
            }
        }
    }
}