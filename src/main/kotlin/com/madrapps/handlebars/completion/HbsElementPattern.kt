package com.madrapps.handlebars.completion

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbParam
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext

class EachBlockPattern : PatternCondition<PsiElement>("eachBlock") {
    override fun accepts(t: PsiElement, context: ProcessingContext?): Boolean {
        val isInsideParam = psiElement().withAncestor(4, psiElement(HbTokenTypes.PARAM)).accepts(t)
        if (isInsideParam) {
            val position = PsiTreeUtil.getParentOfType(t, HbParam::class.java)
            val prevSiblingNode = position?.prevSibling?.prevSibling?.node
            return (prevSiblingNode?.elementType == HbTokenTypes.MUSTACHE_NAME && prevSiblingNode?.text == "each")
        }
        return false
    }
}