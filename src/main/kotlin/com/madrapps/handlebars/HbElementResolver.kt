@file:Suppress("unused")

package com.madrapps.handlebars

import com.dmarcotte.handlebars.parsing.HbTokenTypes.ID
import com.dmarcotte.handlebars.psi.*
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.impl.source.PsiClassReferenceType

class HbElementResolver(private val templateClass: PsiClass) {

    fun resolve(element: HbPsiElement): PsiElement? {
        val grandParent = element.parent?.parent?.parent?.parent
        val classes = if (grandParent is HbOpenBlockMustache) {
            findElement(grandParent.parent as? HbPsiElement)
        } else {
            findElement(element)
        }

        return classes.findInDepth(element)?.element
    }

    private fun findElement(hbElement: HbPsiElement?): MutableList<PsiGroup?> {
        val blockWrapper: HbBlockWrapper? = hbElement?.findAncestorOfType()
        if (blockWrapper != null) {
            val elementList = findElement(blockWrapper)

            val hbOpenBlockMustache = blockWrapper.findChildOfType<HbOpenBlockMustache>()
                    ?: return elementList.addAndReturn(null)
            val hbMustacheName = hbOpenBlockMustache.findChildOfType<HbMustacheName>()
                    ?: return elementList.addAndReturn(null)
            val hbParam = hbOpenBlockMustache.findChildOfType<HbParam>() ?: return elementList.addAndReturn(null)

            when (hbMustacheName.name) {
                // TODO "with" behaves same as "each", but we should ensure that Lists are not possible in "with" (inspection/autocompletion)
                "each", "with" -> {
                    val psiGroup = elementList.findInDepth(hbParam.findHbPath())
                    return elementList.addAndReturn(psiGroup)
                }
                else -> return elementList
            }
        } else {
            return mutableListOf(PsiGroup(null, templateClass))
        }
    }

    private fun List<PsiGroup?>.findInDepth(element: PsiElement?): PsiGroup? {
        if (element == null) return null
        fun something(psiElement: PsiElement): List<PsiGroup?> {
            val previousSibling = psiElement.previousSiblingOfType(ID)
            if (previousSibling != null) {
                val classes = something(previousSibling)
                when (previousSibling.text) {
                    ".." -> return classes.dropLast(1)
                    "this" -> return classes.takeLast(1)
                    else -> {
                        val psiGroup = classes.findInDepth(previousSibling.text)
                        return classes.toMutableList().addAndReturn(psiGroup)
                    }
                }
            } else {
                return this
            }
        }

        val classes = something(element)

        return when (element.text) {
            ".." -> classes.dropLast(1).lastOrNull()
            "this" -> classes.lastOrNull()
            else -> classes.findInDepth(element.text)
        }
    }

    private fun List<PsiGroup?>.findInDepth(fieldName: String): PsiGroup? {
        reversed().forEach { group ->
            if (group != null) {
                val field = group.psiClass.findFieldByName(fieldName, true)
                if (field != null) {
                    val psiClass = (field.type as? PsiClassReferenceType)?.resolveToClass()
                    if (psiClass != null) {
                        return PsiGroup(field, psiClass)
                    }
                }
            }
        }
        return null
    }
}

private fun <E> MutableList<E>.addAndReturn(element: E): MutableList<E> {
    add(element)
    return this
}

private data class PsiGroup(val psiField: PsiField?, val psiClass: PsiClass) {
    val element: PsiElement
        get() = psiField ?: psiClass
}