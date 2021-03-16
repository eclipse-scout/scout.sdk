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
package org.eclipse.scout.sdk.s2i.environment

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.util.DirectoryUtil
import com.intellij.lang.LanguageImportStatements
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.LocalTimeCounter
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.model.CompilationUnitInfo
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.SdkFuture
import org.eclipse.scout.sdk.core.util.SdkException
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import java.io.File
import java.nio.file.Path

open class CompilationUnitWriteOperation(val project: Project, val source: CharSequence, val cuInfo: CompilationUnitInfo) {

    fun run(progress: IdeaProgress, resultSupplier: () -> IType?): IFuture<IType?> {
        doWriteCompilationUnit(progress)
        return SdkFuture.completed(resultSupplier, null)
    }

    fun schedule(resultSupplier: () -> IType?): IFuture<IType?> {
        val task = OperationTask(message("write.cu.x", cuInfo.fileName()), project, TransactionManager.current(), this::doWriteCompilationUnit)
        return task.schedule(resultSupplier, hidden = true)
    }

    protected fun doWriteCompilationUnit(progress: IdeaProgress) {
        progress.init(3, message("write.cu.x", cuInfo.fileName()))

        // PSI requires all line separators to be "\n".
        // As the source might be constructed from existing source fragments, it must be cleaned to ensure no crlf exists
        val sourceClean = Strings.replace(source, "\r", "")

        // create in memory file
        val newPsi = PsiFileFactory.getInstance(project)
                .createFileFromText(cuInfo.fileName(), JavaFileType.INSTANCE, sourceClean, LocalTimeCounter.currentTime(), false, false)
        progress.worked(1)

        formatAndOptimizeImports(newPsi, progress)

        TransactionManager.current().register(CompilationUnitWriter(cuInfo.targetFile(), newPsi, newPsi.text /* create source here to have the transaction member as short as possible */))
    }

    protected fun formatAndOptimizeImports(psi: PsiFile, progress: IdeaProgress) = computeInReadAction(project) {
        try {
            CodeStyleManager.getInstance(project).reformat(psi)
            progress.worked(1)
            LanguageImportStatements.INSTANCE.forFile(psi)
                    .filter { it.supports(psi) }
                    .forEach { it.processFile(psi).run() }
            progress.worked(1)
        } catch (e: Exception) {
            SdkLog.warning("Error formatting Java source of file '{}'.", psi.name, e)
        }
    }

    private class CompilationUnitWriter(val targetFile: Path, val psi: PsiFile, val source: CharSequence) : TransactionMember {

        override fun file() = targetFile

        override fun commit(progress: IdeaProgress): Boolean {
            progress.init(2, toString())

            val project = psi.project
            val targetDirectory = targetFile.parent
            val dir = DirectoryUtil.mkdirs(PsiManager.getInstance(project), targetDirectory.toString().replace(File.separatorChar, '/'))
                    ?: throw SdkException("Cannot write '$targetFile' because the directory could not be created.")
            progress.worked(1)

            val existingFile = dir.findFile(targetFile.fileName.toString())
            if (existingFile == null) {
                SdkLog.debug("Add new compilation unit '{}'.", targetFile)
                dir.add(psi)
                progress.worked(1)
            } else {
                FileWriter(targetFile, source, project).commit(progress.newChild(1))
            }
            return true
        }

        override fun toString() = message("write.cu.x", targetFile.fileName)
    }
}
