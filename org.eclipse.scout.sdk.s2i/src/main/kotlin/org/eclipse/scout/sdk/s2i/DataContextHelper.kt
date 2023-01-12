/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i

import com.intellij.analysis.AnalysisScope
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager

open class DataContextHelper(val context: DataContext) {

    fun psiElement(): PsiElement? = CommonDataKeys.PSI_ELEMENT.getData(context)

    fun psiFile(): PsiFile? = CommonDataKeys.PSI_FILE.getData(context) ?: psiElement()?.containingFile

    fun virtualFile(): VirtualFile? = CommonDataKeys.VIRTUAL_FILE.getData(context) ?: psiFile()?.virtualFile

    fun editor() = CommonDataKeys.EDITOR.getData(context)

    fun scope(): AnalysisScope? {
        val project = project() ?: return null

        val psiFile = psiFile()
        if (psiFile != null) {
            return AnalysisScope(psiFile)
        }

        val virtualFile = virtualFile()
        if (virtualFile != null) {
            if (virtualFile.isDirectory) {
                val psiDirectory = PsiManager.getInstance(project).findDirectory(virtualFile)
                if (psiDirectory != null) {
                    return AnalysisScope(psiDirectory)
                }
            }
            return AnalysisScope(project, listOf(virtualFile))
        }

        return AnalysisScope(project)
    }

    fun module(): Module? {
        val module = psiElement()?.containingModule() ?: ModuleUtilCore.findModuleForFile(psiFile())
        if (module != null) {
            return module
        }

        val project = project()
        val virtualFile = virtualFile()
        if (project != null && virtualFile != null) {
            return ModuleUtilCore.findModuleForFile(virtualFile, project)
        }
        return null
    }

    fun project(): Project? = CommonDataKeys.PROJECT.getData(context)
}
