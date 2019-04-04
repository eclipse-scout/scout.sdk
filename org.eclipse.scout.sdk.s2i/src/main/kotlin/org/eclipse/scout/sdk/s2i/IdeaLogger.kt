package org.eclipse.scout.sdk.s2i

import com.intellij.notification.NotificationGroup
import com.intellij.openapi.components.BaseComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.MessageType
import org.eclipse.scout.sdk.core.log.ISdkConsoleSpi
import org.eclipse.scout.sdk.core.log.SdkConsole
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import org.eclipse.scout.sdk.s2i.settings.SettingsChangedListener
import java.util.logging.Level

open class IdeaLogger : ISdkConsoleSpi, BaseComponent, SettingsChangedListener {

    private val m_textLog = Logger.getInstance(IdeaLogger::class.java)
    private val m_balloonLog = NotificationGroup.balloonGroup("Scout SDK")

    override fun initComponent() {
        SdkConsole.setConsoleSpi(this)
        ScoutSettings.addListener(this)
        refreshLogLevel()
    }

    override fun disposeComponent() {
        ScoutSettings.removeListener(this)
        SdkConsole.setConsoleSpi(null)
    }

    override fun changed(key: String, oldVal: String?, newVal: String?) {
        if (ScoutSettings.logLevelKey == key) {
            refreshLogLevel()
        }
    }

    protected fun refreshLogLevel() =
            SdkLog.setLogLevel(ideaToJulLevel(ScoutSettings.logLevel()))

    protected fun ideaToJulLevel(ideaLevel: String): Level =
            when (ideaLevel) {
                "Error" -> Level.SEVERE
                "Info" -> Level.INFO
                "Debug" -> Level.FINE
                else -> Level.FINE
            }

    override fun clear() {
        // nop
    }

    override fun println(level: Level, s: String, vararg exceptions: Throwable?) {
        logToTextFile(level, s, exceptions.firstOrNull())
        if (level == Level.SEVERE || level == Level.WARNING) {
            logToEventLogWindow(level, s)
        }
    }

    protected fun logToEventLogWindow(level: Level, s: String) {
        val notification = m_balloonLog.createNotification(s, levelToMessageType(level))
        notification.isImportant = level == Level.SEVERE
        notification.notify(null)
    }

    protected fun logToTextFile(level: Level, s: String, exception: Throwable?) {
        when (level) {
            Level.SEVERE -> m_textLog.error(s, exception)
            Level.WARNING -> m_textLog.warn(s, exception)
            Level.INFO -> m_textLog.info(s, exception)
            else -> m_textLog.debug(s, exception)
        }
    }

    protected fun levelToMessageType(level: Level): MessageType = when (level) {
        Level.WARNING -> MessageType.WARNING
        Level.SEVERE -> MessageType.ERROR
        else -> MessageType.INFO
    }
}