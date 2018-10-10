package com.madrapps.handlebars

import com.dmarcotte.handlebars.psi.*
import com.intellij.psi.PsiClass
import com.intellij.psi.impl.source.PsiClassReferenceType

class HbElementResolver(private val templateClass: PsiClass) {

    fun findElement(hbElement: HbPsiElement): MutableList<PsiClass?> {
        val blockWrapper: HbBlockWrapper? = hbElement.findParentOfType()
        if (blockWrapper != null) {
            val elementList = findElement(blockWrapper)
            val ktElement = elementList.last()
            val hbOpenBlockMustache = blockWrapper.findChildOfType<HbOpenBlockMustache>()
                    ?: return elementList.addAndReturn(null)
            val hbMustacheName = hbOpenBlockMustache.findChildOfType<HbMustacheName>()
                    ?: return elementList.addAndReturn(null)
            val hbParam = hbOpenBlockMustache.findChildOfType<HbParam>() ?: return elementList.addAndReturn(null)

            when (hbMustacheName.name) {
                "each" -> {
                    if (ktElement !is PsiClass) return elementList.addAndReturn(null)
                    val type = ktElement.findFieldByName(hbParam.text, true)?.type as? PsiClassReferenceType
                    val typeName = type?.className
                    if (typeName == "List") {
                        return elementList.addAndReturn((type.parameters[0] as PsiClassReferenceType).resolve())
                    } else if (typeName == "Map") {
                        return elementList.addAndReturn((type.parameters[1] as PsiClassReferenceType).resolve())
                    }
                    return elementList.addAndReturn(null)
                }
                "with" -> return elementList
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
