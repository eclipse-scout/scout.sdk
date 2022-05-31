/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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