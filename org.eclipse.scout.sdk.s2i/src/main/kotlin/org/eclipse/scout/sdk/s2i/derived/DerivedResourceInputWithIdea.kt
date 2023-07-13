/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.derived

import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.java.model.api.IClasspathEntry
import org.eclipse.scout.sdk.core.java.model.api.IType
import org.eclipse.scout.sdk.core.java.model.api.MissingTypeException
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.derived.IDerivedResourceInput
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.resolvePsi
import org.eclipse.scout.sdk.s2i.resolveSourceRoot
import org.eclipse.scout.sdk.s2i.toIdea
import org.eclipse.scout.sdk.s2i.toScoutType
import java.util.*

open class DerivedResourceInputWithIdea(val type: PsiClass) : IDerivedResourceInput {

    override fun getSourceType(env: IEnvironment): Optional<IType> = computeInReadAction(env.toIdea().project, true) {
        try {
            Optional.ofNullable(type.toScoutType(env.toIdea(), false))
        } catch (e: MissingTypeException) {
            SdkLog.info("Unable to update DTO for '{}' because there are compile errors in the compilation unit.", toString(), e)
            Optional.empty()
        }
    }

    override fun getSourceFolderOf(t: IType, env: IEnvironment): Optional<IClasspathEntry> = computeInReadAction(env.toIdea().project) {
        Optional.ofNullable(t.resolvePsi()
            ?.resolveSourceRoot()
            ?.let { env.toIdea().findClasspathEntry(it) })
    }

    override fun toString() = type.name ?: "Unknown"
}
