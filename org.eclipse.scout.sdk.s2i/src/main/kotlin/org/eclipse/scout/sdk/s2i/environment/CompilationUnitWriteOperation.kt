/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
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
import com.intellij.lang.LanguageImportStatements
import com.intellij.lang.java.JavaImportOptimizer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.LocalTimeCounter
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.model.CompilationUnitInfo
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.SdkFuture
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction

open class CompilationUnitWriteOperation(val project: Project, val source: CharSequence, val cuInfo: CompilationUnitInfo) {

    lateinit var formattedSource: String

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

        formattedSource = formatAndOptimizeImports(newPsi, progress)

        TransactionManager.current().register(FileWriter(cuInfo.targetFile(), formattedSource, project))
    }

    protected fun formatAndOptimizeImports(psi: PsiFile, progress: IdeaProgress): String = computeInReadAction(project) {
        try {
            CodeStyleManager.getInstance(project).reformat(psi)
            progress.worked(1)

            val optimizers = LanguageImportStatements.INSTANCE.forFile(psi)
            if (optimizers.isEmpty()) {
                // ensure at least the Java optimizer is executed
                // in IJ >= 2021.2 the Java optimizer is not returned because JavaImportOptimizer#supports only returns true if in a source folder
                optimizers.add(JavaImportOptimizer())
            }
            optimizers.forEach { it.processFile(psi).run() }
            progress.worked(1)
        } catch (e: Exception) {
            SdkLog.warning("Error formatting Java source of file '{}'.", psi.name, e)
        }
        return@computeInReadAction psi.text /* create source here to have the transaction member as short as possible (is executed in ui thread) */
    }
}
