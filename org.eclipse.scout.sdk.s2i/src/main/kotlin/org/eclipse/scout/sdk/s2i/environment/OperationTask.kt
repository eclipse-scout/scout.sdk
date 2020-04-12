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
package org.eclipse.scout.sdk.s2i.environment

import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.util.CoreUtils
import org.eclipse.scout.sdk.core.util.EventListenerList
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.s2i.environment.TransactionManager.Companion.runInTransaction
import org.eclipse.scout.sdk.s2i.toScoutProgress
import java.util.concurrent.TimeUnit

open class OperationTask(title: String, project: Project, private val task: (IdeaProgress) -> Unit) : Task.Backgroundable(project, title, true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {

    private val m_progress = FinalValue<ProgressIndicator>()
    private val m_listeners = EventListenerList()
    private val m_name = CoreUtils.toStringIfOverwritten(task).orElse(null)

    override fun run(indicator: ProgressIndicator) {
        m_progress.setIfAbsent(indicator)
        indicator.checkCanceled()
        val scoutProgress = indicator.toScoutProgress()
        val workForCommit = 10
        val workForTask = 1000
        scoutProgress.init(m_name, workForTask + workForCommit)
        runInTransaction(project, { scoutProgress.newChild(workForCommit) }) {
            task.invoke(scoutProgress.newChild(workForTask))
        }
    }

    fun cancel(): Boolean = m_progress.opt()
            .map { it.cancel(); true; }
            .orElse(false)

    fun <T> schedule(resultSupplier: (() -> T)? = null, delay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS): IFuture<T> {
        val result = TaskFuture(this, resultSupplier)
        if (delay == 0L) {
            queue()
        } else {
            AppExecutorUtil.getAppScheduledExecutorService().schedule({
                queue()
            }, delay, unit)
        }
        return result
    }

    fun addListener(listener: OperationTaskListener) = m_listeners.add(listener)

    fun removeListener(listener: OperationTaskListener): Boolean = m_listeners.remove(listener)


    override fun onFinished() {
        super.onFinished()
        try {
            m_listeners.get(OperationTaskListener::class.java)
                    .forEach { it.onFinished(this) }
        } catch (e: RuntimeException) {
            SdkLog.error("Error in {} when finishing task {}.", OperationTaskListener::class.java.simpleName, m_name, e)
        }
    }

    override fun onThrowable(error: Throwable) {
        // no super call here
        try {
            m_listeners.get(OperationTaskListener::class.java)
                    .forEach { it.onThrowable(this, error) }
        } catch (e: RuntimeException) {
            SdkLog.error("Unable to complete task. Original error:", error)
            SdkLog.error("Error in {} when finishing task {} exceptionally. See previous log for details about the original error of the task.", OperationTaskListener::class.java.simpleName, m_name, e)
        }
    }

    override fun onCancel() {
        super.onCancel()
        try {
            m_listeners.get(OperationTaskListener::class.java)
                    .forEach { it.onCancel(this) }
        } catch (e: RuntimeException) {
            SdkLog.error("Error in {} when cancelling task {}.", OperationTaskListener::class.java.simpleName, m_name, e)
        }
    }
}
