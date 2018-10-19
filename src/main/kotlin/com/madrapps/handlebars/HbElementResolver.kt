@file:Suppress("unused")

package com.madrapps.handlebars

import com.dmarcotte.handlebars.parsing.HbTokenTypes.ID
import com.dmarcotte.handlebars.psi.*
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiPrimitiveType
import com.intellij.psi.impl.source.PsiClassReferenceType
import org.jetbrains.kotlin.asJava.elements.KtLightMember

class HbElementResolver(private val templateClass: PsiClass) {

    fun resolveForCompletion(element: HbPsiElement): List<PsiClass> {
        return findInBlockScope(element).findInDotScope(element).mapNotNull { it?.psiClass }.reversed()
    }

    fun resolve(element: HbPsiElement): PsiElement? {
        val psiElement = findInBlockScope(element).findInDepth(element)?.element
        if (psiElement is KtLightMember<*>) {
            return psiElement.lightMemberOrigin?.originalElement
        }
        return psiElement
    }

    private fun findInBlockScope(element: HbPsiElement): MutableList<PsiGroup?> {
        fun find(hbElement: HbPsiElement?): MutableList<PsiGroup?> {
            val blockWrapper: HbBlockWrapper? = hbElement?.findAncestorOfType()
            if (blockWrapper != null) {
                val elementList = find(blockWrapper)

                val hbOpenBlockMustache = blockWrapper.findChildOfType<HbOpenBlockMustache>()
                val hbParam = hbOpenBlockMustache?.findChildOfType<HbParam>()
                val hbMustacheName = hbOpenBlockMustache?.findChildOfType<HbMustacheName>()

                return when (hbMustacheName?.name) {
                    // TODO "with" behaves same as "each", but we should ensure that Lists are not possible in "with" (inspection/autocompletion)
                    "each", "with" -> {
                        val psiGroup = elementList.findInDepth(hbParam?.findHbPath()?.lastChild as? HbPsiElement)
                        elementList.addAndReturn(psiGroup)
                    }
                    null -> elementList.addAndReturn(null)
                    else -> elementList
                }
            } else {
                return mutableListOf(PsiGroup(null, templateClass))
            }
        }

        val grandParent = element.parent?.parent?.parent?.parent
        return if (grandParent is HbOpenBlockMustache) {
            find(grandParent.parent as? HbPsiElement)
        } else {
            find(element)
        }
    }

    private fun List<PsiGroup?>.findInDepth(element: HbPsiElement?): PsiGroup? {
        if (element == null) return null
        val classes = findInDotScope(element)
        return when (element.text) {
            ".." -> classes.dropLast(1).lastOrNull()
            "this" -> classes.lastOrNull()
            else -> classes.findInDepth(element.text)
        }
    }

    private fun List<PsiGroup?>.findInDotScope(psiElement: PsiElement): List<PsiGroup?> {
        val previousSibling = psiElement.previousSiblingOfType(ID)
        return if (previousSibling != null) {
            val classes = findInDotScope(previousSibling)
            when (previousSibling.text) {
                ".." -> classes.dropLast(1)
                "this" -> classes.takeLast(1)
                else -> {
                    val psiGroup = classes.findInDepth(previousSibling.text)
                    if (psiGroup?.isCollection == false) mutableListOf(psiGroup) else emptyList()
                }
            }
        } else {
            this
        }
    }

    private fun List<PsiGroup?>.findInDepth(fieldName: String): PsiGroup? {
        reversed().forEach { group ->
            val psiField = group?.psiClass?.findFieldByName(fieldName, true)
            val type = psiField?.type
            if (type is PsiClassReferenceType) {
                with(type.canonicalText) {
                    return when {
                        startsWith("java.lang.String") -> PsiGroup(psiField, null)
                        startsWith("java.util.List") -> PsiGroup(psiField, type.resolveToClass(), true)
                        startsWith("java.util.Map") -> PsiGroup(psiField, type.resolveToClass(), true)
                        else -> PsiGroup(psiField, type.resolveToClass())
                    }
                }
            } else if (type is PsiPrimitiveType) {
                return PsiGroup(psiField, null)
            }
        }
        return null
    }
}

private fun <E> MutableList<E>.addAndReturn(element: E): MutableList<E> {
    add(element)
    return this
}

private data class PsiGroup(val psiField: PsiField?, val psiClass: PsiClass?, val isCollection: Boolean = false) {
    val element: PsiElement?
        get() = psiField ?: psiClass
}