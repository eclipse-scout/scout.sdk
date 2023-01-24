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

import com.intellij.lang.javascript.psi.JSField
import com.intellij.lang.javascript.psi.JSOptionalOwner
import org.eclipse.scout.sdk.core.typescript.model.api.IField
import org.eclipse.scout.sdk.core.typescript.model.api.internal.FieldImplementor
import org.eclipse.scout.sdk.core.typescript.model.spi.AbstractNodeElementSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.FieldSpi

open class IdeaJavaScriptField(protected val ideaModule: IdeaNodeModule, protected val javaScriptField: JSField) : AbstractNodeElementSpi<IField>(ideaModule), FieldSpi {
    override fun createApi() = FieldImplementor(this)

    override fun source() = ideaModule.sourceFor(javaScriptField)

    override fun name() = javaScriptField.name

    override fun isOptional(): Boolean = (javaScriptField as? JSOptionalOwner)?.isOptional ?: false
}