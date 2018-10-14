package com.madrapps.handlebars

import com.dmarcotte.handlebars.psi.HbParam
import com.dmarcotte.handlebars.psi.HbPath
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.tree.IElementType

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

/**
 * Find the position of the element in the parent. A parent can have many children, but this method returns the position
 * relative to the IElementType of the children. So if the type is ID, then only those children are considered, and the position
 * is returned based on that.
 */
internal fun HbPsiElement.childPositionInParent(type: IElementType): Int {
    val filteredChildren = parent.children.filter { it.node.elementType == type }
    return filteredChildren.indexOf(this)
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