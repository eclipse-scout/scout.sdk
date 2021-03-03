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
import org.eclipse.scout.sdk.core.s.util.ScoutTier
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.findTypeByName

class S2iScoutTier {
    companion object {
        fun valueOf(module: Module, environment: IdeaEnvironment? = null): ScoutTier? = ScoutTier.valueOf({ module.findTypeByName(it) != null }, ApiHelper.scoutApiFor(module, environment)).orElse(null)
    }
}