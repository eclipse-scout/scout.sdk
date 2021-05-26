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
package org.eclipse.scout.sdk.s2i.element

import com.intellij.codeInsight.daemon.JavaErrorBundle
import com.intellij.codeInsight.daemon.impl.analysis.HighlightClassUtil
import com.intellij.ide.ui.newItemPopup.NewItemPopupUtil
import com.intellij.ide.ui.newItemPopup.NewItemSimplePopupPanel
import com.intellij.ide.util.EditorHelper
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameHelper
import com.intellij.psi.util.PsiUtil
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.util.ScoutTier
import org.eclipse.scout.sdk.s2i.*
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.util.SourceFolderHelper
import java.util.*
import java.util.function.BiConsumer

abstract class CreateElementAction<OP : BiConsumer<IEnvironment, IProgress>>(val text: String, val description: String) : AnAction(text, description, null) {

    override fun actionPerformed(e: AnActionEvent) {
        val dataContext = e.dataContext
        val view = LangDataKeys.IDE_VIEW.getData(dataContext) ?: return
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return
        val dir = view.orChooseDirectory ?: return

        createPopup(dir) { EditorHelper.openInEditor(it, true, true) }.showCenteredInCurrentWindow(project)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val dataContext = e.dataContext
        val presentation = e.presentation
        val file = LangDataKeys.VIRTUAL_FILE.getData(dataContext)

        if (file == null || !file.isDirectory) {
            presentation.isEnabledAndVisible = false
            return
        }

        val project = CommonDataKeys.PROJECT.getData(dataContext)
        if (project == null) {
            presentation.isEnabledAndVisible = false
            return
        }

        val fileIndex = ProjectFileIndex.getInstance(project)
        val sourceFolder = fileIndex.getSourceFolder(file)

        if (sourceFolder == null || sourceFolder.isTestSource) {
            presentation.isEnabledAndVisible = false
            return
        }

        presentation.isEnabledAndVisible = true
    }

    protected fun createPopup(dir: PsiDirectory, openPsiElement: (PsiElement) -> Unit): JBPopup {
        val contentPanel = NewItemSimplePopupPanel()
        val nameField = contentPanel.textField
        val popup = NewItemPopupUtil.createNewItemPopup(text, contentPanel, nameField)
        contentPanel.setApplyAction {
            val name = nameField.text
            val errorMessage = validateInput(dir, name)
            if (errorMessage != null) {
                contentPanel.setError(errorMessage)
            } else {
                createElement(name, dir, openPsiElement)
                popup.closeOk(it)
            }
        }
        return popup
    }

    protected fun validateInput(dir: PsiDirectory, name: String): String? {
        var errorMessage: String? = null
        val psiNameHelper = PsiNameHelper.getInstance(dir.project)
        val level = PsiUtil.getLanguageLevel(dir)
        if (!psiNameHelper.isQualifiedName(name) || !psiNameHelper.isQualifiedName(name.toLowerCase()) || name.contains("$")) {
            errorMessage = EclipseScoutBundle.message("invalid.java.qualified.name")
        }
        val shortName = StringUtil.getShortName(name)
        if (HighlightClassUtil.isRestrictedIdentifier(shortName, level)) {
            errorMessage = JavaErrorBundle.message("restricted.identifier", shortName)
        }
        return errorMessage
    }

    protected fun createElement(name: String, dir: PsiDirectory, openPsiElement: (PsiElement) -> Unit) {
        IdeaEnvironment.callInIdeaEnvironment(dir.project, description) { env, progress ->
            val project = env.project
            val fileIndex = ProjectFileIndex.getInstance(project)
            val sourceFolder = fileIndex.getSourceFolder(dir.virtualFile)

            if (sourceFolder?.isTestSource == true) {
                SdkLog.warning("No source folder could be determined for location '{}'", dir.virtualFile)
                return@callInIdeaEnvironment null
            }

            val sourceFolderHelper = SourceFolderHelper(project, sourceFolder!!) { env.findClasspathEntry(it) }

            if (sourceFolderHelper.classpathEntry() == null) {
                SdkLog.warning("No source folder could be determined for location '{}'", dir.virtualFile)
                return@callInIdeaEnvironment null
            }
            if (!startScoutTiers().contains(sourceFolderHelper.tier())) {
                SdkLog.warning("Location '{}' is not a {} module", dir.virtualFile, startScoutTiers().joinToString())
                return@callInIdeaEnvironment null
            }

            var pkg = fileIndex.getPackageNameByDirectory(dir.virtualFile)
            val lastIndexOf = name.lastIndexOf(".")
            if (lastIndexOf > 0) pkg += "." + name.substring(0, lastIndexOf)

            val op = sourceFolderHelper.classpathEntry()?.javaEnvironment()?.let {
                EclipseScoutBundle.elementCreationManager()
                        .createOperation(operationClass(), name.substring(lastIndexOf + 1, name.length), pkg, sourceFolderHelper, it)
            } ?: return@callInIdeaEnvironment null
            op.accept(env, progress)
            return@callInIdeaEnvironment psiClassToOpen(op)
        }.thenAccept {
            DumbService.getInstance(dir.project).smartInvokeLater {
                it.get()?.invoke()?.let { psi -> openPsiElement(psi) }
            }
        }.exceptionally {
            SdkLog.error("Error creating {}.", text, it)
            null
        }
    }

    protected open fun startScoutTiers(): Collection<ScoutTier> = listOf(ScoutTier.Client, ScoutTier.Shared, ScoutTier.Server)

    protected abstract fun operationClass(): Class<OP>

    protected abstract fun psiClassToOpen(op: OP): () -> PsiClass?
}
