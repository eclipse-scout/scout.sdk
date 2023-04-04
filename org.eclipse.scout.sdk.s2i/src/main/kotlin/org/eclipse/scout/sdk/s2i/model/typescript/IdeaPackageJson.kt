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

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement
import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson
import org.eclipse.scout.sdk.core.typescript.model.api.JsonPointer
import org.eclipse.scout.sdk.core.typescript.model.api.JsonPointer.IJsonPointerElement
import org.eclipse.scout.sdk.core.typescript.model.api.internal.PackageJsonImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.PackageJsonSpi
import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.SdkException
import org.eclipse.scout.sdk.s2i.model.typescript.util.NodeModuleUtils
import org.eclipse.scout.sdk.s2i.resolveLocalPath
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors
import java.util.stream.StreamSupport

class IdeaPackageJson(private val ideaModule: IdeaNodeModule, private val moduleDir: VirtualFile) : AbstractNodeElementSpi<IPackageJson>(ideaModule), PackageJsonSpi {

    private val m_dependencies = FinalValue<Collection<NodeModuleSpi>>()
    private val m_packageJson = moduleDir.findChild(IPackageJson.FILE_NAME) ?: throw SdkException("Invalid Node module dir: '{}'. No {} found in this directory.", moduleDir, IPackageJson.FILE_NAME)

    private val m_root by lazy { InputStreamReader(content()).use { JsonParser.parseReader(it) }.asJsonObject }

    override fun createApi() = PackageJsonImplementor(this)

    override fun content() = m_packageJson.inputStream

    override fun exportType() = INodeElement.ExportType.NONE

    override fun containingDir() = moduleDir.resolveLocalPath()

    override fun resolveContainingFile(): Path = api().location()

    override fun existsFile(relPath: String) = moduleDir.findFileByRelativePath(relPath) != null

    override fun dependencies(): Collection<NodeModuleSpi> = m_dependencies.computeIfAbsentAndGet {
        NodeModuleUtils.findDependenciesInNodeModulesDirs(moduleDir).asSequence()
            .mapNotNull { ideaModule.moduleInventory.getOrCreateModule(it) }
            .toSet()
    }

    override fun getString(name: String?) = m_root[name]?.takeIf { it.isJsonPrimitive }?.asString

    override fun find(pointer: JsonPointer): Any? {
        return Optional.ofNullable(pointer.find(GsonPointerElement(m_root)))
            .map { extractValue((it as GsonPointerElement).element) }
            .orElse(null)
    }


    private fun extractValue(value: JsonElement): Any? {
        if (value.isJsonNull) {
            return null
        }
        if (value.isJsonObject) {
            return value.asJsonObject.entrySet().stream()
                .collect(Collectors.toMap({ it.key }, { extractValue(it.value) }, Ensure::failOnDuplicates, { LinkedHashMap() }))
        }
        if (value.isJsonArray) {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(value.asJsonArray.iterator(), Spliterator.ORDERED), false)
                .map { extractValue(it) }
                .toList()
        }
        val primitive = value.asJsonPrimitive
        if (primitive.isBoolean) {
            return primitive.asBoolean
        }
        if (primitive.isNumber) {
            return primitive.asBigDecimal
        }
        return primitive.asString
    }

    private inner class GsonPointerElement(val element: JsonElement) : IJsonPointerElement {
        override fun arrayLength(): Int {
            if (!element.isJsonArray) return 0
            return element.asJsonArray.size()
        }

        override fun isObject() = element.isJsonObject

        override fun element(name: String?): IJsonPointerElement? {
            val element = element.asJsonObject[name] ?: return null
            return GsonPointerElement(element)
        }

        override fun element(index: Int): IJsonPointerElement? {
            val arr = element.asJsonArray
            if (index >= arr.size()) return null
            val element = arr[index] ?: return null
            return GsonPointerElement(element)
        }
    }
}