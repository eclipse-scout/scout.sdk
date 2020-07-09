/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.template

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key
import com.intellij.patterns.PsiJavaPatterns.psiClass
import com.intellij.patterns.PsiJavaPatterns.psiElement
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.log.SdkLog.onTrace
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.Strings

class TemplateCompletionContributor : CompletionContributor() {

    companion object {
        val SCOUT_TEMPLATE_MARKER = Key.create<Boolean>("ScoutTemplateCompletion.marker")
        val USE_LEGACY_SEMICOLON_NEEDED = FinalValue<Boolean>()
    }

    init {
        extend(CompletionType.BASIC, capture(), TemplateCompletionProvider())
    }

    private fun capture() = psiElement()
            .withParent(psiElement(PsiJavaCodeReferenceElement::class.java))
            .withSuperParent(3, psiClass())

    private class TemplateCompletionProvider : CompletionProvider<CompletionParameters>() {

        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val declaringClass = PsiTreeUtil.getParentOfType(parameters.position, PsiClass::class.java) ?: return
            val elements = Templates.templatesFor(declaringClass).map { templateDescriptorToLookupElement(it, declaringClass, result.prefixMatcher.prefix) }
            result.addAllElements(elements)
        }

        private fun templateDescriptorToLookupElement(templateDescriptor: TemplateDescriptor, declaringClass: PsiClass, prefix: String): LookupElement {
            val description = Strings.notBlank(templateDescriptor.description()).map { " ($it)" }.orElse("")
            val name = templateDescriptor.name()
            val element = LookupElementBuilder.create(declaringClass, name)
                    .withCaseSensitivity(false)
                    .withPresentableText(name)
                    .withTailText(description)
                    .withIcon(AllIcons.Nodes.Class)
                    .withLookupStrings(templateDescriptor.aliasNames())
                    .withInsertHandler(TemplateInsertHandler(templateDescriptor, prefix))
            element.putUserData(CodeCompletionHandlerBase.DIRECT_INSERTION, true) // instructs the completion engine to not insert the name of the LookupElement into the source
            element.putUserData(SCOUT_TEMPLATE_MARKER, true) // to identify scout LookupElements. Used for testing.
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
        if (semicolonNeeded(context.editor, file, offset)) {
            return CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED + ";"
        }
        val leaf = file.findElementAt(offset)
        if (leaf is PsiIdentifier || leaf is PsiKeyword) {
            return CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED
        }
        return null
    }

    private fun semicolonNeeded(editor: Editor, file: PsiFile, startOffset: Int): Boolean {
        val useLegacy = USE_LEGACY_SEMICOLON_NEEDED.computeIfAbsentAndGet { isUseLegacySemicolonNeeded() }
        val result = if (useLegacy) semicolonNeededLegacy().invoke(null, editor, file, startOffset) else semicolonNeededNew().invoke(null, file, startOffset)
        return result as Boolean
    }

    /**
     * New Version used by IJ 2020.2
     */
    private fun semicolonNeededNew() = JavaCompletionContributor::class.java.getMethod("semicolonNeeded", PsiFile::class.java, Int::class.java)

    /**
     * Legacy version used until IJ 2020.1
     * Can be removed if the supported min. IJ version is 2020.2
     */
    private fun semicolonNeededLegacy() = JavaCompletionContributor::class.java.getMethod("semicolonNeeded", Editor::class.java, PsiFile::class.java, Int::class.java)

    private fun isUseLegacySemicolonNeeded() =
            try {
                semicolonNeededNew()
                false
            } catch (e: NoSuchMethodException) {
                SdkLog.debug("Using legacy JavaCompletionContributor.semicolonNeeded() method.", onTrace(e))
                true
            }
}


