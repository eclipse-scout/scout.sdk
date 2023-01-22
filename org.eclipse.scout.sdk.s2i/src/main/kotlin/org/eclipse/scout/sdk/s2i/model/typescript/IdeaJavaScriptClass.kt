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

import com.intellij.lang.javascript.psi.ecmal4.JSClass
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ES6ClassImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi

open class IdeaJavaScriptClass(protected val ideaModule: IdeaNodeModule, internal val javaScriptClass: JSClass) : AbstractNodeElementSpi<IES6Class>(ideaModule), ES6ClassSpi {

    override fun createApi() = ES6ClassImplementor(this)

    override fun source() = ideaModule.sourceFor(javaScriptClass)

    override fun name() = javaScriptClass.name
}