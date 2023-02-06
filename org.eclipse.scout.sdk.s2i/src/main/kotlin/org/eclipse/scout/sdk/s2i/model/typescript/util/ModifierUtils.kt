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

import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList.ModifierType
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier
import org.eclipse.scout.sdk.core.util.SdkException

val MODIFIER_MAPPING = Modifier.values()
    .associateWith { ModifierType.values().first { mt -> it.keyword == mt.keyword } }

fun Modifier.toModifierType(): ModifierType {
    return MODIFIER_MAPPING[this] ?: throw SdkException("No PSI mapping for Modifier '{}' (keyword={}).", name, keyword)
}