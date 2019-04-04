package org.eclipse.scout.sdk.s2i.environment

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.environment.IFuture
import java.nio.file.Path
import java.util.Collections.singleton

open class FileWriter(private val file: Path, private val content: CharSequence, val project: Project, private val vFile: VirtualFile?) {

    constructor(file: Path, content: CharSequence, project: Project) : this(file, content, project, LocalFileSystem.getInstance().findFileByIoFile(file.toFile()))

    fun run(progress: IdeaProgress) =
            IdeaEnvironment.computeInWriteAction(project) { doWriteFile(progress) }

    fun schedule(): IFuture<Void> = OperationTask({ p -> run(IdeaEnvironment.toIdeaProgress(p)) }, "Write " + file.fileName, project).schedule(null)

    protected fun doWriteFile(progress: IdeaProgress) {
        progress.init("Write file " + file.fileName, 4)

        var existingFile = vFile
        if (existingFile?.exists() != true) {
            // new file
            val dir = VfsUtil.createDirectoryIfMissing(file.parent.toString())
            if (dir == null) {
                SdkLog.warning("Cannot write '{}' because the directory could not be created.", file)
                return
            }
            progress.worked(1)
            existingFile = dir.createChildData(this, file.fileName.toString())
            progress.worked(1)
        }
        progress.setWorkRemaining(2)

        val status = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(singleton(existingFile))
        progress.worked(1)
        if (status.hasReadonlyFiles()) {
            SdkLog.warning("Cannot save file '{}' because it is read only.", file)
        } else {
            existingFile.setBinaryContent(content.toString().toByteArray(existingFile.charset))
            progress.worked(1)
        }
        progress.setWorkRemaining(0)
    }
}