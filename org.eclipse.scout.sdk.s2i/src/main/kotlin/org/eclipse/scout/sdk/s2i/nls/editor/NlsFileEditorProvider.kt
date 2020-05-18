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
package org.eclipse.scout.sdk.s2i.nls.editor

import com.intellij.openapi.fileEditor.AsyncFileEditorProvider
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.nls.NlsFileType

class NlsFileEditorProvider : FileEditorProvider, AsyncFileEditorProvider, DumbAware {

    override fun getEditorTypeId(): String = "Scout.nls"

    override fun accept(project: Project, file: VirtualFile) =
            file.isValid
                    && !file.isDirectory
                    && file.exists()
                    && project.isInitialized
                    && file.containingModule(project) != null
                    && file.fileType == NlsFileType.INSTANCE

    override fun createEditorAsync(project: Project, file: VirtualFile): AsyncFileEditorProvider.Builder = object : AsyncFileEditorProvider.Builder() {
        override fun build(): FileEditor {
            return NlsEditor(project, file)
        }
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor = createEditorAsync(project, file).build()

    override fun getPolicy() = FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR
}
