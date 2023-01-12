/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.environment

import java.util.*

interface OperationTaskListener : EventListener {
    fun onFinished(task: OperationTask)

    fun onThrowable(task: OperationTask, throwable: Throwable)

    fun onCancel(task: OperationTask)
}