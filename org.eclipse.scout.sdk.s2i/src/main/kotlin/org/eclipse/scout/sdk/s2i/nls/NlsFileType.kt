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
package org.eclipse.scout.sdk.s2i.nls

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.Companion.message
import java.nio.charset.StandardCharsets
import javax.swing.Icon

class NlsFileType private constructor() : FileType {

    companion object {
        @JvmField
        val INSTANCE: NlsFileType = NlsFileType()
    }

    override fun getDefaultExtension() = "nls"

    override fun getIcon(): Icon = AllIcons.Nodes.ResourceBundle

    override fun getCharset(file: VirtualFile, content: ByteArray): String = StandardCharsets.UTF_8.name()

    override fun getName() = message("nls.file.desc")

    override fun getDescription() = message("nls.file.desc")

    override fun isBinary() = false

    override fun isReadOnly() = false

}
