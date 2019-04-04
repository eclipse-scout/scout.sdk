package org.eclipse.scout.sdk.s2i.derived

import com.intellij.openapi.project.Project
import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import java.util.function.BiFunction

abstract class AbstractDerivedResourceHandlerFactory : DerivedResourceHandlerFactory {

    override fun createHandlersFor(scope: SearchScope, project: Project): List<BiFunction<IEnvironment, IProgress, Collection<IFuture<*>>>> =
            IdeaEnvironment.computeInReadAction(project) { createHandlers(scope, project) }

    protected abstract fun createHandlers(scope: SearchScope, project: Project): List<BiFunction<IEnvironment, IProgress, Collection<IFuture<*>>>>
}