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

import com.intellij.lang.javascript.psi.JSElement
import org.eclipse.scout.sdk.core.typescript.model.api.IExportFrom
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ExportFromImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.ExportFromSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementSpi

open class IdeaExportFrom(protected val ideaModule: IdeaNodeModule, internal val exportDeclaration: JSElement, private val name: String, private val exportedElement: NodeElementSpi) : AbstractNodeElementSpi<IExportFrom>(ideaModule),
    ExportFromSpi {

    override fun createApi() = ExportFromImplementor(this)

    override fun source() = ideaModule.sourceFor(exportDeclaration)

    override fun name() = name

    override fun element() = exportedElement
}