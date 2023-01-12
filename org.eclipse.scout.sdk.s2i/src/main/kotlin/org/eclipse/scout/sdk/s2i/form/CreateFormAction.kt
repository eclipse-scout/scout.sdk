/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.form

import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.s.form.FormNewOperation
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.element.CreateElementAction
import org.eclipse.scout.sdk.s2i.findTypeByName
import org.eclipse.scout.sdk.s2i.toIdea

class CreateFormAction : CreateElementAction<FormNewOperation>(EclipseScoutBundle.message("create.form"), EclipseScoutBundle.message("create.form.desc")) {

    override fun operationClass(): Class<FormNewOperation> = FormNewOperation::class.java

    override fun psiClassToOpen(op: FormNewOperation): () -> PsiClass? {
        val clientModule = op.clientSourceFolder?.javaEnvironment()?.toIdea()?.module
        return { clientModule?.findTypeByName(op.createdFormFqn) }
    }
}
