package org.eclipse.scout.sdk.s2i.environment

import java.nio.file.Path

interface TransactionMember {

    fun file(): Path

    fun commit(progress: IdeaProgress): Boolean

}