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
package org.eclipse.scout.sdk.s2i.entity

import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.s.entity.EntityNewOperation
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.element.CreateElementAction
import org.eclipse.scout.sdk.s2i.findTypeByName
import org.eclipse.scout.sdk.s2i.toIdea

class CreateEntityAction : CreateElementAction<EntityNewOperation>(EclipseScoutBundle.message("create.entity"), EclipseScoutBundle.message("create.entity.desc")) {

    override fun operationClass(): Class<EntityNewOperation> = EntityNewOperation::class.java

    override fun psiClassToOpen(op: EntityNewOperation): () -> PsiClass? {
        val clientModule = op.clientSourceFolder?.javaEnvironment()?.toIdea()?.module
        return { op.formNewOperation?.let { clientModule?.findTypeByName(it.createdFormFqn) } }
    }
}
