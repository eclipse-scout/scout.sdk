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

import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.util.EventListenerList
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.s2i.environment.TransactionManager.Companion.callInExistingTransaction
import org.eclipse.scout.sdk.s2i.environment.TransactionManager.Companion.callInNewTransaction
import org.eclipse.scout.sdk.s2i.toScoutProgress
import java.util.concurrent.TimeUnit

open class OperationTask(title: String, project: Project, private val transactionManager: TransactionManager? = null, private val task: (IdeaProgress) -> Unit)
    : Task.Backgroundable(project, title, true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {

    private val m_progress = FinalValue<ProgressIndicator>()
    private val m_listeners = EventListenerList()

    override fun run(indicator: ProgressIndicator) {
        m_progress.setIfAbsent(indicator)
        indicator.checkCanceled()
        val scoutProgress = indicator.toScoutProgress()
        val workForCommit = 10
        val workForTask = 1000
        scoutProgress.init(workForTask + workForCommit, title)
        try {
            if (transactionManager == null) {
                // new independent top level transaction
                callInNewTransaction(project, title, { scoutProgress.newChild(workForCommit) }) {
                    task.invoke(scoutProgress.newChild(workForTask))
                }
            } else {
                // new asynchronous task running in existing parent transaction
                callInExistingTransaction(transactionManager) {
                    task.invoke(scoutProgress.newChild(workForTask))
                }
            }
        } catch (e: RuntimeException) {
            SdkLog.error("Error in background job.", e)
        }
    }

    fun cancel(): Boolean = m_progress.opt()
            .map { it.cancel(); true; }
            .orElse(false)

    fun <T> schedule(resultSupplier: (() -> T)? = null, delay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS, hidden: Boolean = false): IFuture<T> {
        val result = TaskFuture(this, resultSupplier)
        if (hidden) {
            scheduleHidden(delay, unit)
        } else {
            scheduleWithUi(delay, unit)
        }
        return result
    }

    protected fun scheduleHidden(delay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        val runnable = Runnable {
            try {
                val progress = EmptyProgressIndicator()
                run(progress)
                if (progress.isCanceled) {
                    onCancel()
                } else {
                    onFinished()
                }
            } catch (e: Throwable) {
                onThrowable(e)
            }
        }
        AppExecutorUtil.getAppScheduledExecutorService().schedule(runnable, delay, unit)
    }

    protected fun scheduleWithUi(delay: Long = 0, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        if (delay == 0L) {
            queue()
        } else {
            AppExecutorUtil.getAppScheduledExecutorService().schedule({ queue() }, delay, unit)
        }
    }

    fun addListener(listener: OperationTaskListener) = m_listeners.add(listener)

    fun removeListener(listener: OperationTaskListener): Boolean = m_listeners.remove(listener)


    override fun onFinished() {
        super.onFinished()
        m_listeners.get(OperationTaskListener::class.java).forEach {
            try {
                it.onFinished(this)
            } catch (e: RuntimeException) {
                SdkLog.error("Error in {} '{}' when finishing task {}.", OperationTaskListener::class.java.simpleName, it, title, e)
            }
        }
    }

    override fun onThrowable(error: Throwable) {
        // no super call here
        m_listeners.get(OperationTaskListener::class.java).forEach {
            try {
                it.onThrowable(this, error)
            } catch (e: RuntimeException) {
                SdkLog.error("Unable to complete task. Original error:", error)
                SdkLog.error("Error in {} '{}' when finishing task {} exceptionally. See previous log for details about the original error of the task.", OperationTaskListener::class.java.simpleName, it, title, e)
            }
        }
    }

    override fun onCancel() {
        super.onCancel()
        m_listeners.get(OperationTaskListener::class.java).forEach {
            try {
                it.onCancel(this)
            } catch (e: RuntimeException) {
                SdkLog.error("Error in {} '{}' when cancelling task {}.", OperationTaskListener::class.java.simpleName, it, title, e)
            }
        }
    }
}
