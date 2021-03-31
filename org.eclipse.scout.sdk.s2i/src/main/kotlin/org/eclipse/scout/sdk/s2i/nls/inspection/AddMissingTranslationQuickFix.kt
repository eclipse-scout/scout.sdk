/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.nls.inspection

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec.Companion.translationDependencyScope
import org.eclipse.scout.sdk.s2i.nls.TranslationStoreStackLoader
import org.eclipse.scout.sdk.s2i.nls.editor.TranslationNewDialog
import java.util.stream.Collectors.toList

class AddMissingTranslationQuickFix(val key: CharSequence) : LocalQuickFix {

    val quickFixName = message("add.missing.translation")
    private val m_saveTasks = ArrayList<(ITranslationEntry, IEnvironment, IProgress) -> Unit>()

    override fun getFamilyName(): String = quickFixName

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        applyFix(descriptor.psiElement)
    }

    fun applyFix(psiElement: PsiElement) {
        val module = psiElement.containingModule() ?: return
        val scope = psiElement.translationDependencyScope() ?: return
        val stack = TranslationStoreStackLoader.createStack(module, scope)
        ApplicationManager.getApplication().invokeLater { showStoreChooser(module, psiElement.containingFile, stack) }
    }

    /**
     * Add custom save tasks to the [LocalQuickFix].
     * These tasks are executed in a worker thread when the missing translation is added.
     * It therefore requires the [TranslationNewDialog] to be finished with "ok".
     * It is not necessary to save the new [ITranslationEntry]. This is done automatically.
     *
     * The [ITranslationEntry] passed to the save task is the one created in the [TranslationNewDialog].
     */
    fun withSaveTask(task: (ITranslationEntry, IEnvironment, IProgress) -> Unit) = apply {
        m_saveTasks.add(task)
    }

    private fun showStoreChooser(module: Module, psiFile: PsiFile, stack: TranslationStoreStack?) {
        val stores = stack?.allEditableStores()?.collect(toList())
        if (stores == null || stores.isEmpty()) {
            SdkLog.warning("Cannot create missing translation because no editable text provider service could be found in module '{}'.", module.name)
            return
        }

        val project = module.project
        val editor = prepareAndGetEditor(psiFile) // must be executed before opening the dialog so that a potential running template can be finished
        if (stores.size == 1) {
            openDialog(project, stores[0], stack)
            return
        }

        val popup = JBPopupFactory.getInstance().createListPopup(TranslationStorePopupStep(project, stack, stores), 10)
        if (editor != null) {
            popup.showInBestPositionFor(editor)
        } else {
            popup.showCenteredInCurrentWindow(project)
        }
    }

    private fun prepareAndGetEditor(psiFile: PsiFile): Editor? {
        if (!psiFile.isPhysical) return null
        val project = psiFile.project
        val fileEditorManager = FileEditorManager.getInstance(project)
        val fileEditor = fileEditorManager.getSelectedEditor(psiFile.virtualFile) as? TextEditor ?: return null
        val editor = fileEditor.editor
        // in case the quick fix was launched directly from within a template. To show the chooser the template must first be finished
        TemplateManager.getInstance(project).finishTemplate(editor)
        return editor
    }

    private fun openDialog(project: Project, store: ITranslationStore, stack: TranslationStoreStack) {
        val dialog = TranslationNewDialog(project, store, stack, stack.generateNewKey(key.toString()))
        val ok = dialog.showAndGet()
        if (ok) {
            val created = dialog.createdTranslation() ?: return
            callInIdeaEnvironment(project, message("store.new.translation")) { env, progress ->
                stack.flush(env, progress)
                m_saveTasks.forEach { it(created, env, progress) }
            }
        }
    }

    private inner class TranslationStorePopupStep(val project: Project, val stack: TranslationStoreStack, val stores: MutableList<ITranslationStore>) :
            BaseListPopupStep<ITranslationStore>(message("create.new.translation.in"), stores, AllIcons.Nodes.Services) {

        init {
            defaultOptionIndex = 0
        }

        override fun getTextFor(value: ITranslationStore): String = value.service().type().name()

        override fun onChosen(selectedValue: ITranslationStore, finalChoice: Boolean): PopupStep<*>? = doFinalStep {
            openDialog(project, selectedValue, stack)
        }

        override fun isSpeedSearchEnabled() = stores.size > 3
    }
}