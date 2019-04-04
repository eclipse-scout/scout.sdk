package org.eclipse.scout.sdk.s2i.environment

import org.eclipse.scout.sdk.core.s.environment.Future

class TaskFuture<V>(val task: OperationTask, resultSupplier: (() -> V)?) : Future<V>() {

    init {
        val listener = object : OperationTaskListener {
            override fun onThrowable(task: OperationTask, throwable: Throwable) {
                always()
                doCompletion(false, throwable, resultSupplier)
            }

            override fun onCancel(task: OperationTask) {
                always()
                doCompletion(true, null, resultSupplier)
            }

            override fun onFinished(task: OperationTask) {
                always()
                doCompletion(false, null, resultSupplier)
            }

            private fun always() {
                task.removeListener(this)
            }
        }
        task.addListener(listener)
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        task.cancel()
        return super.cancel(mayInterruptIfRunning)
    }
}