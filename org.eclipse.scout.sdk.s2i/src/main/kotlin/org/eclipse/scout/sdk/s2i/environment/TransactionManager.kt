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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.util.CoreUtils.callInContext
import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.core.util.FinalValue
import java.nio.file.Path

class TransactionManager constructor(val project: Project) {

    companion object {

        private val CURRENT = ThreadLocal.withInitial<TransactionManager> { null }

        /**
         * Executes a task within a [TransactionManager] and commits all members on successful completion of the transaction.
         *
         * Successful completion means the given progress monitor is not canceled and no exception is thrown from the [runnable].
         * @param project The [Project] for which the transaction should be started
         * @param progressProvider A provider for a progress indicator to use when committing the transaction. This provider is also used to determine if the task has been canceled. Only if not canceled the transaction will be committed.
         * @param runnable The runnable to execute
         */
        fun runInNewTransaction(project: Project, progressProvider: () -> IdeaProgress = { IdeaEnvironment.toIdeaProgress(null) }, runnable: () -> Unit) {
            callInNewTransaction(project, progressProvider) {
                runnable.invoke()
            }
        }

        /**
         * Executes a task within a [TransactionManager] and commits all members on successful completion of the transaction.
         *
         * Successful completion means the given progress monitor is not canceled and no exception is thrown from the [callable].
         * @param project The [Project] for which the transaction should be started
         * @param progressProvider A provider for a progress indicator to use when committing the transaction. This provider is also used to determine if the task has been canceled. Only if not canceled the transaction will be committed.
         * @param callable The runnable to execute
         */
        fun <R> callInNewTransaction(project: Project, progressProvider: () -> IdeaProgress = { IdeaEnvironment.toIdeaProgress(null) }, callable: () -> R?): R? {
            var save = false
            val transactionManager = TransactionManager(project)
            val result: R?
            try {
                result = callInExistingTransaction(transactionManager, callable)
                save = true
            } finally {
                transactionManager.finishTransaction(save, progressProvider.invoke())
            }
            return result
        }

        /**
         * Executes the [callable] specified in the context of the [TransactionManager] specified.
         *
         * If the [callable] throws an exception the transaction is not automatically discarded. It is the responsibility of the caller to decide if the thrown exception is a
         * reason to abort the transaction by rethrowing it up to the transaction creation or catching the exception and continue the transaction.
         * @param transactionManager The [TransactionManager] under which the [callable] should be executed. Must not be null.
         * @param callable The task to execute. Must not be null.
         */
        fun <R> callInExistingTransaction(transactionManager: TransactionManager, callable: () -> R): R = callInContext(CURRENT, transactionManager, callable)

        /**
         * Retrieves the [TransactionManager] of the current thread.
         *
         * Throws an exception if there is no transaction registered on the current thread.
         * @return the [TransactionManager] of the current thread.
         */
        fun current(): TransactionManager = Ensure.notNull(CURRENT.get(), "No transaction available in context")

        /**
         * Executes the [callable] specified holding all necessary write locks.
         *
         * @param project The [Project] for which the [callable] should be executed.
         * @param callable The task to execute
         * @return The result of the [callable].
         */
        fun <T> computeInWriteAction(project: Project, callable: () -> T): T {
            val result = FinalValue<T>()
            ApplicationManager.getApplication().invokeAndWait {
                // this is executed in the UI thread! keep short to prevent freezes!
                // write operations are only allowed in the UI thread
                // see http://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/general_threading_rules.html
                result.computeIfAbsent {
                    WriteAction.compute<T, RuntimeException> { computeInCommandProcessor(project, callable) }
                }
            }
            return result.get()
        }

        private fun <T> computeInCommandProcessor(project: Project, callable: () -> T): T {
            val result = FinalValue<T>()
            CommandProcessor.getInstance().executeCommand(project, { result.computeIfAbsent(callable) }, null, null)
            return result.get()
        }
    }

    private val m_members = HashMap<Path, MutableList<TransactionMember>>()
    @Volatile
    private var m_size = 0
    @Volatile
    private var m_open = true

    private fun finishTransaction(save: Boolean, progress: IdeaProgress): Boolean = synchronized(this) {
        try {
            return finishTransactionImpl(save, progress)
        } finally {
            m_open = false
        }
    }

    /**
     * Tries to commit all [TransactionMember]s registered so far. This [TransactionManager] may be used to
     * register more [TransactionMember]s afterwards.
     *
     * @param progress The [IdeaProgress] for progress indication
     * @return true if all [TransactionMember]s have been committed successfully
     */
    @Suppress("unused")
    fun checkpoint(progress: IdeaProgress?) = synchronized(this) { finishTransactionImpl(true, IdeaEnvironment.toIdeaProgress(progress)) }

    /**
     * Registers the [TransactionMember] specified. If the running transaction ends the member is asked to commit. If the transaction is not committed
     * (e.g. because of an error or because the task was canceled), the member will never be committed. There is no rollback operation.
     * @param member The [TransactionMember] to register.
     */
    fun register(member: TransactionMember) = synchronized(this) {
        ensureOpen()
        m_members.computeIfAbsent(member.file()) { ArrayList() }
                .add(member)
        m_size++
    }

    /**
     * @return The number of [TransactionMember] instances available in this manager
     */
    fun size(): Int = m_size // do not use m_members.flatMap().size() here because this would require synchronization. Then the size() method could not longer be called from commitAllAsync -> deadlock!

    private fun finishTransactionImpl(save: Boolean, progress: IdeaProgress): Boolean {
        ensureOpen()
        if (m_members.isEmpty()) {
            return true
        }

        try {
            if (!save || progress.indicator.isCanceled) {
                return false
            }
            return computeInWriteAction(project) { commitAllAsync(progress) }
        } finally {
            m_members.clear()
            m_size = 0
        }
    }

    private fun commitAllAsync(progress: IdeaProgress): Boolean {
        val workForEnsureWritable = 1
        progress.init("Flush file content", size() + workForEnsureWritable)

        val fileSystem = LocalFileSystem.getInstance()
        val existingFiles = m_members.keys
                .map(Path::toFile)
                .mapNotNull(fileSystem::findFileByIoFile)

        val status = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(existingFiles)
        if (status.hasReadonlyFiles()) {
            SdkLog.info("Unable to make all resources writable. Transaction will be discarded. Message: ${status.readonlyFilesMessage}")
            return false
        }
        progress.worked(workForEnsureWritable)

        return m_members.values
                .flatten()
                .map { commitMember(it, progress.newChild(1)) }
                .all { committed -> committed }
    }

    private fun commitMember(member: TransactionMember, progress: IdeaProgress) =
            try {
                member.commit(progress)
            } catch (e: Exception) {
                SdkLog.warning("Error committing transaction member '$member'.", e)
                false
            }

    private fun ensureOpen() {
        Ensure.isTrue(m_open, "Transaction has already been committed.")
    }
}
