package org.eclipse.scout.sdk.s2i.environment

import java.util.*

interface OperationTaskListener : EventListener {
    fun onFinished(task: OperationTask)

    fun onThrowable(task: OperationTask, throwable: Throwable)

    fun onCancel(task: OperationTask)
}