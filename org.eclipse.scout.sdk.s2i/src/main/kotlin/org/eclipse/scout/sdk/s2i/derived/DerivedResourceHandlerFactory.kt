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
package org.eclipse.scout.sdk.s2i.derived

import com.intellij.openapi.project.Project
import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import java.util.function.BiFunction

interface DerivedResourceHandlerFactory {
    fun createHandlersFor(scope: SearchScope, project: Project): Sequence<BiFunction<IEnvironment, IProgress, Collection<IFuture<*>>>>
}