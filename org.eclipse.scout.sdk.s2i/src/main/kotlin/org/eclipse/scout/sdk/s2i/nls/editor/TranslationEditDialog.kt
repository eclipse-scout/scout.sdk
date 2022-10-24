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
package org.eclipse.scout.sdk.s2i.nls.editor

import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.s.nls.Translation
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle

class TranslationEditDialog(project: Project, val translation: IStackedTranslation, manager: TranslationManager, initialLanguageShown: Language? = null) :
    AbstractTranslationDialog(project, translation.languagesOfAllStores().toList(), manager, translation.key(), initialLanguageShown) {

    init {
        title = EclipseScoutBundle.message("edit.translation.x", translation.key())
        translation.texts().forEach { languageTextField(it.key)?.text = it.value }
        keyTextField().isEnabled = false
    }

    override fun doSave(result: Translation) {
        translationManager.setTranslation(result)
    }
}