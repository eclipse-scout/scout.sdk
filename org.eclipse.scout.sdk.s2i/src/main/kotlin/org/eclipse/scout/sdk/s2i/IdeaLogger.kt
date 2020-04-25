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
package org.eclipse.scout.sdk.s2i

import com.intellij.notification.NotificationGroup
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.ControlFlowException
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Disposer
import org.eclipse.scout.sdk.core.log.ISdkConsoleSpi
import org.eclipse.scout.sdk.core.log.LogMessage
import org.eclipse.scout.sdk.core.log.SdkConsole
import org.eclipse.scout.sdk.core.util.Strings
import java.util.logging.Level

open class IdeaLogger : ISdkConsoleSpi, StartupActivity, DumbAware, Disposable {

    private val m_textLog = Logger.getInstance(IdeaLogger::class.java)
    @Suppress("MissingRecentApi") // method does not exist on the companion, but static access is available -> ok
    private val m_balloonLog = NotificationGroup.balloonGroup("Scout")
    private var m_previousConsoleSpi: ISdkConsoleSpi? = null

    /**
     * Executed on [Project] open
     */
    override fun runActivity(project: Project) {
        val existingConsoleSpi = SdkConsole.getConsoleSpi()
        if (existingConsoleSpi == this) {
            // there is already a project open which registered the logger already
            return
        }

        Disposer.register(project, this)
        m_previousConsoleSpi = existingConsoleSpi
        SdkConsole.setConsoleSpi(this)

        if (isRunningInSandbox()) {
            m_textLog.setLevel(org.apache.log4j.Level.DEBUG)
        }
    }

    protected fun isRunningInSandbox(): Boolean {
        val sandbox = "sandbox"
        return Strings.countMatches(PathManager.getPluginsPath(), sandbox) > 0
                || Strings.countMatches(PathManager.getConfigPath(), sandbox) > 0
                || Strings.countMatches(PathManager.getSystemPath(), sandbox) > 0
    }

    /**
     * Executed on [Project] close
     */
    override fun dispose() {
        SdkConsole.setConsoleSpi(m_previousConsoleSpi)
        m_previousConsoleSpi = null
    }

    override fun clear() {
        // nop
    }

    override fun isEnabled(level: Level): Boolean {
        if (level == Level.OFF) {
            return false
        }
        if (level == Level.FINE) {
            return m_textLog.isDebugEnabled
        }
        if (level.intValue() < Level.FINE.intValue()) {
            return m_textLog.isTraceEnabled
        }
        return true
    }

    override fun println(msg: LogMessage) {
        logToTextFile(msg)
        if (msg.severity() == Level.SEVERE || msg.severity() == Level.WARNING) {
            logToEventLogWindow(msg)
        }
    }

    protected fun logToEventLogWindow(msg: LogMessage) {
        if (!Strings.hasText(msg.text())) {
            return // no balloon for empty log message
        }
        val notification = m_balloonLog.createNotification(msg.text(), levelToMessageType(msg.severity()))
        notification.isImportant = msg.severity() == Level.SEVERE
        notification.notify(null)
    }

    protected fun logToTextFile(msg: LogMessage) {
        val exception: Throwable? = msg.throwables()
                .filter { it !is ControlFlowException } // ControlFlowExceptions should not be logged. See com.intellij.openapi.diagnostic.Logger.checkException
                .findFirst()
                .orElse(null)
        // do not log the prefix here as this information is already logged by the text logger
        when (msg.severity()) {
            Level.SEVERE -> m_textLog.error(msg.text(), exception)
            Level.WARNING -> m_textLog.warn(msg.text(), exception)
            Level.INFO -> m_textLog.info(msg.text(), exception)
            Level.FINE -> m_textLog.debug(msg.text(), exception)
            else -> {
                m_textLog.trace(msg.text())
                exception?.let { m_textLog.trace(it) }
            }
        }
    }

    protected fun levelToMessageType(level: Level): MessageType = when (level) {
        Level.WARNING -> MessageType.WARNING
        Level.SEVERE -> MessageType.ERROR
        else -> MessageType.INFO
    }
}
