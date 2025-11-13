/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.classid

import com.intellij.openapi.Disposable
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.search.SearchScope
import java.util.concurrent.Future

/**
 * Class to search for @ClassIds
 */
interface ClassIdCache : Disposable {

    /**
     * Finds all [ClassIdAnnotation]s in the [SearchScope] specified. This method does not use the cache but calculates the result from scratch.
     * @param scope The [SearchScope] in which should be searched.
     * @param indicator An optional [ProgressIndicator] to use.
     * @return A [Sequence] holding all findings
     */
    fun findAllClassIds(scope: SearchScope, indicator: ProgressIndicator? = null): Sequence<ClassIdAnnotation>

    /**
     * Builds the cache and waits until the setup completed. If the cache is already ready, this method does nothing.
     */
    fun setup()

    /**
     * Schedules an asynchronous setup of the ClassId cache.
     * This method returns immediately after the setup has been scheduled.
     * Use [isCacheReady] to check if it has completed or use the resulting [Future] to wait for setup completion.
     *
     * If there is already a setup scheduled or the cache is already set up, this method does nothing.
     */
    fun scheduleSetup(): Future<*>

    /**
     * @return true if the @ClassId cache is ready and the [duplicates] methods may be used.
     */
    fun isCacheReady(): Boolean

    /**
     * @return A [Map] holding all @ClassId values that exist more than once in the cache.
     * The key of the map is the @ClassId value, the value of the Map are all [ClassIdOccurrence] having corresponding @ClassId value.
     *
     * This method only returns a result if the cache has been created (see [isCacheReady] and [setup])
     */
    fun duplicates(): Map<String, List<ClassIdOccurrence>>

    /**
     * @param absoluteFilePath A [String] holding an absolute path on the local file system pointing to the file for which the duplicates should be returned.
     * The cache only contains entries of the current project. This means the file must be within the project to return a result.
     * @return A [Map] holding all duplicate @ClassId values for the given file path.
     * The key of the map is the @ClassId value, the value of the Map are all [ClassIdOccurrence] that have the corresponding @ClassId value.
     * This means at least one class in the list of each entry will be within the file specified.
     *
     * This method only returns a result if the cache has been created (see [isCacheReady] and [setup])
     */
    fun duplicates(absoluteFilePath: String): Map<String, List<ClassIdOccurrence>>

    data class ClassIdOccurrence(val path: String, val fqn: String, val classId: String)
}