package com.madrapps.handlebars

import com.dmarcotte.handlebars.psi.HbParam
import com.dmarcotte.handlebars.psi.HbPath
import com.dmarcotte.handlebars.psi.HbPsiElement

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

internal fun HbPsiElement.isBlockParameter(): Boolean {
    return parent?.parent?.parent is HbParam
}