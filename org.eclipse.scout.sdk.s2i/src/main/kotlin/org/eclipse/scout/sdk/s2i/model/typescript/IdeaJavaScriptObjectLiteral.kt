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

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral
import org.eclipse.scout.sdk.core.typescript.model.api.internal.ObjectLiteralImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.ObjectLiteralSpi

open class IdeaJavaScriptObjectLiteral(protected val ideaModule: IdeaNodeModule, protected val jsObjectLiteral: JSObjectLiteralExpression) : AbstractNodeElementSpi<IObjectLiteral>(ideaModule), ObjectLiteralSpi {
    
    override fun createApi() = ObjectLiteralImplementor(this)

    override fun source() = ideaModule.sourceFor(jsObjectLiteral)

    override fun name() = ""
}
