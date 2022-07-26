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

import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.LocalTimeCounter
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.SdkFuture
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import java.nio.file.Path
import kotlin.io.path.name

open class JsWriteOperation(val project: Project, val source: CharSequence, val filePath: Path) {

    lateinit var formattedSource: String

    fun run(progress: IdeaProgress): IFuture<Void?> {
        doWriteJs(progress)
        return SdkFuture.completed(null)
    }

    fun schedule(): IFuture<Void?> {
        val fileName = filePath.fileName.name
        val task = OperationTask("Write JS file $fileName", project, TransactionManager.current(), this::doWriteJs)
        return task.schedule({ null }, hidden = true)
    }

    protected fun doWriteJs(progress: IdeaProgress) {
        val fileName = filePath.fileName.name
        progress.init(2, "Write JS file $fileName")

        // PSI requires all line separators to be "\n".
        // As the source might be constructed from existing source fragments, it must be cleaned to ensure no crlf exists
        val sourceClean = Strings.replace(source, "\r", "")

        // create in memory file
        val newPsi = PsiFileFactory.getInstance(project)
                .createFileFromText(fileName, JavaScriptFileType.INSTANCE, sourceClean, LocalTimeCounter.currentTime(), false, false)
        progress.worked(1)

        formattedSource = format(newPsi, progress)

        TransactionManager.current().register(FileWriter(filePath, formattedSource, project))
    }

    protected fun format(psi: PsiFile, progress: IdeaProgress): String = computeInReadAction(project) {
        try {
            CodeStyleManager.getInstance(project).reformat(psi)
            progress.worked(1)
        } catch (e: Exception) {
            SdkLog.warning("Error formatting JS of file '{}'.", psi.name, e)
        }
        return@computeInReadAction psi.text /* create source here to have the transaction member as short as possible (is executed in ui thread) */
    }
}
