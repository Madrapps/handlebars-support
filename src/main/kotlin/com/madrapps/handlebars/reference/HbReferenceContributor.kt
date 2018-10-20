package com.madrapps.handlebars.reference

import com.dmarcotte.handlebars.parsing.HbTokenTypes
import com.dmarcotte.handlebars.psi.HbPsiElement
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import com.madrapps.handlebars.HbElementResolver
import com.madrapps.handlebars.persistence.PersistenceService

class HbReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val paramElementInBlocks = PlatformPatterns.psiElement(HbPsiElement::class.java)
                .withAncestor(3, PlatformPatterns.psiElement().withElementType(HbTokenTypes.PARAM))
                .andNot(PlatformPatterns.psiElement().withChild(PlatformPatterns.psiElement(HbPsiElement::class.java)))
        val simpleElements = PlatformPatterns.psiElement(HbPsiElement::class.java)
                .withAncestor(3, PlatformPatterns.psiElement().withElementType(HbTokenTypes.MUSTACHE))
                .andNot(PlatformPatterns.psiElement().withChild(PlatformPatterns.psiElement(HbPsiElement::class.java)))
        registrar.registerReferenceProvider(paramElementInBlocks, HbReferenceProvider())
        registrar.registerReferenceProvider(simpleElements, HbReferenceProvider())
    }
}

class HbReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if (element !is HbPsiElement) return emptyArray()
        return arrayOf(HbsElementReference(element))
    }
}

class HbsElementReference(private val element: HbPsiElement)
    : PsiReferenceBase<PsiElement>(element, TextRange.allOf(element.text)) {

    override fun resolve(): PsiElement? {
        val psiClass = psiClass() ?: return null

        val resolver = HbElementResolver(psiClass)
        return resolver.resolve(element)
    }

    private fun psiClass(): PsiClass? {
        val hbFilePath = element.containingFile.virtualFile.path
        val project = element.project
        val qualifiedName = PersistenceService.getInstance(project).psiMap[hbFilePath] ?: return null
        return JavaPsiFacade.getInstance(project).findClass(qualifiedName, GlobalSearchScope.allScope(project))
    }

    override fun handleElementRename(newElementName: String?): PsiElement {
        with(element.lastChild) {
            if (this is LeafPsiElement && newElementName != null) {
                replaceWithText(newElementName)
            }
        }
        return element
    }

    override fun getVariants(): Array<Any> = emptyArray()
}