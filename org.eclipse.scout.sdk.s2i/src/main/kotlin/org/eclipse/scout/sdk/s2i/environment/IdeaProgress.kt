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
import com.intellij.openapi.progress.ProgressIndicator
import org.eclipse.scout.sdk.core.log.MessageFormatter.arrayFormat
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.environment.IProgress

open class IdeaProgress(ind: ProgressIndicator?) : IProgress {

    val indicator = ind ?: EmptyProgressIndicator()
    private var m_totalTicks = 0
    private var m_ticksDone = 0

    private var m_step = 0.0
    private var m_percentDone = 0.0
    private var m_percentToConsume = 1.0

    override fun init(totalWork: Int, name: String?, vararg args: Any?): IdeaProgress {
        val msg = arrayFormat(name, *args).message()
        SdkLog.debug(msg)

        m_totalTicks = totalWork
        m_ticksDone = 0
        indicator.text = msg
        if (m_totalTicks > 0) {
            m_step = m_percentToConsume / m_totalTicks
            indicator.isIndeterminate = false
        } else {
            indicator.isIndeterminate = true
        }
        if (!indicator.isRunning) {
            indicator.start()
        }
        worked(0)
        return this
    }

    override fun newChild(work: Int): IdeaProgress {
        // in case there was a child already created before: its tick have already been applied to this parent.
        // but it is only passed to the underlying indicator on the creation of the next child. it is assumed that the first child has finished
        if (!indicator.isIndeterminate) {
            indicator.fraction = m_percentDone
        }

        val child = IdeaProgress(indicator)
        child.m_percentToConsume = m_step * work
        child.m_percentDone = m_percentDone
        m_percentDone += child.m_percentToConsume
        m_ticksDone += work
        return child
    }

    override fun setWorkRemaining(workRemaining: Int): IdeaProgress {
        indicator.checkCanceled()
        m_percentDone += (m_totalTicks - m_ticksDone - workRemaining) * m_step
        indicator.fraction = m_percentDone
        return this
    }

    override fun worked(work: Int): IdeaProgress {
        indicator.checkCanceled()
        m_ticksDone += work
        m_percentDone += (work * m_step)
        if (!indicator.isIndeterminate) {
            indicator.fraction = m_percentDone
        }
        return this
    }
}
