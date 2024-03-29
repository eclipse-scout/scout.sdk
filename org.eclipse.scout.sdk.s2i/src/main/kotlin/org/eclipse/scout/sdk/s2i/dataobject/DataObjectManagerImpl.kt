/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.dataobject

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.SearchScope
import org.eclipse.scout.sdk.core.java.model.api.IType
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.dataobject.DataObjectModel
import org.eclipse.scout.sdk.core.s.dataobject.DoConvenienceMethodsUpdateOperation
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.environment.TransactionManager
import org.eclipse.scout.sdk.s2i.findTypesByName
import org.eclipse.scout.sdk.s2i.newSubTypeHierarchy
import org.eclipse.scout.sdk.s2i.toIdea
import org.eclipse.scout.sdk.s2i.toScoutType
import kotlin.streams.asSequence

class DataObjectManagerImpl(val project: Project) : DataObjectManager {

    override fun createDataObjectModel(type: IType): DataObjectModel? = DataObjectModel.wrap(type).orElse(null)

    override fun scheduleConvenienceMethodsUpdate(scope: SearchScope) = callInIdeaEnvironment(project, message("update.dataobject.in.scope")) { e, p ->
        try {
            updateConvenienceMethods(scope, e, p)
        } catch (t: Throwable) {
            SdkLog.warning("DataObject convenience methods update failed.", t)
        }
    }

    private fun updateConvenienceMethods(scope: SearchScope, env: IdeaEnvironment, progress: IdeaProgress) {
        val totalWork = 200
        val workForDiscovery = 10
        progress.init(totalWork, message("update.dataobject.in.scope"))
        val doEntities = computeInReadAction(project, true, progress.newChild(workForDiscovery).indicator) {
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
        val operation = object : DoConvenienceMethodsUpdateOperation() {
            private var m_counter = 0

            override fun write(newSource: CharSequence, dataObjectType: IType, environment: IEnvironment, progress: IProgress): IFuture<IType> {
                val indicator = progress.toIdea().indicator
                indicator.text2 = dataObjectType.name()
                indicator.checkCanceled()

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
        operation
                .withDataObjects(doEntities)
                .withLineSeparator("\n") // always use \n for IntelliJ. Line separators are cleaned on file write anyway.
                .accept(env, progress.newChild(totalWork - workForDiscovery))
    }
}