@file:Suppress("unused")

package com.madrapps.handlebars

import com.dmarcotte.handlebars.psi.*
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.PsiClassReferenceType

class HbElementResolver(private val templateClass: PsiClass) {

    fun resolve(element: HbPsiElement): PsiElement? {
        val parent = element.parent?.parent
        val classes = if (parent is HbBlockWrapper) {
            findElement(parent)
        } else {
            findElement(element)
        }
        classes.reverse()
        classes.forEach { psiClass ->
            if (psiClass != null) {
                val resolvedElement = psiClass.allFields.find { it.name == element.text }
                if (resolvedElement != null) return resolvedElement
            }
        }
        return null
    }

    private fun findElement(hbElement: HbPsiElement): MutableList<PsiClass?> {
        val blockWrapper: HbBlockWrapper? = hbElement.findParentOfType()
        if (blockWrapper != null) {
            val elementList = findElement(blockWrapper)
            val element = elementList.last() ?: return elementList.addAndReturn(null)

            val hbOpenBlockMustache = blockWrapper.findChildOfType<HbOpenBlockMustache>()
                    ?: return elementList.addAndReturn(null)
            val hbMustacheName = hbOpenBlockMustache.findChildOfType<HbMustacheName>()
                    ?: return elementList.addAndReturn(null)
            val hbParam = hbOpenBlockMustache.findChildOfType<HbParam>() ?: return elementList.addAndReturn(null)

            when (hbMustacheName.name) {
                "each" -> {
                    val type = element.findFieldByName(hbParam.text, true)?.type as? PsiClassReferenceType
                    val typeName = type?.className
                    if (typeName == "List") {
                        return elementList.addAndReturn((type.parameters[0] as PsiClassReferenceType).resolve())
                    } else if (typeName == "Map") {
                        return elementList.addAndReturn((type.parameters[1] as PsiClassReferenceType).resolve())
                    }
                    return elementList.addAndReturn(null)
                }
                "with" -> {
                    val type = element.findFieldByName(hbParam.text, true)?.type as? PsiClassReferenceType
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
