/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.util

import com.intellij.openapi.module.Module
import org.eclipse.scout.sdk.core.apidef.IApiSpecification
import org.eclipse.scout.sdk.core.s.util.ITier
import org.eclipse.scout.sdk.core.apidef.OptApiFunction
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.findTypeByName
import java.util.*

class S2iTier {
    companion object {
        fun of(module: Module, environment: IdeaEnvironment? = null): ITier<*>? = ITier.of({ module.findTypeByName(it) != null },
            object : OptApiFunction {
                override fun <T : IApiSpecification> apply(c: Class<T>): Optional<T> {
                    return Optional.ofNullable(ApiHelper.apiFor(module, c, environment))
                }
            })
            .orElse(null)
    }
}