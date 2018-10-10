package com.madrapps.handlebars

import com.dmarcotte.handlebars.psi.HbPsiElement

inline fun <reified T> HbPsiElement.findParentOfType(): T? {
    var tempParent = this.parent
    while (tempParent != null) {
        if (tempParent is T) {
            return tempParent
        }
        tempParent = tempParent.parent
    }
    return null
}

inline fun <reified T> HbPsiElement.findChildOfType(): T? {
    return children.find { it is T } as? T
}
