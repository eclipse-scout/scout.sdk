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

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifierAlias
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiQualifiedReferenceElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementSpi

class IdeaNodeModules(val project: Project) {

    private val m_modules = HashMap<VirtualFile, IdeaNodeModule>()
    private val m_packageJsonLocationByFile = HashMap<VirtualFile, VirtualFile?>()

    fun create(nodeModuleDir: VirtualFile) = getOrCreateModule(nodeModuleDir)

    fun clear() {
        m_modules.clear()
        m_packageJsonLocationByFile.clear()
    }

    fun resolveReferencedElement(element: JSElement): NodeElementSpi? {
        val reference = PsiTreeUtil.findChildOfType(element, PsiQualifiedReferenceElement::class.java, false) ?: return null
        return resolveReference(reference)
    }

    fun resolveImport(importSpecifier: ES6ImportSpecifier): NodeElementSpi? {
        val reference = importSpecifier.reference ?: return null
        return resolveReference(reference)
    }

    private fun resolveReference(reference: PsiReference): NodeElementSpi? {
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
        return getOrCreateModule(file)
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

    internal fun getOrCreateModule(file: VirtualFile): IdeaNodeModule? {
        val packageJsonFile = file.canonicalFile?.let { findParentPackageJson(it) } ?: return null
        return m_modules.computeIfAbsent(packageJsonFile.parent) { IdeaNodeModule(this, it) }
    }

    internal fun getModules(): Collection<IdeaNodeModule> = m_modules.values.toSet()

    fun remove(vf: VirtualFile): Collection<IdeaNodeModule> {
        val modules = m_modules
            .filter { vf.path.startsWith(it.key.path) }
            .map { it.value }

        if (modules.isEmpty()) return emptySet()

        return remove(modules)
    }

    fun remove(module: IdeaNodeModule) = remove(setOf(module))

    fun remove(modules: Collection<IdeaNodeModule>): Collection<IdeaNodeModule> {
        val dependentModules = collectDependentModules(modules)

        // remove everything that is mapped to a module contained in dependentModules
        m_modules.values.removeIf { it in dependentModules }

        return dependentModules
    }

    private fun collectDependentModules(modules: Collection<IdeaNodeModule>): Collection<IdeaNodeModule> {
        val remainingModules = m_modules.values.toMutableSet()

        val result = mutableSetOf<IdeaNodeModule>()
        var current = modules.filter { it in remainingModules }.toSet()

        while (current.isNotEmpty()) {
            // add to result
            result.addAll(current)

            // remove from remaining modules
            remainingModules.removeAll(current)

            // all modules with a dependency on one of the current modules
            current = remainingModules
                .filter { it.packageJson().dependencies().any { dependency -> dependency in current } }
                .toSet()
        }

        return result
    }
}