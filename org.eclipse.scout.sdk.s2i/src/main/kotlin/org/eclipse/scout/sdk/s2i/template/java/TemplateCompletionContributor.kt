/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.template.java

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PsiJavaPatterns.psiClass
import com.intellij.patterns.PsiJavaPatterns.psiElement
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.scoutApi
import org.eclipse.scout.sdk.s2i.template.TemplateHelper
import org.eclipse.scout.sdk.s2i.util.CompatibilityHelper

class TemplateCompletionContributor : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, capture(), TemplateCompletionProvider())
    }

    private fun capture() = psiElement()
            .withParent(psiElement(PsiJavaCodeReferenceElement::class.java))
            .withSuperParent(3, psiClass())

    private class TemplateCompletionProvider : CompletionProvider<CompletionParameters>() {

        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val declaringClass = PsiTreeUtil.getParentOfType(parameters.position, PsiClass::class.java) ?: return
            val scoutApi = declaringClass.scoutApi() ?: return
            val elements = Templates.templatesFor(declaringClass, scoutApi.hierarchy()).map { templateDescriptorToLookupElement(it, declaringClass, scoutApi, result.prefixMatcher.prefix) }
            result.addAllElements(elements)
        }

        private fun templateDescriptorToLookupElement(templateDescriptor: TemplateDescriptor, declaringClass: PsiClass, scoutApi: IScoutApi, prefix: String): LookupElement {
            val description = Strings.notBlank(templateDescriptor.description()).map { " ($it)" }.orElse("")
            val name = templateDescriptor.name()
            val element = LookupElementBuilder.create(declaringClass, name)
                    .withCaseSensitivity(false)
                    .withPresentableText(name)
                    .withTailText(description)
                    .withIcon(AllIcons.Nodes.Class)
                    .withLookupStrings(templateDescriptor.aliasNames())
                    .withInsertHandler(TemplateInsertHandler(templateDescriptor, scoutApi, prefix))
            element.putUserData(CodeCompletionHandlerBase.DIRECT_INSERTION, true) // instructs the completion engine to not insert the name of the LookupElement into the source
            element.putUserData(TemplateHelper.SCOUT_LOOKUP_ELEMENT_MARKER, true) // to identify scout LookupElements. Used for testing.
            return element
        }
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        if (context.completionType != CompletionType.BASIC) return
        val dummy = customizeDummyIdentifier(context, context.file)
        if (dummy != null) {
            // if customizeDummyIdentifier returns a value, this identifier will be set by the JavaCompletionContributor
            // As it is not allowed to modify the identifier more than once, abort in that case.
            return
        }
        context.dummyIdentifier = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED + ";"
    }

    /**
     * @see com.intellij.codeInsight.completion.JavaCompletionContributor.customizeDummyIdentifier
     */
    private fun customizeDummyIdentifier(context: CompletionInitializationContext, file: PsiFile): String? {
        val offset = context.startOffset
        if (PsiTreeUtil.findElementOfClassAtOffset(file, offset - 1, PsiReferenceParameterList::class.java, false) != null) {
            return CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED
        }
        if (CompatibilityHelper.semicolonNeeded(context.editor, file, offset)) {
            return CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED + ";"
        }
        val leaf = file.findElementAt(offset)
        if (leaf is PsiIdentifier || leaf is PsiKeyword) {
            return CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED
        }
        return null
    }
}


