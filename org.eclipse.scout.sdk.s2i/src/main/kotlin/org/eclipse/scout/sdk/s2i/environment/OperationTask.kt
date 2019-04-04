package org.eclipse.scout.sdk.s2i.environment

import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.util.EventListenerList
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.s2i.toScoutProgress

open class OperationTask(private val task: (IdeaProgress) -> Unit, title: String, project: Project?) : Task.Backgroundable(project, title, true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {

    private val m_progress = FinalValue<ProgressIndicator>()
    private val m_listeners = EventListenerList()

    override fun run(indicator: ProgressIndicator) {
        m_progress.setIfAbsent(indicator)
        indicator.checkCanceled()
        task.invoke(indicator.toScoutProgress())
    }

    fun cancel(): Boolean {
        return m_progress.opt()
                .map { it.cancel(); true; }
                .orElse(false)
    }

    fun schedule(): IFuture<Void> {
        return schedule(null)
    }

    fun <T> schedule(resultSupplier: (() -> T)?): IFuture<T> {
        val result = TaskFuture(this, resultSupplier)
        queue()
        return result
    }

    fun addListener(listener: OperationTaskListener) = m_listeners.add(listener)

    fun removeListener(listener: OperationTaskListener): Boolean = m_listeners.remove(listener)


    override fun onFinished() {
        super.onFinished()
        m_listeners.get(OperationTaskListener::class.java)
                .forEach { it.onFinished(this) }
    }

    override fun onThrowable(error: Throwable) {
        // no super call here
        m_listeners.get(OperationTaskListener::class.java)
                .forEach { it.onThrowable(this, error) }
    }

    override fun onCancel() {
        super.onCancel()
        m_listeners.get(OperationTaskListener::class.java)
                .forEach { it.onCancel(this) }
    }
}