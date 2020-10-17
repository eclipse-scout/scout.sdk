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
package org.eclipse.scout.sdk.s2i.environment

import com.intellij.codeInsight.actions.OptimizeImportsProcessor
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.util.DirectoryUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.LocalTimeCounter
import org.eclipse.scout.sdk.core.generator.compilationunit.CompilationUnitPath
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.SdkFuture
import org.eclipse.scout.sdk.core.util.SdkException
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.toIdeaProgress
import java.io.File
import java.nio.file.Path

open class CompilationUnitWriteOperation(val project: Project, val source: CharSequence, val cuPath: CompilationUnitPath) {

    var createdPsi: PsiFile? = null

    fun run(progress: IdeaProgress, resultSupplier: () -> IType?): IFuture<IType?> {
        doWriteCompilationUnit(progress)
        return SdkFuture.completed(resultSupplier, null)
    }

    fun schedule(resultSupplier: () -> IType?): IFuture<IType?> {
        val task = OperationTask(message("write.cu.x", cuPath.fileName()), project, TransactionManager.current(), this::doWriteCompilationUnit)
        return task.schedule(resultSupplier, hidden = true)
    }

    protected fun doWriteCompilationUnit(progress: IdeaProgress) {
        progress.init(3, message("write.cu.x", cuPath.fileName()))

        // create in memory file
        val newPsi = PsiFileFactory.getInstance(project)
                .createFileFromText(cuPath.fileName(), JavaFileType.INSTANCE, source, LocalTimeCounter.currentTime(), false, false)
        progress.worked(1)

        formatSource(newPsi)
        progress.worked(1)

        optimizeImports(newPsi)
        progress.worked(1)

        TransactionManager.current().register(CompilationUnitWriter(cuPath.targetFile(), newPsi))

        createdPsi = newPsi
    }

    protected fun formatSource(newJavaPsi: PsiFile) {
        try {
            IdeaEnvironment.computeInReadAction(project) { CodeStyleManager.getInstance(project).reformat(newJavaPsi) }
        } catch (e: Exception) {
            SdkLog.warning("Error formatting Java source of file '{}'.", newJavaPsi.name, e)
        }
    }

    protected fun optimizeImports(newJavaPsi: PsiFile) {
        try {
            OptimizeImportsProcessor(project, newJavaPsi).run()
        } catch (e: Exception) {
            SdkLog.warning("Error optimizing imports in file '{}'.", newJavaPsi.name, e)
        }
    }


    companion object {
        private class CompilationUnitWriter(val targetFile: Path, val psi: PsiFile) : TransactionMember {

            override fun file() = targetFile

            override fun commit(progress: IdeaProgress): Boolean {
                progress.init(2, message("write.cu.x", psi.name))

                val targetDirectory = targetFile.parent
                val dir = DirectoryUtil.mkdirs(PsiManager.getInstance(psi.project), targetDirectory.toString().replace(File.separatorChar, '/'))
                        ?: throw SdkException("Cannot write '$targetFile' because the directory could not be created.")
                progress.worked(1)

                val existingFile = dir.findFile(targetFile.fileName.toString())
                if (existingFile == null) {
                    SdkLog.debug("Add new compilation unit '{}'.", psi.name)
                    dir.add(psi)
                } else {
                    FileWriter(targetFile, psi.text, psi.project).commit(toIdeaProgress(null))
                }
                progress.worked(1)
                return true
            }

            override fun toString() = message("write.cu.x", targetFile)
        }
    }
}
