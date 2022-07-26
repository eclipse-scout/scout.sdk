/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.js.element

import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.s.environment.IFuture

/**
 * Service to deal with JS element related tasks
 */
interface JsElementsManager {
    /**
     * Schedules a creation of the JS elements for the given scope.
     * @param scope The [SearchScope] in which the JS elements should be created.
     * @return A future representing the asynchronous create operation.
     */
    fun scheduleJsElementsCreation(scope: SearchScope): IFuture<Unit>
}