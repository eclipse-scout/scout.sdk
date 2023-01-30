/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.model.typescript

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifierAlias
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementSpi

class IdeaNodeModules {

    val spiFactory = IdeaSpiFactory(this)
    private val m_modules = HashMap<VirtualFile, IdeaNodeModule>()
    private val m_packageJsonLocationByFile = HashMap<VirtualFile, VirtualFile?>()

    fun create(project: Project, nodeModuleDir: VirtualFile) = getOrCreateModule(project, nodeModuleDir)

    fun resolveReferencedElement(element: JSElement): NodeElementSpi? {
        val reference = PsiTreeUtil.findChildOfType(element, JSReferenceExpression::class.java, false) ?: return null
        var referencedElement = reference.resolve()
        if (referencedElement is ES6ImportSpecifierAlias) {
            referencedElement = referencedElement.findAliasedElement()
        }
        val containingModule = findContainingModule(referencedElement) ?: return null
        val name = (referencedElement as? PsiNamedElement)?.name ?: return null
        return containingModule.exports()[name]?.referencedElement()
    }

    fun findContainingModule(element: PsiElement?): IdeaNodeModule? {
        val file = element?.containingFile?.virtualFile ?: return null
        return getOrCreateModule(element.project, file)
    }

    private fun findParentPackageJson(file: VirtualFile) = m_packageJsonLocationByFile.computeIfAbsent(file) { resolveParentPackageJson(file) }

    private fun resolveParentPackageJson(file: VirtualFile?): VirtualFile? {
        var candidate = file
        while (candidate != null) {
            val packageJson = candidate.findChild(IPackageJson.FILE_NAME)
            if (packageJson != null) {
                return packageJson
            }
            candidate = candidate.parent
        }
        return null
    }

    private fun getOrCreateModule(project: Project, file: VirtualFile): IdeaNodeModule? {
        val packageJsonFile = findParentPackageJson(file) ?: return null
        return m_modules.computeIfAbsent(packageJsonFile) { IdeaNodeModule(project, this, it.parent) }
    }
}