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

        val position = element.childPositionInParent(ID)

        val findInDepth1 = classes.findInDepth1(element)

        println("FOME -> ${findInDepth1?.element?.text}")

        return classes.findInDepth1(element)?.element
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
                    val psiField = elementList.findInDepth1(hbParam.findHbPath())?.psiField
                            ?: return elementList.addAndReturn(null)
                    val type = psiField.type as? PsiClassReferenceType
                    val psiClass = type?.resolveToClass() ?: return elementList.addAndReturn(null)
                    return elementList.addAndReturn(PsiGroup(psiField, psiClass))
                }
                else -> return elementList
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
//    private fun MutableList<PsiGroup?>.findInDepth(path: HbPath?, position: Int = 999): PsiField? {
//        if (path == null || position < 0) return null
//        val segments = path.children.filter { it.node.elementType == ID }
//
//        var psiField: PsiField? = null
//        var shouldDeepFind = false
//        loop@ for (i in 0..position) {
//            when (i) {
//                segments.size -> (break@loop)
//                0 -> when (segments[i].text) {
//                    "this" -> psiField = last()?.psiField
//                    ".." -> {
//                        shouldDeepFind = true
//                        psiField = dropLast(1).lastOrNull()?.psiField
//                    }
//                    else -> psiField = findInDepth(segments[i].text)
//                }
//                else -> {
//                    when (segments[i].text) {
//                        "this" -> {
//                            shouldDeepFind = false
//                            //psiField = psiField
//                        }
//                        ".." -> {
//                            shouldDeepFind = true
//                            psiField = dropLast(position + 1).lastOrNull()?.psiField
//                        }
//                        else -> {
//                            if (shouldDeepFind) {
//                                shouldDeepFind = false
//                                psiField = dropLast(position).findInDepth(segments[i].text)
//                            } else {
//                                psiField = (psiField?.type as? PsiClassReferenceType)?.resolveToClass()?.findFieldByName(segments[i].text, true)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return psiField
//    }

    private fun List<PsiGroup?>.findInDepth1(element: PsiElement?): PsiGroup? {
        if (element == null) return null
        fun something(psiElement: PsiElement): List<PsiGroup?> {
            val previousSibling = psiElement.previousSiblingOfType(ID)
            if (previousSibling != null) {
                val classes = something(previousSibling)
                when (previousSibling.text) {
                    ".." -> return classes.dropLast(1)
                    "this" -> return classes.takeLast(1)
                    else -> {
                        val psiField = classes.findInDepth(previousSibling.text)?.psiField
                        val psiClass = (psiField?.type as? PsiClassReferenceType)?.resolveToClass()
                        if (psiClass != null) {
                            return classes.toMutableList().addAndReturn(PsiGroup(psiField, psiClass))
                        } else {
                            return classes.toMutableList().addAndReturn(null)
                        }
                    }
                }
            } else {
                return this
            }
        }

        val something = something(element)

        something.map {
            println("classes -> ${it?.psiField?.name} :: ${it?.psiClass?.name}")
        }
        return when (element.text) {
            ".." -> something.dropLast(1).lastOrNull()
            "this" -> something.lastOrNull()
            else -> something.findInDepth(element.text)
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