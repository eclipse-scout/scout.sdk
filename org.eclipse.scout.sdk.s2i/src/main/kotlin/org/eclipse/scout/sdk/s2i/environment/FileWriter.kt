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

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.log.SdkLog
import java.nio.file.Path

open class FileWriter(val targetFile: Path, private val content: CharSequence, val project: Project, private val vFile: VirtualFile?) : TransactionMember {

    constructor(file: Path, content: CharSequence, project: Project) : this(file, content, project, null)

    override fun file(): Path = targetFile

    override fun commit(progress: IdeaProgress): Boolean {
        progress.init(4, "Write file {}", targetFile.fileName)

        var existingFile = vFile ?: LocalFileSystem.getInstance().findFileByIoFile(targetFile.toFile())
        if (existingFile?.exists() != true) {
            // new file
            val dir = VfsUtil.createDirectoryIfMissing(targetFile.parent.toString())
            if (dir == null) {
                SdkLog.warning("Cannot write '$targetFile' because the directory could not be created.")
                return false
            }
            progress.worked(1)
            existingFile = dir.createChildData(this, targetFile.fileName.toString())
            progress.worked(1)
        }
        progress.setWorkRemaining(1)

        existingFile.setBinaryContent(content.toString().toByteArray(existingFile.charset))
        progress.worked(1)
        return true
    }
}