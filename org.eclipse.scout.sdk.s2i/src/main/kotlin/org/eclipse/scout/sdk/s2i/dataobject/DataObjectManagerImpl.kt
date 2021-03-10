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
package org.eclipse.scout.sdk.s2i.dataobject

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi
import org.eclipse.scout.sdk.core.s.dataobject.DataObjectModel
import org.eclipse.scout.sdk.core.s.dataobject.DoConvenienceMethodsUpdateOperation
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.TransactionManager
import org.eclipse.scout.sdk.s2i.findTypesByName
import org.eclipse.scout.sdk.s2i.newSubTypeHierarchy
import org.eclipse.scout.sdk.s2i.toScoutType
import kotlin.streams.asSequence

class DataObjectManagerImpl(val project: Project) : DataObjectManager {

    override fun createDataObjectModel(type: IType): DataObjectModel? = DataObjectModel.wrap(type).orElse(null)

    override fun scheduleConvenienceMethodsUpdate(scope: SearchScope) = callInIdeaEnvironment(project, message("update.dataobject.in.scope")) { e, p ->
        updateConvenienceMethods(scope, e, p)
    }

    private fun updateConvenienceMethods(scope: SearchScope, env: IdeaEnvironment, progress: IdeaProgress) {
        val totalWork = 200
        val workForDiscovery = 10
        progress.init(totalWork, message("update.dataobject.in.scope"))
        val doEntities = computeInReadAction(project, true, progress.indicator) {
            ScoutApi.allKnown().asSequence()
                    .map { it.IDoEntity().fqn() }
                    .distinct()
                    .flatMap { project.findTypesByName(it) }
                    .flatMap { it.newSubTypeHierarchy(scope, checkDeep = true, includeAnonymous = false, includeRoot = false).asSequence() }
                    .filter { it.isPhysical }
                    .filter { it.isWritable }
                    .filter { !it.isInterface }
                    .filter { it.hasModifierProperty(PsiModifier.PUBLIC) }
                    .filter { it.isValid }
                    .filter { !it.isEnum }
                    .mapNotNull { it.toScoutType(env) }
                    .toList()

        }
        if (doEntities.isEmpty()) {
            return
        }
        progress.worked(workForDiscovery)
        val operation = object : DoConvenienceMethodsUpdateOperation() {
            private var m_counter = 0

            override fun write(newSource: CharSequence, dataObjectType: IType, environment: IEnvironment, progress: IProgress): IFuture<IType> {
                val future = super.write(newSource, dataObjectType, environment, progress)
                commitIfNecessary()
                return future
            }

            private fun commitIfNecessary() {
                m_counter++
                if (m_counter >= TransactionManager.BULK_UPDATE_LIMIT) {
                    TransactionManager.current().checkpoint(null)
                    m_counter = 0
                }
            }
        }
        operation.withDataObjects(doEntities).accept(env, progress.newChild(totalWork - workForDiscovery))
    }
}