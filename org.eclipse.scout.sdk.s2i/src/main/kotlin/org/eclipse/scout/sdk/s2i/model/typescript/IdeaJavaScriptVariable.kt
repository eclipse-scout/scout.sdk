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

import com.intellij.lang.javascript.psi.JSVariable
import org.eclipse.scout.sdk.core.typescript.model.api.IVariable
import org.eclipse.scout.sdk.core.typescript.model.api.internal.VariableImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.VariableSpi

open class IdeaJavaScriptVariable(protected val ideaModule: IdeaNodeModule, internal val javaScriptVariable: JSVariable) : AbstractNodeElementSpi<IVariable>(ideaModule), VariableSpi {

    override fun createApi() = VariableImplementor(this)

    override fun source() = ideaModule.sourceFor(javaScriptVariable)

    override fun name() = javaScriptVariable.name

    override fun constantValue() = ideaModule.spiFactory.createConstantValue(javaScriptVariable.initializer)
}