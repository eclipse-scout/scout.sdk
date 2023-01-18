/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.dataobject

import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.java.model.api.IType
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