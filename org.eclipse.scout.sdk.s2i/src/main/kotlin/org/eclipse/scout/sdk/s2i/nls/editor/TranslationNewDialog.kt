/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.nls.editor

import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.Translation
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle

class TranslationNewDialog(project: Project, store: ITranslationStore, stack: TranslationStoreStack, initialKey: String? = null) : AbstractTranslationDialog(project, store, stack, initialKey) {

    private var m_createdTranslation: ITranslationEntry? = null

    init {
        title = EclipseScoutBundle.message("create.new.translation.in.x", store.service().type().elementName())
    }

    override fun doSave(result: Translation) {
        m_createdTranslation = stack.addNewTranslation(result, store)
    }

    fun createdTranslation() = m_createdTranslation
}