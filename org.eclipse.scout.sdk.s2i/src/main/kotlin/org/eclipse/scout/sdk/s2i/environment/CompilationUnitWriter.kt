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
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.environment.Future
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.util.JavaTypes
import org.eclipse.scout.sdk.core.util.SdkException
import java.io.File
import java.nio.file.Path

open class CompilationUnitWriter(val project: Project, val source: CharSequence, packageName: String, classSimpleName: String, sourceFolder: Path) {

    val fileName = classSimpleName + JavaTypes.JAVA_FILE_SUFFIX
    val targetDirectory: Path = sourceFolder.resolve(packageName.replace(JavaTypes.C_DOT, File.separatorChar))
    val targetFile: Path = targetDirectory.resolve(fileName)

    var createdPsi: PsiFile? = null

    fun run(progress: IdeaProgress, resultSupplier: () -> IType?): IFuture<IType?> {
        doWriteCompilationUnit(progress)
        return Future.completed(resultSupplier, null)
    }

    fun schedule(resultSupplier: () -> IType?): IFuture<IType?> {
        val task = OperationTask("Write $fileName", project, this::doWriteCompilationUnit)
        return task.schedule(resultSupplier)
    }

    protected fun doWriteCompilationUnit(progress: IdeaProgress) {
        progress.init("Write $fileName", 3)

        // create in memory file
        val newPsi = PsiFileFactory.getInstance(project).createFileFromText(fileName, StdFileTypes.JAVA, source, LocalTimeCounter.currentTime(), false, false)
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
        val existingFile = LocalFileSystem.getInstance().findFileByIoFile(targetFile.toFile())
        if (existingFile?.exists() == true) {
            // update existing file
            TransactionManager
                    .current()
                    .register(FileWriter(targetFile, psi.text, project, existingFile))
        } else {
            // new file
            TransactionManager
                    .current()
                    .register(NewCompilationUnitWriter(psi, targetFile))
        }
    }

    companion object {
        private class NewCompilationUnitWriter(val psi: PsiFile, val targetFile: Path) : TransactionMember {

            override fun file() = targetFile

            override fun commit(progress: IdeaProgress): Boolean {
                progress.init("Write ${psi.name}", 2)
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
