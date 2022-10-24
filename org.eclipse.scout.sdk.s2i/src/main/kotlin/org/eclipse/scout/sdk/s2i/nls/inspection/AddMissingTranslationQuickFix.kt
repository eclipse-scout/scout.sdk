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

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
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
import org.eclipse.scout.sdk.core.s.nls.ITranslation
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.TransactionManager
import org.eclipse.scout.sdk.s2i.environment.TransactionMember
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec
import org.eclipse.scout.sdk.s2i.nls.TranslationLanguageSpec.Companion.translationSpec
import org.eclipse.scout.sdk.s2i.nls.TranslationManagerLoader
import org.eclipse.scout.sdk.s2i.nls.editor.TranslationNewDialog
import org.eclipse.scout.sdk.s2i.resolveLocalPath
import java.nio.file.Path
import java.util.stream.Collectors.toList

class AddMissingTranslationQuickFix(val key: String) : LocalQuickFix {

    val quickFixName = message("add.missing.translation")

    override fun getFamilyName(): String = quickFixName

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        applyFix(descriptor.psiElement)
    }

    fun applyFix(psiElement: PsiElement) {
        val module = psiElement.containingModule() ?: return
        val spec = psiElement.translationSpec() ?: return
        val manager = TranslationManagerLoader.createManager(module, spec.translationDependencyScope)
        ApplicationManager.getApplication().invokeLater { showStoreChooser(module, spec, manager) }
    }

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo = IntentionPreviewInfo.EMPTY

    private fun showStoreChooser(module: Module, spec: TranslationLanguageSpec, manager: TranslationManager?) {
        val stores = manager?.allEditableStores()?.collect(toList())
        if (stores.isNullOrEmpty()) {
            SdkLog.warning("Cannot create missing translation because no editable text provider service could be found in module '{}'.", module.name)
            return
        }

        val project = module.project
        val editor = prepareAndGetEditor(spec.element.containingFile, project) // must be executed before opening the dialog so that a potential running template can be finished
        if (stores.size == 1) {
            openDialog(project, stores[0], manager, spec)
            return
        }

        val popup = JBPopupFactory.getInstance().createListPopup(TranslationStorePopupStep(project, manager, spec, stores), 10)
        if (editor != null) {
            popup.showInBestPositionFor(editor)
        } else {
            popup.showCenteredInCurrentWindow(project)
        }
    }

    private fun prepareAndGetEditor(psiFile: PsiFile, project: Project): Editor? {
        if (!psiFile.isPhysical) return null
        val fileEditorManager = FileEditorManager.getInstance(project)
        val fileEditor = fileEditorManager.getSelectedEditor(psiFile.virtualFile) as? TextEditor ?: return null
        val editor = fileEditor.editor
        // in case the quick fix was launched directly from within a template. To show the chooser the template must first be finished
        TemplateManager.getInstance(project).finishTemplate(editor)
        return editor
    }

    private fun openDialog(project: Project, store: ITranslationStore, manager: TranslationManager, spec: TranslationLanguageSpec) {
        val dialog = TranslationNewDialog(project, store, manager, manager.generateNewKey(key))
        if (!dialog.showAndGet()) return
        val created = dialog.createdTranslation() ?: return

        callInIdeaEnvironment(project, message("store.new.translation")) { env, progress ->
            manager.flush(env, progress)
            updateTranslationKey(key, created, spec)
        }
    }

    private fun updateTranslationKey(existingKey: String, createdNlsEntry: ITranslation, translationSpec: TranslationLanguageSpec) {
        val createdKey = createdNlsEntry.key()
        if (createdKey == existingKey) return
        val path = translationSpec.element.containingFile.virtualFile.resolveLocalPath() ?: return
        TransactionManager.current().register(UpdateTranslationKeyMember(path, createdKey, translationSpec))
    }

    private class UpdateTranslationKeyMember(val path: Path, val createdKey: String, val translationSpec: TranslationLanguageSpec) : TransactionMember {
        override fun file() = path

        override fun commit(progress: IdeaProgress): Boolean {
            val newElement = translationSpec.createNewLiteral(createdKey)
            translationSpec.element.replace(newElement)
            return true
        }
    }

    private inner class TranslationStorePopupStep(val project: Project, val manager: TranslationManager, val spec: TranslationLanguageSpec, val stores: MutableList<ITranslationStore>) :
        BaseListPopupStep<ITranslationStore>(message("create.new.translation.in"), stores, AllIcons.Nodes.Services) {

        init {
            defaultOptionIndex = 0
        }

        override fun getTextFor(value: ITranslationStore): String = value.service().type().name()

        override fun onChosen(selectedValue: ITranslationStore, finalChoice: Boolean): PopupStep<*>? = doFinalStep {
            openDialog(project, selectedValue, manager, spec)
        }

        override fun isSpeedSearchEnabled() = stores.size > 3
    }
}