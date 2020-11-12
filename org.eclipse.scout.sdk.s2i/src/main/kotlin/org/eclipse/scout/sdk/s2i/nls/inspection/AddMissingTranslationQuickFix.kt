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
package org.eclipse.scout.sdk.s2i.nls.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
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
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import org.eclipse.scout.sdk.s2i.nls.TranslationStoreStackLoader
import org.eclipse.scout.sdk.s2i.nls.editor.TranslationNewDialog
import org.eclipse.scout.sdk.s2i.nlsDependencyScope
import java.util.stream.Collectors.toList

class AddMissingTranslationQuickFix(val key: CharSequence) : LocalQuickFix {

    val quickFixName = message("add.missing.translation")

    override fun getFamilyName(): String = quickFixName

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        applyFix(descriptor.psiElement)
    }

    fun applyFix(psiElement: PsiElement) {
        val module = psiElement.containingModule() ?: return
        val scope = psiElement.nlsDependencyScope() ?: return
        val psiFile = psiElement.containingFile
        val stack = TranslationStoreStackLoader.createStack(module, scope)
        ApplicationManager.getApplication().invokeLater { showStoreChooser(module, psiFile, stack) }
    }

    private fun showStoreChooser(module: Module, psiFile: PsiFile, stack: TranslationStoreStack?) {
        val stores = stack?.allEditableStores()?.collect(toList())
        if (stores == null || stores.isEmpty()) {
            SdkLog.warning("Cannot create missing translation because no editable text provider service could be found in module '{}'.", module.name)
            return
        }

        val project = module.project
        if (stores.size == 1) {
            openDialog(project, stores[0], stack)
            return
        }

        val fileEditor = if (psiFile.isPhysical) FileEditorManager.getInstance(project).getSelectedEditor(psiFile.virtualFile) else null
        val editor = if (fileEditor is TextEditor) fileEditor.editor else null
        val popup = JBPopupFactory.getInstance().createListPopup(TranslationStorePopupStep(project, stack, stores), 10)
        if (editor != null) {
            popup.showInBestPositionFor(editor)
        } else {
            popup.showCenteredInCurrentWindow(project)
        }
    }

    private fun openDialog(project: Project, store: ITranslationStore, stack: TranslationStoreStack) {
        val dialog = TranslationNewDialog(project, store, stack, stack.generateNewKey(key.toString()))
        val ok = dialog.showAndGet()
        if (ok) {
            callInIdeaEnvironment(project, message("store.new.translation")) { env, progress -> stack.flush(env, progress) }
            // no need to call DaemonCodeAnalyzer.getInstance(project).restart() after flush because it is triggered automatically
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