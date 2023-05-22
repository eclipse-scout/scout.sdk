/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
        override fun build(): FileEditor = createEditor(project, file)
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor = NlsEditor(project, file)

    override fun getPolicy() = FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR
}
