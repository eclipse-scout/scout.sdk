/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.derived.impl

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.model.api.MissingTypeException
import org.eclipse.scout.sdk.core.s.derived.IDerivedResourceInput
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.resolvePsi
import org.eclipse.scout.sdk.s2i.resolveSourceRoot
import org.eclipse.scout.sdk.s2i.toIdea
import org.eclipse.scout.sdk.s2i.toScoutType
import java.util.*


open class DerivedResourceInputWithIdea(val type: PsiClass) : IDerivedResourceInput {

    override fun getSourceType(env: IEnvironment): Optional<IType> =
            try {
                Optional.ofNullable(type.toScoutType(env.toIdea(), false))
            } catch (e: MissingTypeException) {
                SdkLog.info("Unable to update DTO for '{}' because there are compile errors in the compilation unit.", toString(), e)
                Optional.empty()
            }

    override fun getSourceFolderOf(t: IType, env: IEnvironment): Optional<IClasspathEntry> =
            Optional.ofNullable(t.resolvePsi()
                    ?.resolveSourceRoot()
                    ?.let { env.toIdea().findClasspathEntry(it) })

    protected fun project(): Project = type.project

    override fun toString() = IdeaEnvironment.computeInReadAction(project()) {
        // read action required! as the nameIdentifier may be cached, this is not always obvious
        type.nameIdentifier?.text ?: type.toString()
    }
}
