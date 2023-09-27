/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.util.compat

import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.util.messages.Topic
import org.apache.commons.lang3.ClassUtils
import org.eclipse.scout.sdk.core.log.SdkLog

/**
 * Compatibility Layer for IJ 2023.3 in which AppTopics class is deprecated.
 * This class can be removed as soon as IJ 2023.3 is the oldest supported version.
 */
object AppTopics {
    fun fileDocumentSync(): Topic<FileDocumentManagerListener> {
        val topic = try {
            // IJ >= 2023.3
            FileDocumentManagerListener::class.java.getField("TOPIC").get(null)
        } catch (e: Throwable) {
            SdkLog.debug("Using legacy file document sync topic.", e)
            // IJ < 2023.3
            ClassUtils.getClass(AppTopics::class.java.classLoader, "com.intellij.AppTopics", true).getField("FILE_DOCUMENT_SYNC").get(null)
        }
        @Suppress("UNCHECKED_CAST")
        return topic as Topic<FileDocumentManagerListener>
    }
}