@file:Suppress("unused")

package com.madrapps.handlebars

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.*
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.impl.source.PsiClassReferenceType

class HbElementResolver(private val templateClass: PsiClass) {

    fun resolve(element: HbPsiElement): PsiElement? {
        val classes = if (element.isBlockParameter()) {
            element.findAncestorOfType<HbBlockWrapper>()?.let {
                findElement(it)
            } ?: mutableListOf()
        } else {
            findElement(element)
        }

        return classes.findInDepth(element.text)
    }

    private fun MutableList<PsiClass?>.findInDepth(fieldName: String): PsiField? {
        reversed().forEach { psiClass ->
            if (psiClass != null) {
                val field = psiClass.findFieldByName(fieldName, true)
                if (field != null) return field
            }
        }
        return null
    }

    private fun MutableList<PsiClass?>.findInDepth(path: HbPath?): PsiField? {
        if (path == null) return null
        val pathElements = path.children.filter { it.node.elementType == HbTokenTypes.ID }
        var currentType: PsiField? = null
        pathElements.forEachIndexed { index, psiElement ->
            currentType = if (index == 0) {
                findInDepth(psiElement.text)
            } else {
                (currentType?.type as? PsiClassReferenceType)?.resolve()?.findFieldByName(psiElement.text, true)
            }
        }
        return currentType
    }


    private fun findElement(hbElement: HbPsiElement): MutableList<PsiClass?> {
        val blockWrapper: HbBlockWrapper? = hbElement.findAncestorOfType()
        if (blockWrapper != null) {
            val elementList = findElement(blockWrapper)

            val hbOpenBlockMustache = blockWrapper.findChildOfType<HbOpenBlockMustache>()
                    ?: return elementList.addAndReturn(null)
            val hbMustacheName = hbOpenBlockMustache.findChildOfType<HbMustacheName>()
                    ?: return elementList.addAndReturn(null)
            val hbParam = hbOpenBlockMustache.findChildOfType<HbParam>() ?: return elementList.addAndReturn(null)

            return when (hbMustacheName.name) {
                "each" -> {
                    val type = elementList.findInDepth(hbParam.findHbPath())?.type as? PsiClassReferenceType
                    when (type?.className) {
                        "List" -> elementList.addAndReturn((type.parameters[0] as PsiClassReferenceType).resolve())
                        "Map" -> elementList.addAndReturn((type.parameters[1] as PsiClassReferenceType).resolve())
                        else -> elementList.addAndReturn(null)
                    }
                }
                "with" -> {
                    val type = elementList.findInDepth(hbParam.findHbPath())
                    elementList.addAndReturn((type?.type as? PsiClassReferenceType)?.resolve())
                }
                else -> elementList
            }
        } else {
            return mutableListOf(templateClass)
        }
    }
}

private fun <E> MutableList<E>.addAndReturn(element: E): MutableList<E> {
    add(element)
    return this
}