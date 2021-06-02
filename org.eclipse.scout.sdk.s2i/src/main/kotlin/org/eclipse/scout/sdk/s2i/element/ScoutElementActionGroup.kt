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
package org.eclipse.scout.sdk.s2i.element

import com.intellij.ide.actions.NonTrivialActionGroup
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle

class ScoutElementActionGroup : NonTrivialActionGroup() {
    init {
        val presentation = templatePresentation
        presentation.text = EclipseScoutBundle.message("create.element")
        presentation.description = EclipseScoutBundle.message("create.element.desc")
        presentation.icon = EclipseScoutBundle.scoutIcon(15)
    }
}
