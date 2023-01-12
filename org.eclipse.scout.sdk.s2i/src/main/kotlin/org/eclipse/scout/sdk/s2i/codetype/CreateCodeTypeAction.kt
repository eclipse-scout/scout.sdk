/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.codetype

import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.s.codetype.CodeTypeNewOperation
import org.eclipse.scout.sdk.core.s.util.ITier
import org.eclipse.scout.sdk.core.s.util.ScoutTier
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.element.CreateElementAction
import org.eclipse.scout.sdk.s2i.findTypeByName
import org.eclipse.scout.sdk.s2i.toIdea

class CreateCodeTypeAction : CreateElementAction<CodeTypeNewOperation>(EclipseScoutBundle.message("create.codetype"), EclipseScoutBundle.message("create.codetype.desc")) {

    override fun startTiers(): Collection<ITier<*>> = listOf(ScoutTier.Shared)

    override fun operationClass(): Class<CodeTypeNewOperation> = CodeTypeNewOperation::class.java

    override fun psiClassToOpen(op: CodeTypeNewOperation): () -> PsiClass? {
        val sharedModule = op.sharedSourceFolder?.javaEnvironment()?.toIdea()?.module
        return { sharedModule?.findTypeByName(op.createdCodeTypeFqn) }
    }
}
