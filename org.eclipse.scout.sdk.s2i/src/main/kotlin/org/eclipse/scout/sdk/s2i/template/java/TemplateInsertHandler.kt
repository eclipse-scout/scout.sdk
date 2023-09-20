/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template.java

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingAdapter
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateImplUtil
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.command.WriteCommandAction.writeCommandAction
import com.intellij.openapi.command.undo.UndoManager
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
import org.eclipse.scout.sdk.core.java.model.api.PropertyBean
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.findTypeByName
import org.eclipse.scout.sdk.s2i.isInstanceOf
import org.eclipse.scout.sdk.s2i.template.TemplateHelper

/**
 * Handler that inserts a selected [TemplateDescriptor].
 */
@Suppress("TestOnlyProblems")
class TemplateInsertHandler(val templateDescriptor: TemplateDescriptor, val scoutApi: IScoutApi, val prefix: CharSequence) : InsertHandler<LookupElement> {

    private lateinit var m_engine: TemplateEngine
    private var m_insertPos: Int = -1

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val editor = context.editor
        val declaringClass = item.getObject() as PsiClass
        val containingModule = declaringClass.containingModule() ?: return
        m_engine = TemplateEngine(templateDescriptor, TemplateEngine.TemplateContext(declaringClass, containingModule, scoutApi, editor.caretModel.offset))

        startTemplateWithTempSettings(buildTemplate(), editor)
    }

    /**
     * The templates do not work if the setting "InsertInnerClassImports" is active.
     * Therefore, execute the template with temporary settings (see [CodeStyleSettingsManager.setTemporarySettings]).
     * The temporary settings will be removed again in the [TemplateListener].
     */
    private fun startTemplateWithTempSettings(template: TemplateImpl, editor: Editor) {
        val project = editor.project

        writeCommandAction(project).run(ThrowableRunnable<RuntimeException> {
            val settingsManager = CodeStyleSettingsManager.getInstance(project)
            val origTempSettings = settingsManager.temporarySettings
            val origProjectSettings = CodeStyle.getSettings(editor) // must be obtained before creating temp settings!
            val tempSettings = CodeStyleSettingsManager.getInstance(project).createTemporarySettings()
            tempSettings.copyFrom(origProjectSettings)
            tempSettings.getCustomSettings(JavaCodeStyleSettings::class.java).isInsertInnerClassImports = false

            val templateListener = TemplateListener(templateDescriptor, editor, settingsManager, origTempSettings) {
                m_insertPos
            }
            TemplateHelper.removePrefix(editor, prefix)
            TemplateManager.getInstance(project).startTemplate(editor, template, templateListener)

            m_insertPos = TemplateManagerImpl.getTemplateState(editor)
                ?.getVariableRange(TemplateDescriptor.VARIABLE_NAME)
                ?.startOffset ?: -1
        })
    }

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
        val descriptor = adapter(m_engine) ?: return
        target.addVariable(descriptor.name, descriptor.expression, descriptor.defaultValueExpression, true)
    }

    private class TemplateListener(
        private val m_templateDescriptor: TemplateDescriptor,
        private val m_editor: Editor,
        private val m_settingsManager: CodeStyleSettingsManager,
        private val m_origSettings: CodeStyleSettings?,
        private val m_positionSupplier: () -> Int
    ) : TemplateEditingAdapter() {

        override fun templateCancelled(template: Template?) {
            onTemplateCompletedOrCancelled(null)
        }

        override fun beforeTemplateFinished(state: TemplateState, template: Template) {
            onTemplateCompletedOrCancelled(state)
        }

        private fun onTemplateCompletedOrCancelled(state: TemplateState?) {
            try {
                insertInnerTypeGetter(computeCompletionPosition(state))
            } finally {
                resetTemporarySettings()
            }
        }

        private fun computeCompletionPosition(state: TemplateState?): Int {
            if (state != null) {
                val start = state.getVariableRange(TemplateDescriptor.VARIABLE_NAME)?.startOffset ?: -1
                if (start >= 0) {
                    return start
                }
            }
            return m_positionSupplier()
        }

        private fun resetTemporarySettings() {
            if (m_origSettings != null) {
                m_settingsManager.setTemporarySettings(m_origSettings)
            } else {
                m_settingsManager.dropTemporarySettings()
            }
        }

        private fun resolveInnerTypeGetterContainer(createdClass: PsiClass): Pair<PsiClass, String>? {
            val containingModule = createdClass.containingModule() ?: return null
            for (info in m_templateDescriptor.innerTypeGetterInfos()) {
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

        private fun insertInnerTypeGetter(insertPos: Int) {
            if (insertPos < 0) return
            val project = m_editor.project ?: return
            if (UndoManager.getInstance(project).isUndoInProgress) return
            val document = m_editor.document
            val psiDocumentManager = PsiDocumentManager.getInstance(project)
            val file = psiDocumentManager.getPsiFile(document) ?: return
            val element = file.findElementAt(insertPos) ?: return
            val createdClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java) ?: return
            val innerTypeGetterInfo = resolveInnerTypeGetterContainer(createdClass) ?: return

            val createdClassFqn = createdClass.qualifiedName
            val createdClassSimpleName = Strings.capitalize(createdClass.name)
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
                psiDocumentManager.commitDocument(document)
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
                .map { it to it.textRange.subSequence(document.immutableCharSequence) }
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