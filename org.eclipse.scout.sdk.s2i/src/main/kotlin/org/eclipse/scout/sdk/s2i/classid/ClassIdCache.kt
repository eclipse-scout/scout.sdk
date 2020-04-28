/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.classid

import com.intellij.openapi.Disposable
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.search.SearchScope

/**
 * Class to search for @ClassIds
 */
interface ClassIdCache : Disposable {

    /**
     * @param classId The @ClassId value to search
     * @return A [Set] holding all fully qualified class names in the project having the id specified.
     */
    fun typesWithClassId(classId: String): Set<String>

    /**
     * Finds all [ClassIdAnnotation]s in the [SearchScope] specified. This method does not use the cache but calculates the result anew.
     * @param scope The [SearchScope] in which should be searched.
     * @param indicator An optional [ProgressIndicator] to use.
     * @return A [Sequence] holding all findings
     */
    fun findAllClassIds(scope: SearchScope, indicator: ProgressIndicator? = null): Sequence<ClassIdAnnotation>

    /**
     * Builds the cache. If the cache is already ready, this method does nothing.
     */
    fun setup()

    /**
     * @return true if the @ClassId cache is ready and the [duplicates] methods may be used.
     */
    fun isCacheReady(): Boolean

    /**
     * @return A [Map] holding all @ClassId values that exist more than once in the cache.
     * The key of the map is the @ClassId value, the value of the Map is a [Set] of fully qualified class names that have the corresponding @ClassId value.
     *
     * This method only returns a result if the cache has been created (see [isCacheReady] and [setup])
     */
    fun duplicates(): Map<String, Set<String>>

    /**
     * @param absoluteFilePath A [String] holding an absolute path on the local file system pointing to the file for which the duplicates should be returned.
     * The cache only contains entries of the current project. This means the file must be within the project to return a result.
     * @return A [Map] holding all duplicate @ClassId values for the given file path.
     * The key of the map is the @ClassId value, the value of the Map is a [Set] of fully qualified class names that have the corresponding @ClassId value.
     * This means at least one class in the set of each entry must be within the file specified.
     *
     * This method only returns a result if the cache has been created (see [isCacheReady] and [setup])
     */
    fun duplicates(absoluteFilePath: String): Map<String, Set<String>>
}