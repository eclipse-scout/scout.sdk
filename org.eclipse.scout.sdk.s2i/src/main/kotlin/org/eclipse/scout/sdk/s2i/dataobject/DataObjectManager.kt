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
package org.eclipse.scout.sdk.s2i.dataobject

import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.dataobject.DataObjectModel
import org.eclipse.scout.sdk.core.s.environment.IFuture

/**
 * Service to deal with DataObject related tasks
 */
interface DataObjectManager {
    /**
     * @return The parsed [DataObjectModel] for the [IType] given or null if it is no valid data object.
     */
    fun createDataObjectModel(type: IType): DataObjectModel?

    /**
     * Schedules an update of the DataObject convenience methods for all dataobjects in the scope given.
     * @param scope The [SearchScope] in which the dataobjects should be updated.
     * @return A future representing the asynchronous update operation.
     */
    fun scheduleConvenienceMethodsUpdate(scope: SearchScope): IFuture<Unit>
}