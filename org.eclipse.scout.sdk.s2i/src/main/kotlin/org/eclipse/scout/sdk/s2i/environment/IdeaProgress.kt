package org.eclipse.scout.sdk.s2i.environment

import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import org.eclipse.scout.sdk.core.s.environment.IProgress

open class IdeaProgress(ind: ProgressIndicator?) : IProgress {

    val indicator = ind ?: EmptyProgressIndicator()
    private var m_totalTicks = 0
    private var m_ticksDone = 0

    private var m_step = 0.0
    private var m_percentDone = 0.0
    private var m_percentToConsume = 1.0

    override fun init(name: String?, totalWork: Int): IdeaProgress {
        m_totalTicks = totalWork
        m_ticksDone = 0
        indicator.text = name
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
        indicator.fraction = m_percentDone
        return this
    }
}