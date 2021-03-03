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
package org.eclipse.scout.sdk.s2i.page

import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.s.page.PageNewOperation
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.element.CreateElementAction
import org.eclipse.scout.sdk.s2i.findTypeByName
import org.eclipse.scout.sdk.s2i.toIdea

class CreatePageAction : CreateElementAction<PageNewOperation>(EclipseScoutBundle.message("create.page"), EclipseScoutBundle.message("create.page.desc")) {

    override fun operationClass(): Class<PageNewOperation> = PageNewOperation::class.java

    override fun psiClassToOpen(op: PageNewOperation): () -> PsiClass? {
        val clientModule = op.clientSourceFolder?.javaEnvironment()?.toIdea()?.module
        return { clientModule?.findTypeByName(op.createdPageFqn) }
    }
}
