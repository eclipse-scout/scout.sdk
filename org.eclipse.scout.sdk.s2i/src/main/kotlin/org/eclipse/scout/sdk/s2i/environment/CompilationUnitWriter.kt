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
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.environment.Future
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.core.util.JavaTypes
import org.eclipse.scout.sdk.core.util.SdkException
import java.io.File
import java.nio.file.Path

open class CompilationUnitWriter(private val project: Project, private val source: CharSequence, packageName: String, classSimpleName: String, sourceFolder: Path) {

    @Suppress("unused")
    constructor(generator: ICompilationUnitGenerator<*>, targetFolder: IClasspathEntry, env: IdeaEnvironment) :
            this(env.project, env.createResource(generator, targetFolder.javaEnvironment()),
                    generator.packageName().orElse(""),
                    generator.elementName().orElseThrow { Ensure.newFail("File name missing in generator") },
                    targetFolder.path())

    private val m_fileName = classSimpleName + JavaTypes.JAVA_FILE_SUFFIX
    private val m_targetDirectory = sourceFolder.resolve(packageName.replace(JavaTypes.C_DOT, File.separatorChar))
    private val m_targetFile = m_targetDirectory.resolve(m_fileName)

    var formattedCode: CharSequence? = null

    fun run(progress: IdeaProgress, resultSupplier: () -> IType?): IFuture<IType?> {
        doWriteCompilationUnit(progress)
        return Future.completed(resultSupplier, null)
    }

    fun schedule(resultSupplier: () -> IType?): IFuture<IType?> {
        val job = OperationTask({ p -> doWriteCompilationUnit(IdeaEnvironment.toIdeaProgress(p)) }, "Write $m_fileName", project)
        return job.schedule(resultSupplier)
    }

    protected fun doWriteCompilationUnit(progress: IdeaProgress) {
        progress.init("Write $m_fileName", 5)

        // create in memory file
        val newJavaPsi = PsiFileFactory.getInstance(project).createFileFromText(m_fileName, StdFileTypes.JAVA, source, LocalTimeCounter.currentTime(), false, false)
        progress.worked(1)

        formatSource(newJavaPsi)
        progress.worked(1)

        optimizeImports(newJavaPsi)
        progress.worked(1)

        storeCompilationUnitContent(newJavaPsi, progress.newChild(2))
        formattedCode = newJavaPsi.text
    }

    protected fun formatSource(newJavaPsi: PsiFile) {
        try {
            IdeaEnvironment.computeInReadAction(project) { CodeStyleManager.getInstance(project).reformat(newJavaPsi) }
        }
        catch(e: Exception) {
            SdkLog.warning("Error formatting Java source of file '{}'.", newJavaPsi.name, e)
        }
    }

    protected fun optimizeImports(newJavaPsi: PsiFile) {
        try {
            OptimizeImportsProcessor(project, newJavaPsi).run()
        }
        catch(e: Exception) {
            SdkLog.warning("Error optimizing imports in file '{}'.", newJavaPsi.name, e)
        }
    }

    protected fun storeCompilationUnitContent(psi: PsiFile, progress: IdeaProgress) {
        val existingFile = LocalFileSystem.getInstance().findFileByIoFile(m_targetFile.toFile())
        if (existingFile?.exists() == true) {
            // update existing file
            FileWriter(m_targetFile, psi.text, project, existingFile).run(progress)
        } else {
            // new file
            IdeaEnvironment.computeInWriteAction(project) { writeNewJavaFile(psi, progress) }
        }
    }

    protected fun writeNewJavaFile(psi: PsiFile, progress: IdeaProgress) {
        progress.init("Write $m_fileName", 2)

        val dir = DirectoryUtil.mkdirs(PsiManager.getInstance(project), m_targetDirectory.toString().replace(File.separatorChar, '/'))
                ?: throw SdkException("Cannot write '{}' because the directory could not be created.", m_targetFile)
        progress.worked(1)

        dir.add(psi)
        progress.worked(1)
    }
}