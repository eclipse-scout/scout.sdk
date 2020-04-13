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

import org.eclipse.scout.sdk.core.s.environment.SdkFuture

class TaskFuture<V>(val task: OperationTask, resultSupplier: (() -> V)?) : SdkFuture<V>() {

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