/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.lookupcall

import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.s.lookupcall.LookupCallNewOperation
import org.eclipse.scout.sdk.core.s.util.ITier
import org.eclipse.scout.sdk.core.s.util.ScoutTier
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.element.CreateElementAction
import org.eclipse.scout.sdk.s2i.findTypeByName
import org.eclipse.scout.sdk.s2i.toIdea

class CreateLookupCallAction : CreateElementAction<LookupCallNewOperation>(EclipseScoutBundle.message("create.lookupcall"), EclipseScoutBundle.message("create.lookupcall.desc")) {

    override fun startTiers(): Collection<ITier<*>> = listOf(ScoutTier.Shared, ScoutTier.Server)

    override fun operationClass(): Class<LookupCallNewOperation> = LookupCallNewOperation::class.java

    override fun psiClassToOpen(op: LookupCallNewOperation): () -> PsiClass? {
        val sharedModule = op.sharedSourceFolder?.javaEnvironment()?.toIdea()?.module
        return { sharedModule?.findTypeByName(op.createdLookupCallFqn) }
    }
}
