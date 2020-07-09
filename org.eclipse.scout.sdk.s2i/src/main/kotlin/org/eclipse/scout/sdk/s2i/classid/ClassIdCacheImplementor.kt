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

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.FileContentUtilCore
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.log.SdkLog.onTrace
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.s.dto.AbstractDtoGenerator
import org.eclipse.scout.sdk.s2i.environment.TransactionManager
import org.eclipse.scout.sdk.s2i.findAllTypesAnnotatedWith
import java.util.concurrent.ConcurrentHashMap

class ClassIdCacheImplementor(val project: Project) : ClassIdCache {

    private val m_fileCache = ConcurrentHashMap<String /* file path */, MutableMap<String /* fqn */, String /* classid */>>()
    private val m_stopTypes: Array<Class<out PsiElement>> = arrayOf(PsiClass::class.java, PsiModifierList::class.java, PsiTypeElement::class.java, PsiTypeParameter::class.java, PsiJavaFile::class.java)

    @Volatile
    private var m_cacheReady = false

    init {
        Disposer.register(project, this) // ensure it is disposed when the project closes
    }

    override fun isCacheReady() = m_cacheReady

    override fun setup() = synchronized(m_fileCache) {
        // synchronized so that only one is creating the cache at a time
        if (isCacheReady()) {
            return
        }

        try {
            TransactionManager.repeatUntilPassesWithIndex(project, false) {
                trySetupCache()
            }

            // initial cache is ready, register listeners to keep it up to date
            PsiManager.getInstance(project).addPsiTreeChangeListener(PsiListener(), this)
            VirtualFileManager.getInstance().addAsyncFileListener(VfsListener(), this)

            m_cacheReady = true

            duplicates().forEach { SdkLog.debug("Duplicate @ClassId value '{}' found for types {}.", it.key, it.value) }
        } catch (e: ProcessCanceledException) {
            SdkLog.debug("@ClassId value cache creation canceled. Retry on next use.", onTrace(e))
        } catch (t: Exception) {
            SdkLog.warning("Error building @ClassId value cache.", t)
        }
    }

    override fun dispose() {
        m_fileCache.clear()
    }

    override fun findAllClassIds(scope: SearchScope, indicator: ProgressIndicator?) =
            project.findAllTypesAnnotatedWith(IScoutRuntimeTypes.ClassId, scope, indicator)
                    .filter { it.isValid }
                    .mapNotNull { ClassIdAnnotation.of(it) }
                    .filter { it.hasValue() }

    override fun typesWithClassId(classId: String): List<String> = usageByClassId()[classId] ?: emptyList()

    override fun duplicates(): Map<String, List<String>> = duplicates(null)

    override fun duplicates(absoluteFilePath: String): Map<String, List<String>> {
        val classIdsInFile = m_fileCache[absoluteFilePath]?.map { it.value }?.toSet() ?: return emptyMap()
        return duplicates { classIdsInFile.contains(it.value) } // only duplicates of the current file
    }

    internal fun duplicates(filter: ((Map.Entry<String, String>) -> Boolean)?) = usageByClassId(filter).filter { it.value.size > 1 }

    internal fun usageByClassId(filter: ((Map.Entry<String, String>) -> Boolean)? = null): Map<String /* classId */, List<String /* fqn */>> {
        val nullSafeFilter = filter ?: { true }
        return m_fileCache.values
                .asSequence()
                .map { it.entries }
                .flatten()
                .filter { nullSafeFilter.invoke(it) }
                .groupBy({ it.value }, { it.key })
    }

    internal fun trySetupCache() {
        SdkLog.debug("Start building @ClassId value cache.")
        val start = System.currentTimeMillis()
        findAllClassIds(GlobalSearchScope.projectScope(project))
                .filter { !ignoreClassId(it) }
                .forEach {
                    val fqn = it.ownerFqn() ?: return@forEach
                    val classIdValue = it.value() ?: return@forEach
                    val filePath = it.psiClass.containingFile.virtualFile.path
                    m_fileCache.computeIfAbsent(filePath) { ConcurrentHashMap() }[fqn] = classIdValue
                }
        SdkLog.debug("Finished building initial @ClassId value cache in {}ms. @ClassId values found in {} files.", System.currentTimeMillis() - start, m_fileCache.size)
    }

    internal fun ignoreClassId(classId: ClassIdAnnotation) = classId.value()?.endsWith(AbstractDtoGenerator.FORMDATA_CLASSID_SUFFIX) ?: true

    internal fun fileCache() = m_fileCache // for testing

    private inner class PsiListener : PsiTreeChangeAdapter() {
        override fun childrenChanged(event: PsiTreeChangeEvent) {
            val file = event.file ?: return
            if (file.language != JavaLanguage.INSTANCE || !file.isPhysical) {
                return
            }
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

            if (mappingsInFile.isEmpty()) {
                m_fileCache.remove(path)
            } else {
                m_fileCache[path] = mappingsInFile
            }
        }
    }

    private inner class VfsListener : AsyncFileListener {
        override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
            val removedFiles = events.mapNotNull { toFileIfRemoved(it) }
            if (removedFiles.isEmpty()) {
                return null
            }
            return object : AsyncFileListener.ChangeApplier {
                override fun afterVfsChange() {
                    removedFiles.forEach { m_fileCache.remove(it) }
                }
            }
        }

        private fun toFileIfRemoved(event: VFileEvent): String? {
            if (event is VFileDeleteEvent) {
                return event.file.path // deleted
            }
            if (event is VFilePropertyChangeEvent && event.propertyName == VirtualFile.PROP_NAME && event.requestor != FileContentUtilCore.FORCE_RELOAD_REQUESTOR) {
                return event.oldPath // renamed
            }
            return null
        }
    }
}

