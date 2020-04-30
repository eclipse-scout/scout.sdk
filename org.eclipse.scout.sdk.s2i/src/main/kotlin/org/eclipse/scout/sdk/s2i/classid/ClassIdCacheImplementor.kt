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

import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.concurrency.AppExecutorUtil
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.s.dto.AbstractDtoGenerator
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.computeInReadAction
import org.eclipse.scout.sdk.s2i.environment.TransactionManager
import org.eclipse.scout.sdk.s2i.findAllTypesAnnotatedWith
import java.util.Collections.newSetFromMap
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class ClassIdCacheImplementor(val project: Project) : PsiTreeChangeAdapter(), ClassIdCache {

    private val m_classIdCache = ConcurrentHashMap<String /* class id */, MutableSet<String /* fqn */>>()
    private val m_fileCache = ConcurrentHashMap<String /* file path */, MutableMap<String /* fqn */, String /* classid */>>()
    private val m_stopTypes: Array<Class<out PsiElement>> = arrayOf(PsiClass::class.java, PsiModifierList::class.java, PsiTypeElement::class.java, PsiTypeParameter::class.java)
    @Volatile
    private var m_cacheReady = false

    init {
        Disposer.register(project, this) // ensure it is disposed when the project closes
    }

    fun isCacheRequired(): Boolean = !project.isDisposed && project.isOpen
            && InspectionProjectProfileManager.getInstance(project).currentProfile.isToolEnabled(HighlightDisplayKey.find(DuplicateClassIdInspection.SHORT_NAME))

    override fun findAllClassIds(scope: SearchScope, indicator: ProgressIndicator?) =
            project.findAllTypesAnnotatedWith(IScoutRuntimeTypes.ClassId, scope, indicator)
                    .filter { it.isValid }
                    .mapNotNull { ClassIdAnnotation.of(it) }
                    .filter { it.hasValue() }

    override fun typesWithClassId(classId: String): Set<String> = synchronized(m_classIdCache) {
        // synchronized so that asking for the cache waits until it is initially built
        return classIdCache()[classId] ?: emptySet()
    }

    override fun dispose() {
        PsiManager.getInstance(project).removePsiTreeChangeListener(this)
        fileCache().clear()
        classIdCache().clear()
    }

    override fun isCacheReady() = m_cacheReady

    override fun scheduleCacheSetupIfEnabled(): Future<*> {
        if (!isCacheRequired()) {
            return completedFuture(null)
        }
        return AppExecutorUtil.getAppScheduledExecutorService().schedule(this::setup, 5, TimeUnit.SECONDS)
    }

    override fun setup() = synchronized(m_classIdCache) {
        if (isCacheReady()) {
            return
        }

        try {
            TransactionManager.repeatUntilPassesWithIndex(project) {
                DumbService.getInstance(project).waitForSmartMode()
                trySetupCache()
                PsiManager.getInstance(project).addPsiTreeChangeListener(this) // initial cache is ready, register listener to keep it up to date
                m_cacheReady = true
            }
            duplicates().forEach { SdkLog.debug("Duplicate @ClassId value '{}' found for types {}.", it.key, it.value) }
        } catch (t: Exception) {
            SdkLog.warning("Error building @ClassId value cache.", t)
        }
    }

    override fun duplicates(): Map<String, Set<String>> = classIdCache()
            .filter { it.value.size > 1 }

    override fun duplicates(absoluteFilePath: String): Map<String, Set<String>> {
        val classIdCache = classIdCache()
        val classIdsInFile = fileCache()[absoluteFilePath]
                ?.map { it.value }
                ?.toSet() ?: return emptyMap()
        return classIdCache
                .filter { classIdsInFile.contains(it.key) }
                .filter { it.value.size > 1 }
    }

    internal fun trySetupCache() {
        SdkLog.debug("Starting to build @ClassId value cache.")
        val start = System.currentTimeMillis()
        val fileCache = fileCache()
        val allClassIds = computeInReadAction(project) {
            findAllClassIds(GlobalSearchScope.projectScope(project))
                    .filter { !ignoreClassId(it) }
                    .toList() // collect to list so that it is executed in the read action (terminal operation)
        }

        allClassIds.forEach {
            val fqn = it.ownerFqn() ?: return@forEach
            val classIdValue = it.value() ?: return@forEach
            val filePath = it.psiClass.containingFile.virtualFile.path
            updateOrAddType(fqn, classIdValue, null)
            fileCache.computeIfAbsent(filePath) { ConcurrentHashMap() }[fqn] = classIdValue
        }
        SdkLog.debug("Finished building initial @ClassId value cache in {}ms. {} distinct @ClassId values found in {} files.", System.currentTimeMillis() - start, classIdCache().size, fileCache.size)
    }

    internal fun ignoreClassId(classId: ClassIdAnnotation) =
            classId.value()?.endsWith(AbstractDtoGenerator.FORMDATA_CLASSID_SUFFIX) ?: true

    override fun childrenChanged(event: PsiTreeChangeEvent) {
        val file = event.file ?: return
        val path = file.virtualFile.path
        val mappingsInFile = ConcurrentHashMap<String /* fqn */, String /* classid value */>()

        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitLiteralExpression(expression: PsiLiteralExpression) {
                if (expression.parent !is PsiNameValuePair) {
                    return
                }
                val declaringAnnotation = PsiTreeUtil.getParentOfType(expression, PsiAnnotation::class.java, false, *m_stopTypes) ?: return
                val classId = ClassIdAnnotation.of(declaringAnnotation) ?: return
                if (ignoreClassId(classId)) {
                    return
                }
                val value = classId.value() ?: return
                val qualifiedName = classId.ownerFqn() ?: return
                mappingsInFile[qualifiedName] = value
            }
        })

        updateCache(path, mappingsInFile)
    }

    internal fun updateCache(file: String, mappingsInFile: MutableMap<String /* fqn */, String /* classid value */>) {
        val fileCache = fileCache()
        try {
            val oldMappings = fileCache[file]
            if (oldMappings == null) {
                // new file
                mappingsInFile.forEach { updateOrAddType(it.key, it.value, null) }
                return
            }

            val newOrChangedMappings = HashMap(mappingsInFile)
            newOrChangedMappings.entries.removeAll(oldMappings.entries) // remove all entries that did not change (the same for key and value)
            newOrChangedMappings.forEach { updateOrAddType(it.key, it.value, oldMappings[it.key]) }

            val removedMappings = HashMap(oldMappings)
            mappingsInFile.keys.forEach { removedMappings.remove(it) }
            removedMappings.forEach { removeType(it.key, it.value) }
        } finally {
            // ensure file cache is update to date
            if (mappingsInFile.isEmpty()) {
                fileCache.remove(file)
            } else {
                fileCache[file] = mappingsInFile
            }
        }
    }

    internal fun fileCache() = m_fileCache

    internal fun classIdCache() = m_classIdCache

    internal fun updateOrAddType(fqn: String, newClassId: String, oldClassId: String?) {
        classIdCache().computeIfAbsent(newClassId) { newSetFromMap(ConcurrentHashMap(1)) }.add(fqn)
        if (oldClassId != null) {
            removeType(fqn, oldClassId)
        }
    }

    internal fun removeType(fqn: String, classId: String) {
        val cache = classIdCache()
        val usedClassIds = cache[classId] ?: return
        usedClassIds.remove(fqn)
        if (usedClassIds.isEmpty()) {
            cache.remove(classId)
        }
    }
}

