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

import java.nio.file.Path

/**
 * Represents an action to be executed for a file as part of the running transaction
 *
 * When running in a transaction scope (see [TransactionManager.callInNewTransaction]) members may be added to the transaction using [TransactionManager.register] (the current manager can be retrieved using [TransactionManager.current]).
 *
 * If all the code within the transaction scope is executed without throwing exceptions, the transaction tries to commit all members registered. Otherwise, they are discarded and nothing is done.
 */
interface TransactionMember {

    /**
     * @return The absolute path of the file that might be changed by this member. The member is not allowed to change any files other than the one provided here.
     * The transaction manager takes care of acquiring any necessary locks required to write that file (which includes possibly installed SCM systems using pessimistic locking).
     */
    fun file(): Path

    /**
     * Tries to commit the changes to the file as returned by [file]. The commit operation may not change any other files!
     *
     * As all write operations within the platform have to run in the UI thread the commit operation should be as short as possible (prevent freezes).
     *
     * Please note: While committing the transaction the current thread holds the necessary write locks and runs in smart mode on the UI thread.
     * Thanks to the smart mode the commit operation may make use of indices.
     * But as of the characteristic of the platform the smart mode may end at any time (which throws an exception when trying to access an index).
     * The transaction manager catches that exception and performs a retry by executing all members of the transaction again.
     * This means the commit operation must be implemented in an idempotent way (must be capable to run several times with always the same result).
     *
     * If the commit operation throws an exception the other members will be executed even though (commit continues). This gives the transaction the characteristic of "writing as much as I can" which may result in an inconsistent state.
     * The whole transaction will then be marked as failed.
     *
     * @param progress The [IdeaProgress] which may be used to cancel the commit operation (if some dramatic changes occurred). As commit operations should run very short it should not be necessary to indicate any progress.
     * @return true if the commit was executed successfully. If all members return true, the transaction is marked as successful. Returning false does not prevent any subsequent members to be executed. Neither does throwing exceptions.
     * If aborting the commit is required use the progress indicator provided.
     */
    fun commit(progress: IdeaProgress): Boolean

    /**
     * Specifies if this [TransactionMember] should replace the given one. This method is only called for members having the same [file].
     * @param member The [TransactionMember] which should be checked
     * @return true if this instance should replace the given member.
     * This is typically the case if a member performs an idempotent action.
     * In that case any existing member performing the same action can be replaced.
     */
    fun replaces(member: TransactionMember) = false
}