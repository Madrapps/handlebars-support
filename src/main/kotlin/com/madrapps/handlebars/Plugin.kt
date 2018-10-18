package com.madrapps.handlebars

import com.dmarcotte.handlebars.psi.HbParam
import com.dmarcotte.handlebars.psi.HbPath
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.dmarcotte.handlebars.psi.HbPsiFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassOwner
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.tree.IElementType
import kotlin.String as HbsFilePath

internal inline fun <reified T> HbPsiElement.findAncestorOfType(): T? {
    var tempParent = this.parent
    while (tempParent != null) {
        if (tempParent is T) {
            return tempParent
        }
        tempParent = tempParent.parent
    }
    return null
}

internal inline fun <reified T> HbPsiElement.findChildOfType(): T? {
    return children.find { it is T } as? T
}

internal fun HbParam.findHbPath(): HbPath? {
    if (children.isNotEmpty()) {
        val psiMustacheName = children[0]
        if (psiMustacheName.children.isNotEmpty()) {
            return psiMustacheName.children[0] as? HbPath
        }
    }
    return null
}

internal fun PsiClassReferenceType.resolveToClass(): PsiClass? {
    return when (className) {
        "List" -> (parameters[0] as PsiClassReferenceType).resolve()
        "Map" -> (parameters[1] as PsiClassReferenceType).resolve()
        else -> resolve()
    }
}

internal fun PsiElement.previousSiblingOfType(type: IElementType): PsiElement? {
    var previous = prevSibling
    while (previous != null && previous.node.elementType != type) {
        previous = previous.prevSibling
    }
    return previous
}

internal val PsiFile.classes: List<PsiClass>
    get() = (this as PsiClassOwner).classes.toList()

internal fun HbPsiFile.psiClass(fileMap: Map<HbsFilePath, PsiClass?>): PsiClass? {
    val path = virtualFile.path
    return fileMap[path] ?: return null
}