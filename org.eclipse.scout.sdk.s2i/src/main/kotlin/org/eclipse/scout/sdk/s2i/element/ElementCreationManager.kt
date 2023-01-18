/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.element

import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.s2i.util.SourceFolderHelper
import java.util.function.BiConsumer

interface ElementCreationManager {

    val operationMap: OperationMap

    fun <OP : BiConsumer<IEnvironment, IProgress>> createOperation(opClass: Class<OP>, elementName: String, pkg: String?, sourceFolderHelper: SourceFolderHelper, javaEnv: IJavaEnvironment): OP? {
        return operationMap.get(opClass)?.createOperation(elementName, pkg, sourceFolderHelper, javaEnv)
    }

    interface OperationStrategy<OP : BiConsumer<IEnvironment, IProgress>> {

        var createOperationFunc: (IJavaEnvironment) -> OP

        var prepareOperationFuncList: MutableList<(OP, String, String?, SourceFolderHelper) -> Unit>

        fun createOperation(elementName: String, pkg: String?, sourceFolderHelper: SourceFolderHelper, javaEnv: IJavaEnvironment): OP {
            val op = createOperationFunc(javaEnv)
            prepareOperationFuncList.forEach { it(op, elementName, pkg, sourceFolderHelper) }
            return op
        }
    }

    class OperationMap {
        private val m_map: HashMap<Class<out BiConsumer<IEnvironment, IProgress>>, OperationStrategy<out BiConsumer<IEnvironment, IProgress>>> = HashMap()

        fun <OP : BiConsumer<IEnvironment, IProgress>> put(opClass: Class<OP>, opStrategy: OperationStrategy<OP>) {
            m_map[opClass] = opStrategy
        }

        fun <OP : BiConsumer<IEnvironment, IProgress>> get(opClass: Class<OP>): OperationStrategy<OP>? {
            val operationStrategy = m_map[opClass] ?: return null
            @Suppress("UNCHECKED_CAST")
            return operationStrategy as OperationStrategy<OP>
        }
    }

    fun elementNameWithSuffix(elementName: String, suffix: String?) = if (suffix?.let { elementName.endsWith(it) } == true) elementName else elementName.plus(suffix)
}
