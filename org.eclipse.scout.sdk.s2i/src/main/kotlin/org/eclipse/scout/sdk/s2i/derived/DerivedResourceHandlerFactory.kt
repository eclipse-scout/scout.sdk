package org.eclipse.scout.sdk.s2i.derived

import com.intellij.openapi.project.Project
import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import java.util.function.BiFunction

interface DerivedResourceHandlerFactory {
    fun createHandlersFor(scope: SearchScope, project: Project): List<BiFunction<IEnvironment, IProgress, Collection<IFuture<*>>>>
}