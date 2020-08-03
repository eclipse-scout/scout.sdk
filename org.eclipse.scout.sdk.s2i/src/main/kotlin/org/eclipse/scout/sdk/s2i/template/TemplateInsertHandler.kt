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

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingAdapter
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateImplUtil
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.command.WriteCommandAction.writeCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Conditions.alwaysTrue
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleSettings
import com.intellij.psi.util.InheritanceUtil.findEnclosingInstanceInScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ThrowableRunnable
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.log.SdkLog.onTrace
import org.eclipse.scout.sdk.core.model.api.PropertyBean
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.findTypeByName
import org.eclipse.scout.sdk.s2i.isInstanceOf
import java.lang.reflect.Method

/**
 * Handler that inserts a selected [TemplateDescriptor].
 */
class TemplateInsertHandler(val templateDescriptor: TemplateDescriptor, val prefix: String) : InsertHandler<LookupElement> {

    companion object {
        private val CREATE_TEMP_SETTINGS_METHOD = FinalValue<Method>()

        private fun createTempSettings(origSettings: CodeStyleSettings, settingsManager: CodeStyleSettingsManager): CodeStyleSettings {
            val createTemporarySettings = CREATE_TEMP_SETTINGS_METHOD.computeIfAbsentAndGet { createTemporarySettingsMethod() }
            if (createTemporarySettings != null) {
                // use createTemporarySettings() factory method in IJ 2020.2 and newer
                val tempSettings = createTemporarySettings.invoke(settingsManager) as CodeStyleSettings
                tempSettings.copyFrom(origSettings)
                return tempSettings
            }

            // use clone method until IJ 2020.1
            // Can be removed if the supported min. IJ version is 2020.2
            return CodeStyleSettings::class.java.getMethod("clone").invoke(origSettings) as CodeStyleSettings
        }

        private fun createTemporarySettingsMethod() =
                try {
                    CodeStyleSettingsManager::class.java.getMethod("createTemporarySettings")
                } catch (e: NoSuchMethodException) {
                    SdkLog.debug("Using legacy temporary CodeStyleSettings creation.", onTrace(e))
                    null
                }
    }

    private lateinit var m_engine: TemplateEngine

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val editor = context.editor
        val declaringClass = item.getObject() as PsiClass
        val containingModule = declaringClass.containingModule() ?: return
        m_engine = TemplateEngine(templateDescriptor, TemplateEngine.TemplateContext(declaringClass, containingModule, editor.caretModel.offset))

        startTemplateWithTempSettings(buildTemplate(), editor)
    }

    /**
     * The templates do not work if the setting "InsertInnerClassImports" is active.
     * Therefore execute the template with temporary settings (see [CodeStyleSettingsManager.setTemporarySettings]).
     * The temporary settings will be removed again in the [TemplateListener].
     */
    private fun startTemplateWithTempSettings(template: TemplateImpl, editor: Editor) {
        val project = editor.project
        val settingsManager = CodeStyleSettingsManager.getInstance(project)
        val origTempSettings = settingsManager.temporarySettings
        val tempSettings = createTempSettings(CodeStyle.getSettings(editor), settingsManager)
        val tempJavaSettings = tempSettings.getCustomSettings(JavaCodeStyleSettings::class.java)
        tempJavaSettings.isInsertInnerClassImports = false

        writeCommandAction(project).run(ThrowableRunnable<RuntimeException> {
            val templateListener = TemplateListener(templateDescriptor, settingsManager, origTempSettings)
            removePrefix(editor)
            settingsManager.setTemporarySettings(tempSettings)
            TemplateManager.getInstance(project).startTemplate(editor, template, templateListener)
        })
    }

    private fun removePrefix(editor: Editor) {
        if (Strings.isEmpty(prefix)) {
            return
        }
        val document = editor.document
        val offset = editor.caretModel.offset
        var start = offset - prefix.length - 1
        val limit = 0.coerceAtLeast(start - 5)
        val chars = document.immutableCharSequence
        // reduce start index of removal to any preceding alphabet characters
        // this is required for fast typing where the prefix is "older" than the current content of the document
        while (start >= limit && isAlphaChar(chars[start])) {
            start--
        }
        document.replaceString(start + 1, offset, "")
    }

    private fun isAlphaChar(char: Char) = char in 'a'..'z' || char in 'A'..'Z'

    private fun buildTemplate(): TemplateImpl {
        val source = m_engine.buildTemplate()
        val template = TemplateImpl(templateDescriptor.id, source, "Scout")
        template.id = this.templateDescriptor.id
        template.description = templateDescriptor.description()
        template.isToShortenLongNames = true
        template.isToReformat = true
        template.isDeactivated = false
        template.isToIndent = true
        template.setValue(Template.Property.USE_STATIC_IMPORT_IF_POSSIBLE, false)
        TemplateImplUtil.parseVariableNames(source).forEach { addVariable(it, template) }
        template.parseSegments()
        return template
    }

    private fun addVariable(name: String, target: TemplateImpl) {
        // com.intellij.codeInsight.Template internal variables (like "END")
        if (TemplateImpl.INTERNAL_VARS_SET.contains(name)) {
            return
        }

        val adapter = templateDescriptor.variable(name) ?: throw newFail("Variable '{}' is used in the template source but not declared in the template descriptor.", name)
        val descriptor = adapter.invoke(m_engine) ?: return
        target.addVariable(descriptor.name, descriptor.expression, descriptor.defaultValueExpression, true)
    }

    private class TemplateListener(private val templateDescriptor: TemplateDescriptor, private val settingsManager: CodeStyleSettingsManager, private val origSettings: CodeStyleSettings?) : TemplateEditingAdapter() {

        override fun templateCancelled(template: Template?) {
            resetTemporarySettings()
        }

        override fun beforeTemplateFinished(state: TemplateState, template: Template) {
            try {
                insertInnerTypeGetter(state)
            } finally {
                resetTemporarySettings()
            }
        }

        private fun resetTemporarySettings() {
            if (origSettings != null) {
                settingsManager.setTemporarySettings(origSettings)
            } else {
                settingsManager.dropTemporarySettings()
            }
        }

        private fun resolveInnerTypeGetterContainer(createdClass: PsiClass): Pair<PsiClass, String>? {
            val containingModule = createdClass.containingModule() ?: return null
            for (info in templateDescriptor.innerTypeGetterInfos()) {
                val container = if (info.lookupType == TemplateDescriptor.InnerTypeGetterLookupType.CLOSEST) {
                    val innerTypeGetterBase = containingModule.findTypeByName(info.definitionClassFqn) ?: continue
                    findEnclosingInstanceInScope(innerTypeGetterBase, createdClass, alwaysTrue(), false)
                } else {
                    PsiTreeUtil.collectParents(createdClass, PsiClass::class.java, false) { it is PsiFile }
                            .lastOrNull { it.isInstanceOf(info.definitionClassFqn) }
                }

                if (container != null) {
                    return container to info.methodName
                }
            }
            return null
        }

        private fun insertInnerTypeGetter(state: TemplateState) {
            val editor = state.editor
            val project = editor.project ?: return
            val document = editor.document
            val nameRange = state.getVariableRange(TemplateDescriptor.VARIABLE_NAME) ?: return
            val psiDocumentManager = PsiDocumentManager.getInstance(project)
            val file = psiDocumentManager.getPsiFile(document) ?: return
            val element = file.findElementAt(nameRange.startOffset) ?: return
            val createdClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java) ?: return
            val innerTypeGetterInfo = resolveInnerTypeGetterContainer(createdClass) ?: return

            val createdClassFqn = createdClass.qualifiedName
            val createdClassSimpleName = Strings.ensureStartWithUpperCase(createdClass.name)
            val psiElementFactory = JavaPsiFacade.getElementFactory(project)
            val methodName = PropertyBean.GETTER_PREFIX + createdClassSimpleName
            val innerTypeGetterContainer = innerTypeGetterInfo.first
            val innerTypeGetterMethodName = innerTypeGetterInfo.second
            val innerTypeGetter = psiElementFactory.createMethodFromText("public $createdClassFqn $methodName() { return $innerTypeGetterMethodName($createdClassFqn.class);}", innerTypeGetterContainer)
            if (innerTypeGetterContainer.findMethodBySignature(innerTypeGetter, false) != null) {
                return // method already exists
            }

            val anchorInfo = getInsertAnchor(innerTypeGetterContainer, methodName, innerTypeGetterMethodName, document)

            JavaCodeStyleManager.getInstance(project).shortenClassReferences(innerTypeGetter)
            writeCommandAction(project).run(ThrowableRunnable<RuntimeException> {
                if (anchorInfo == null) {
                    innerTypeGetterContainer.add(innerTypeGetter)
                } else {
                    if (anchorInfo.second) {
                        innerTypeGetterContainer.addAfter(innerTypeGetter, anchorInfo.first)
                    } else {
                        innerTypeGetterContainer.addBefore(innerTypeGetter, anchorInfo.first)
                    }
                }
                psiDocumentManager.doPostponedOperationsAndUnblockDocument(document)
            })
        }

        private fun getInsertAnchor(innerTypeGetterContainer: PsiClass, methodName: String, innerTypeGetterMethodName: String, document: Document): Pair<PsiElement, Boolean>? {
            val gettersWithSource = innerTypeGetterContainer.methods
                    .filter { it.name.startsWith(PropertyBean.GETTER_PREFIX) }
                    .map { it to document.getText(it.textRange) }
            val otherInnerTypeGetters = gettersWithSource
                    .filter { it.second.contains(innerTypeGetterMethodName) }
                    .map { it.first }
            return getInsertAnchorFrom(otherInnerTypeGetters, methodName)  // find position within other inner-type-getters first
                    ?: getInsertAnchorFrom(gettersWithSource.map { it.first }, methodName) // find position within all getters
                    ?: getInsertAnchorFrom(innerTypeGetterContainer.methods.filter { !it.isConstructor }, methodName) // find position within all methods
        }

        private fun getInsertAnchorFrom(candidates: Iterable<PsiMethod>, methodName: String) = candidates
                .lastOrNull { it.name < methodName }
                ?.let { it to true } // sort ascending, add after
                ?: candidates
                        .firstOrNull()
                        ?.let { it to false } // all must be after, add before the first
    }
}