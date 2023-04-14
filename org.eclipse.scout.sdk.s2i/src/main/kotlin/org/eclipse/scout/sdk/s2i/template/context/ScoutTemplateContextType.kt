/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template.context

import com.intellij.codeInsight.template.JavaCodeContextType
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.toScoutType
import org.eclipse.scout.sdk.s2i.util.ApiHelper

abstract class ScoutTemplateContextType(id: String, presentableName: String, baseContextType: Class<out TemplateContextType>?) : JavaCodeContextType(id, presentableName, baseContextType) {

    protected fun isInContext(element: PsiElement, instanceCheckTypeNameSupplier: (IScoutApi) -> ITypeNameSupplier): Boolean {
        val module = element.containingModule() ?: return false
        val clazz = PsiTreeUtil.getParentOfType(element, PsiClass::class.java) ?: return false
        return IdeaEnvironment.callInIdeaEnvironmentSync(module.project, IdeaProgress.empty()) { env, _ ->
            val type = clazz.toScoutType(env) ?: return@callInIdeaEnvironmentSync false
            ApiHelper.scoutApiFor(clazz, env)
                ?.let { instanceCheckTypeNameSupplier(it) }
                ?.let { type.isInstanceOf(it) } == true
        }
    }

    class DoEntity : ScoutTemplateContextType("DO_ENTITY", EclipseScoutBundle.message("live.template.context.doentity"), Declaration::class.java) {

        override fun isInContext(element: PsiElement) = isInContext(element) { it.DoEntity() }
    }
}
