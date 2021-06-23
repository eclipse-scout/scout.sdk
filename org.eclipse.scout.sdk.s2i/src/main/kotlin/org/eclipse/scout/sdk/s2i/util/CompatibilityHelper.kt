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
package org.eclipse.scout.sdk.s2i.util

import com.intellij.openapi.observable.properties.PropertyGraph
import org.eclipse.scout.sdk.s2i.util.compat.CompatibilityMethodCaller

object CompatibilityHelper {
    /**
     * Constructor changed with IJ 2021.2
     * Can be removed if the minimal supported IJ version is >= 2021.2
     */
    private val NEW_PROPERTY_GRAPH = CompatibilityMethodCaller<PropertyGraph>()
        .withCandidate(PropertyGraph::class.java, CompatibilityMethodCaller.CONSTRUCTOR_NAME, String::class.java, Boolean::class.java) {
            it.invokeStatic(null, true) // >= IJ 2021.2
        }
        .withCandidate(PropertyGraph::class.java, CompatibilityMethodCaller.CONSTRUCTOR_NAME, String::class.java) {
            it.invokeStatic(null) // < IJ 2021.2
        }

    fun newPropertyGraph() = NEW_PROPERTY_GRAPH.invoke()
}