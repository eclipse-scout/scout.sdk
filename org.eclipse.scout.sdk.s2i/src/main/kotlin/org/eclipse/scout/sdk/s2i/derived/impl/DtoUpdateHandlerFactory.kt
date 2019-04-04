package org.eclipse.scout.sdk.s2i.derived.impl

import com.intellij.lang.jvm.JvmClassKind
import com.intellij.lang.jvm.JvmModifier
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.s.derived.DtoUpdateHandler
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.s2i.derived.AbstractDerivedResourceHandlerFactory
import org.eclipse.scout.sdk.s2i.findAllTypesAnnotatedWith
import java.util.*
import java.util.Collections.singleton
import java.util.function.BiFunction


open class DtoUpdateHandlerFactory : AbstractDerivedResourceHandlerFactory() {
    override fun createHandlers(scope: SearchScope, project: Project): List<BiFunction<IEnvironment, IProgress, Collection<IFuture<*>>>> {
        val elements = HashSet<PsiClass>()
        elements.addAll(project.findAllTypesAnnotatedWith(IScoutRuntimeTypes.Data, scope))
        elements.addAll(project.findAllTypesAnnotatedWith(IScoutRuntimeTypes.FormData, scope))
        elements.addAll(project.findAllTypesAnnotatedWith(IScoutRuntimeTypes.PageData, scope))

        return elements
                .filter(this::acceptClass)
                .flatMap(this::typeToHandlers)
    }

    fun typeToHandlers(type: PsiClass): Iterable<BiFunction<IEnvironment, IProgress, Collection<IFuture<*>>>> =
            singleton(DtoUpdateHandler(DerivedResourceInputWithIdea(type)))

    protected fun acceptClass(type: PsiClass): Boolean = type.isValid
            && type.isPhysical
            && type.isWritable
            && type.classKind == JvmClassKind.CLASS
            && type.hasModifier(JvmModifier.PUBLIC)
            && type.sourceElement != null
            && type.scope is PsiJavaFile
}