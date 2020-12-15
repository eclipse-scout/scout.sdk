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
package org.eclipse.scout.sdk.s2i.model.js

import com.intellij.openapi.vfs.VfsUtilCore.iterateChildrenRecursively
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.s.IWebConstants

/**
 * @param name The module name as declared by the "name" property of the package.json file (e.g. '@eclipse-scout/core').
 * @param namespace The namespace of the module (e.g. 'scout' or 'helloworld').
 * @param sourceRoot The directory containing the [JsModule.mainFile]
 * @param mainFile The [VirtualFile] the "main" property of the package.json points to.
 * @param jsModel The owning [JsModel]
 */
class JsModule(val name: String, val namespace: String, val sourceRoot: VirtualFile, val mainFile: VirtualFile, val jsModel: JsModel) {

    /**
     * All js files found within the [JsModule.sourceRoot] (recursively).
     */
    val files = collectFiles()

    private fun collectFiles(): List<VirtualFile> {
        val files = ArrayList<VirtualFile>()
        iterateChildrenRecursively(sourceRoot, this::acceptFile) { child ->
            child.takeUnless { it.isDirectory }?.let { files.add(it) }
            true // keep on iterating
        }
        return files
    }

    private fun acceptFile(fileOrDirectory: VirtualFile): Boolean {
        if (!fileOrDirectory.isValid || !fileOrDirectory.isInLocalFileSystem) {
            return false
        }
        if (fileOrDirectory.isDirectory) {
            return true
        }
        return fileOrDirectory.name.endsWith(IWebConstants.JS_FILE_SUFFIX)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JsModule
        return name == other.name
    }

    override fun hashCode() = name.hashCode()

    override fun toString() = name
}