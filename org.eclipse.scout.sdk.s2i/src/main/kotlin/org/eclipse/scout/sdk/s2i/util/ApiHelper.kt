/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.util

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiElement
import org.eclipse.scout.sdk.core.apidef.IApiSpecification
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironmentSync
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress

object ApiHelper {

    fun requireScoutApiFor(context: PsiElement, environment: IdeaEnvironment? = null) = scoutApiFor(context, environment)
            ?: throw newFail("Cannot find a module for context element: {}", context)

    fun scoutApiFor(context: PsiElement, environment: IdeaEnvironment? = null) = context.containingModule()
            ?.let { scoutApiFor(it, environment) }

    fun scoutApiFor(module: Module, environment: IdeaEnvironment? = null): IScoutApi? {
        return apiFor(module, IScoutApi::class.java, environment)
    }

    fun <T : IApiSpecification> apiFor(module: Module, api: Class<T>, environment: IdeaEnvironment? = null): T? {
        if (environment == null) {
            return callInIdeaEnvironmentSync(module.project, IdeaProgress.empty()) { env, _ ->
                createApiFor(module, api, env)
            }
        }
        return createApiFor(module, api, environment)
    }

    private fun <T : IApiSpecification> createApiFor(module: Module, api: Class<T>, environment: IdeaEnvironment): T? {
        val env = environment.toScoutJavaEnvironment(module) ?: throw newFail("Module '{}' is no Java module.", module.name)
        return env.api(api).orElse(null)
    }
}