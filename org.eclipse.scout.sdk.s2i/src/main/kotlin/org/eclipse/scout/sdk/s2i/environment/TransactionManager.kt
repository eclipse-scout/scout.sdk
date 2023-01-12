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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.log.SdkLog.onTrace
import org.eclipse.scout.sdk.core.util.CoreUtils.callInContext
import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.toIdea
import org.eclipse.scout.sdk.s2i.toVirtualFile
import java.nio.file.Path

class TransactionManager constructor(val project: Project, val transactionName: String? = null) {

    companion object {

        const val BULK_UPDATE_LIMIT = 100 // see also com.intellij.openapi.application.impl.NonBlockingReadActionImpl.Submission.preventTooManySubmissions
        private val CURRENT = ThreadLocal.withInitial<TransactionManager> { null }

        /**
         * Executes a task within a [TransactionManager] and commits all members on successful completion of the transaction.
         *
         * Successful completion means the given progress monitor is not canceled and no exception is thrown from the [runnable].
         * @param project The [Project] for which the transaction should be started
         * @param name The name that represents the transaction to the user. E.g. when undoing actions (the confirmation dialog will show this text).
         * @param progressProvider A provider for a progress indicator to use when committing the transaction. This provider is also used to determine if the task has been canceled. Only if not canceled the transaction will be committed.
         * @param runnable The runnable to execute
         */
        fun runInNewTransaction(project: Project, name: String? = null, progressProvider: () -> IdeaProgress = { IdeaProgress.empty() }, runnable: () -> Unit) {
            callInNewTransaction(project, name, progressProvider) {
                runnable()
            }
        }

        /**
         * Executes a task within a [TransactionManager] and commits all members on successful completion of the transaction.
         *
         * Successful completion means the given progress monitor is not canceled and no exception is thrown from the [callable].
         * @param project The [Project] for which the transaction should be started
         * @param name The name that represents the transaction to the user. E.g. when undoing actions (the confirmation dialog will show this text).
         * @param progressProvider A provider for a progress indicator to use when committing the transaction. This provider is also used to determine if the task has been canceled. Only if not canceled the transaction will be committed.
         * @param callable The runnable to execute
         */
        fun <R> callInNewTransaction(project: Project, name: String? = null, progressProvider: () -> IdeaProgress = { IdeaProgress.empty() }, callable: () -> R?): R? {
            var save = false
            val transactionManager = TransactionManager(project, name)
            val result: R?
            try {
                result = callInExistingTransaction(transactionManager, callable)
                save = true
            } finally {
                transactionManager.finishTransaction(save, progressProvider())
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
         * The [callable] is executed in the UI thread but the call to this method waits until it is completed.
         *
         * @param project The [Project] for which the [callable] should be executed.
         * @param name The name that represents the write action to the user. E.g. when undoing actions (the confirmation dialog will show this text).
         * @param callable The task to execute
         * @return The result of the [callable].
         */
        private fun <T> computeInWriteAction(project: Project, name: String? = null, callable: () -> T): T? {
            val result = FinalValue<T>()
            // repeat outside write lock to release the UI thread between retries (prevent freezes)
            repeatUntilPassesWithIndex(project) {
                ApplicationManager.getApplication().invokeAndWait {
                    // this is executed in the UI thread! keep short to prevent freezes!
                    // write operations are only allowed in the UI thread
                    // see https://jetbrains.org/intellij/sdk/docs/basics/architectural_overview/general_threading_rules.html
                    result.computeIfAbsent {
                        WriteAction.compute<T, RuntimeException> { computeInCommandProcessor(project, name, callable) }
                    }
                }
            }
            return result.get()
        }

        private fun <T> computeInCommandProcessor(project: Project, name: String? = null, callable: () -> T): T {
            val result = FinalValue<T>()
            if (!project.isInitialized) {
                return result.get()
            }
            CommandProcessor.getInstance().executeCommand(project, {
                result.computeIfAbsent(callable)
            }, name, null)
            return result.get()
        }

        private fun <T> repeatUntilPassesWithIndex(project: Project, callable: () -> T): T {
            val dumbService = DumbService.getInstance(project)
            val readAccessAllowed = ApplicationManager.getApplication().isReadAccessAllowed
            while (true) {
                try {
                    ProgressManager.checkCanceled()
                    if (!project.isInitialized) { // includes disposed | !open
                        throw ProcessCanceledException()
                    }
                    return if (readAccessAllowed) {
                        // we can't wait for smart mode to begin (it would result in a deadlock)
                        // so let's just pretend it's already smart and fail with IndexNotReadyException if not
                        callable()
                    } else {
                        dumbService.waitForSmartMode()
                        ProgressManager.checkCanceled()
                        callable()
                    }
                } catch (e: RuntimeException) {
                    val rootException = unwrap(e)
                    if (rootException is IndexNotReadyException) {
                        SdkLog.debug("Project entered dumb mode unexpectedly. Retrying task.", onTrace(e))
                        if (readAccessAllowed) {
                            // as there is no "waitForSmartMode" when holding the readLock already: quickly wait before retry
                            Thread.sleep(500)
                        }
                    } else {
                        throw e
                    }
                }
            }
        }

        internal fun unwrap(throwable: Throwable): Throwable {
            var t = throwable
            while (t.cause != null && t.cause != t) {
                t = t.cause!!
            }
            return t
        }
    }

    private val m_members = LinkedHashMap<Path, MutableList<TransactionMember>>()
    private val m_documentManager = FileDocumentManager.getInstance()
    private val m_psiDocumentManager = PsiDocumentManager.getInstance(project)

    @Volatile
    private var m_size = 0

    @Volatile
    private var m_open = true

    /**
     * @return true if all transactions members returned true (which means successful)
     */
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
    fun checkpoint(progress: IdeaProgress?) = synchronized(this) { finishTransactionImpl(true, progress.toIdea()) }

    /**
     * Registers the [TransactionMember] specified. If the running transaction ends the member is asked to commit. If the transaction is not committed
     * (e.g. because of an error or because the task was canceled), the member will never be committed. There is no rollback operation.
     * @param member The [TransactionMember] to register.
     */
    fun register(member: TransactionMember) = synchronized(this) {
        ensureOpen(member)
        val listForFile = m_members.computeIfAbsent(member.file()) { ArrayList() }
        val startSize = listForFile.size
        register(member, listForFile)
        m_size += listForFile.size - startSize
    }

    /**
     * Adds the given member to the given list.
     * If the list contains elements for which [TransactionMember.replaces] returns true, these elements are removed from the list.
     */
    private fun register(member: TransactionMember, listForFile: MutableList<TransactionMember>) {
        if (listForFile.isEmpty()) {
            listForFile.add(member)
            return
        }

        var memberInserted = false
        val iterator = listForFile.listIterator()
        while (iterator.hasNext()) {
            val existing = iterator.next()
            if (member.replaces(existing)) {
                if (memberInserted) {
                    // the new member has already been inserted into the list.
                    // But this item is also no longer necessary. remove it
                    iterator.remove()
                } else {
                    iterator.set(member) // replace existing member with the new one
                    memberInserted = true
                }
            }
        }
        if (!memberInserted) {
            listForFile.add(member)
        }
    }

    /**
     * @return The number of [TransactionMember] instances available in this manager
     */
    fun size(): Int = m_size // do not use m_members.flatMap().size() here because this would require synchronization. Then the size() method could no longer be called from commitAllAsync -> deadlock!

    internal fun members() = synchronized(this) {
        m_members.values.flatten()
    }

    private fun finishTransactionImpl(save: Boolean, progress: IdeaProgress): Boolean {
        ensureOpen(null)
        if (m_members.isEmpty()) return true
        try {
            if (!save || progress.indicator.isCanceled) return false
            return computeInWriteAction(project, transactionName) { commitAllInUiThread(progress) } ?: return false
        } finally {
            m_members.clear()
            m_size = 0
        }
    }

    private fun commitAllInUiThread(progress: IdeaProgress): Boolean {
        val workForEnsureWritable = 1
        progress.init(size() + workForEnsureWritable, message("starting.commit.transaction.x", size()))

        // map member path to virtual files
        val files = m_members.keys.associateWith { it.toVirtualFile() }.toMap()

        // make file writable
        val status = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(files.values.filterNotNull())
        if (status.hasReadonlyFiles()) {
            SdkLog.info("Unable to make all resources writable. Transaction will be discarded. Message: ${status.readonlyFilesMessage}")
            return false
        }
        progress.worked(workForEnsureWritable)

        // map to documents
        val documentMappings = files
            .map { pair -> pair.key to pair.value?.let { Pair(it, m_documentManager.getDocument(it)) } }
            .toMap(HashMap())

        // validate documents and prepare for modification
        val documentsReady = documentMappings.values.filterNotNull().all { documentReady(it.second) }
        if (!documentsReady) {
            SdkLog.warning("Cannot commit all transaction members because at least one document cannot be written.")
            return false
        }

        // commit transaction members
        return commitTransaction(documentMappings, progress)
    }

    private fun documentReady(document: Document?): Boolean {
        if (document == null) {
            return true // no document exists yet: new file
        }
        if (!document.isWritable) {
            return false
        }
        return true
    }

    private fun commitTransaction(documentMappings: MutableMap<Path, Pair<VirtualFile, Document?>?>, progress: IdeaProgress) = m_members.entries.minOfOrNull { commitMembersAndPersist(it.key, it.value, documentMappings, progress) } ?: false

    private fun commitMembersAndPersist(path: Path, members: List<TransactionMember>, documentMappings: MutableMap<Path, Pair<VirtualFile, Document?>?>, progress: IdeaProgress): Boolean {
        val success = members.minOfOrNull { member -> commitMember(member, path, documentMappings, progress.newChild(1)) } ?: false
        if (success) {
            val document = documentMappings[path]?.second
            if (document != null && m_documentManager.isDocumentUnsaved(document)) {
                m_documentManager.saveDocument(document)
            }
        }
        return success
    }

    private fun commitMember(member: TransactionMember, path: Path, documentMappings: MutableMap<Path, Pair<VirtualFile, Document?>?>, progress: IdeaProgress): Boolean {
        try {
            prepareDocumentForEdit(path, documentMappings)
            if (member.commit(progress)) {
                updateDocumentMappings(path, documentMappings)
                return true
            }
        } catch (indexException: IndexNotReadyException) {
            throw indexException // will be handled by the retry
        } catch (cancelException: ProcessCanceledException) {
            SdkLog.debug("Transaction member '{}' was cancelled.", member)
        } catch (e: Exception) {
            SdkLog.warning("Error committing transaction member '$member'.", e)
        }
        return false
    }

    private fun prepareDocumentForEdit(path: Path, documentMappings: MutableMap<Path, Pair<VirtualFile, Document?>?>) {
        val document = documentMappings[path]?.second ?: return
        if (m_psiDocumentManager.isDocumentBlockedByPsi(document)) {
            m_psiDocumentManager.doPostponedOperationsAndUnblockDocument(document)
        }
        if (m_psiDocumentManager.isUncommited(document)) {
            m_psiDocumentManager.commitDocument(document)
        }
    }

    private fun updateDocumentMappings(path: Path, documentMappings: MutableMap<Path, Pair<VirtualFile, Document?>?>) {
        val mapping = documentMappings[path]
        var vFile = mapping?.first
        if (vFile == null) {
            vFile = path.toVirtualFile()
        }
        var document = mapping?.second
        if (document == null && vFile != null) {
            document = m_documentManager.getDocument(vFile) // get document for new created file
        }
        if (vFile != null) {
            documentMappings[path] = Pair(vFile, document)
        }
    }

    private fun ensureOpen(member: TransactionMember?) {
        Ensure.isTrue(m_open, "Transaction has already been committed. Tried to register member '{}'.", member)
    }
}

