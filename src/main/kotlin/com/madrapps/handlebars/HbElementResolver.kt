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

        val position = element.childPositionInParent(HbTokenTypes.ID)

        return classes.findInDepth(element.findAncestorOfType<HbPath>(), position)
    }

    private fun findElement(hbElement: HbPsiElement): MutableList<PsiGroup?> {
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
                    val psiField = elementList.findInDepth(hbParam.findHbPath())
                    if (psiField != null) {
                        val type = psiField.type as? PsiClassReferenceType
                        when (type?.className) {
                            "List" -> {
                                val psiClass = (type.parameters[0] as PsiClassReferenceType).resolve()
                                if (psiClass != null) {
                                    elementList.addAndReturn(PsiGroup(psiField, psiClass))
                                } else {
                                    elementList.addAndReturn(null)
                                }
                            }
                            "Map" -> {
                                val psiClass = (type.parameters[1] as PsiClassReferenceType).resolve()
                                if (psiClass != null) {
                                    elementList.addAndReturn(PsiGroup(psiField, psiClass))
                                } else {
                                    elementList.addAndReturn(null)
                                }
                            }
                            else -> elementList.addAndReturn(null)
                        }
                    } else {
                        elementList.addAndReturn(null)
                    }
                }
                "with" -> {
                    val psiField = elementList.findInDepth(hbParam.findHbPath())
                    if (psiField != null) {
                        val psiClass = (psiField.type as? PsiClassReferenceType)?.resolve()
                        if (psiClass != null) {
                            elementList.addAndReturn(PsiGroup(psiField, psiClass))
                        } else {
                            elementList.addAndReturn(null)
                        }
                    } else {
                        elementList.addAndReturn(null)
                    }
                }
                else -> elementList
            }
        } else {
            return mutableListOf(PsiGroup(null, templateClass))
        }
    }

    /**
     * Find the PsiField for a given HbPsiElement. The HbPsiElement is not directly supplied to this method, but rather
     * via 2 parameters (the parent HbParam and the position of HbPsiElement in the parent). For the first HbPsiElement,
     * we will search in-depth (since that's how scope works in handlebars java) works. But for successive HbPsiElement,
     * we will only search for fields of the PsiClass of the previous HbPsiElement.
     *
     * @param path the HbPath element which can have multiple children, but we are concerned only the ID type
     * @param position the position of the child we want to resolve in the HbPath element. For instance, if the HbPath
     * element is `zero.one.two.three`, and the HbPsiElement to resolve is `two`, then the position will be 2. Note that,
     * HbPath has 7 children, but only 4 of them are of Type ID. Hence the position is 2. The position field starts with 0.
     */
    private fun MutableList<PsiGroup?>.findInDepth(path: HbPath?, position: Int = 999): PsiField? {
        if (path == null || position < 0) return null
        val pathElements = path.children.filter { it.node.elementType == HbTokenTypes.ID }

        var currentType: PsiField? = null
        for (i in 0..position) {
            currentType = if (i > pathElements.lastIndex) {
                break
            } else {
                val pathName = pathElements[i].text
                if (i == 0) {
                    if (pathName == "this") {
                        last()?.let { (psiField, _) ->
                            psiField
                        }
                    } else {
                        findInDepth(pathName)
                    }
                } else {
                    (currentType?.type as? PsiClassReferenceType)?.resolveToClass()?.findFieldByName(pathName, true)
                }
            }
        }
        return currentType
    }

    private fun MutableList<PsiGroup?>.findInDepth(fieldName: String): PsiField? {
        reversed().forEach { group ->
            if (group != null) {
                val field = group.psiClass.findFieldByName(fieldName, true)
                if (field != null) return field
            }
        }
        return null
    }

}

private fun <E> MutableList<E>.addAndReturn(element: E): MutableList<E> {
    add(element)
    return this
}

private data class PsiGroup(val psiField: PsiField?, val psiClass: PsiClass)