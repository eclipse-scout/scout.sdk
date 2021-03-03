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
package org.eclipse.scout.sdk.s2i.codetype

import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.s.codetype.CodeTypeNewOperation
import org.eclipse.scout.sdk.core.s.util.ScoutTier
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.element.CreateElementAction
import org.eclipse.scout.sdk.s2i.findTypeByName
import org.eclipse.scout.sdk.s2i.toIdea

class CreateCodeTypeAction : CreateElementAction<CodeTypeNewOperation>(EclipseScoutBundle.message("create.codetype"), EclipseScoutBundle.message("create.codetype.desc")) {

    override fun startScoutTiers(): Collection<ScoutTier> = listOf(ScoutTier.Shared)

    override fun operationClass(): Class<CodeTypeNewOperation> = CodeTypeNewOperation::class.java

    override fun psiClassToOpen(op: CodeTypeNewOperation): () -> PsiClass? {
        val sharedModule = op.sharedSourceFolder?.javaEnvironment()?.toIdea()?.module
        return { sharedModule?.findTypeByName(op.createdCodeTypeFqn) }
    }
}
