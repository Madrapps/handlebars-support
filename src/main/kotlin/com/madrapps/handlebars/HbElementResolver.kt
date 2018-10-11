@file:Suppress("unused")

package com.madrapps.handlebars

import com.dmarcotte.handlebars.psi.*
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.impl.source.PsiClassReferenceType

class HbElementResolver(private val templateClass: PsiClass) {

    fun resolve(element: HbPsiElement): PsiElement? {
        val classes = if (element.isBlockParameter()) {
            element.findParentOfType<HbBlockWrapper>()?.let {
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

    private fun findElement(hbElement: HbPsiElement): MutableList<PsiClass?> {
        val blockWrapper: HbBlockWrapper? = hbElement.findParentOfType()
        if (blockWrapper != null) {
            val elementList = findElement(blockWrapper)

            val hbOpenBlockMustache = blockWrapper.findChildOfType<HbOpenBlockMustache>()
                    ?: return elementList.addAndReturn(null)
            val hbMustacheName = hbOpenBlockMustache.findChildOfType<HbMustacheName>()
                    ?: return elementList.addAndReturn(null)
            val hbParam = hbOpenBlockMustache.findChildOfType<HbParam>() ?: return elementList.addAndReturn(null)

            when (hbMustacheName.name) {
                "each" -> {
                    val type = elementList.findInDepth(hbParam.text)?.type as? PsiClassReferenceType
                    val typeName = type?.className
                    if (typeName == "List") {
                        return elementList.addAndReturn((type.parameters[0] as PsiClassReferenceType).resolve())
                    } else if (typeName == "Map") {
                        return elementList.addAndReturn((type.parameters[1] as PsiClassReferenceType).resolve())
                    }
                    return elementList.addAndReturn(null)
                }
                "with" -> {
                    val type = elementList.findInDepth(hbParam.text)?.type as? PsiClassReferenceType
                    return elementList.addAndReturn(type?.resolve())
                }
                else -> return elementList
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
