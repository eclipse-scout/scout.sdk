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
import com.intellij.ide.util.DirectoryUtil
import com.intellij.openapi.fileTypes.StdFileTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
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
import java.io.File
import java.nio.file.Path

open class CompilationUnitWriteOperation(val project: Project, val source: CharSequence, val cuPath: CompilationUnitPath) {

    var createdPsi: PsiFile? = null

    fun run(progress: IdeaProgress, resultSupplier: () -> IType?): IFuture<IType?> {
        doWriteCompilationUnit(progress)
        return SdkFuture.completed(resultSupplier, null)
    }

    fun schedule(resultSupplier: () -> IType?): IFuture<IType?> {
        val task = OperationTask("Write " + cuPath.fileName(), project, TransactionManager.current(), this::doWriteCompilationUnit)
        return task.schedule(resultSupplier, hidden = true)
    }

    protected fun doWriteCompilationUnit(progress: IdeaProgress) {
        progress.init(3, "Write {}", cuPath.fileName())

        // create in memory file
        val newPsi = PsiFileFactory.getInstance(project).createFileFromText(cuPath.fileName(), StdFileTypes.JAVA, source, LocalTimeCounter.currentTime(), false, false)
        progress.worked(1)

        formatSource(newPsi)
        progress.worked(1)

        optimizeImports(newPsi)
        progress.worked(1)

        registerCompilationUnit(newPsi)

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

    protected fun registerCompilationUnit(psi: PsiFile) {
        val existingFile = LocalFileSystem.getInstance().findFileByIoFile(cuPath.targetFile().toFile())
        if (existingFile?.exists() == true) {
            // update existing file
            TransactionManager
                    .current()
                    .register(FileWriter(cuPath.targetFile(), psi.text, project, existingFile))
        } else {
            // new file
            TransactionManager
                    .current()
                    .register(NewCompilationUnitWriter(psi, cuPath.targetFile()))
        }
    }

    companion object {
        private class NewCompilationUnitWriter(val psi: PsiFile, val targetFile: Path) : TransactionMember {

            override fun file() = targetFile

            override fun commit(progress: IdeaProgress): Boolean {
                progress.init(2, "Write {}", psi.name)
                val targetDirectory = targetFile.parent
                val dir = DirectoryUtil.mkdirs(PsiManager.getInstance(psi.project), targetDirectory.toString().replace(File.separatorChar, '/'))
                        ?: throw SdkException("Cannot write '$targetFile' because the directory could not be created.")
                progress.worked(1)

                dir.add(psi)
                progress.worked(1)
                return true
            }
        }
    }
}
