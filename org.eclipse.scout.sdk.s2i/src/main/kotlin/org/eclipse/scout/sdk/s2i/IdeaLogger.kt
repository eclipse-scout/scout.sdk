package org.eclipse.scout.sdk.s2i

import com.intellij.notification.NotificationGroup
import com.intellij.openapi.components.BaseComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.MessageType
import org.eclipse.scout.sdk.core.log.ISdkConsoleSpi
import org.eclipse.scout.sdk.core.log.SdkConsole
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.settings.ScoutSettings
import org.eclipse.scout.sdk.s2i.settings.SettingsChangedListener
import java.util.*
import java.util.logging.Level

open class IdeaLogger : ISdkConsoleSpi, BaseComponent, SettingsChangedListener {

    private val m_textLog = Logger.getInstance(IdeaLogger::class.java)
    private val m_balloonLog = NotificationGroup.balloonGroup("Scout SDK")

    enum class LogLevel(private val text: String) {
        ERROR("Error"),
        WARNING("Warning"),
        INFO("Info"),
        DEBUG("Debug");

        companion object {
            fun parse(text: String?): LogLevel {
                if (text == null) {
                    return WARNING
                }
                return valueOf(text.toUpperCase(Locale.US))
            }
        }

        override fun toString(): String {
            return text
        }
    }

    override fun initComponent() {
        SdkConsole.setConsoleSpi(this)
        ScoutSettings.addListener(this)

        if (!ScoutSettings.isLogLevelSet() && isRunningInSandbox()) {
            // default log level for dev mode
            ScoutSettings.setLogLevel(LogLevel.INFO)
        }

        refreshLogLevel()
    }

    override fun disposeComponent() {
        ScoutSettings.removeListener(this)
        SdkConsole.setConsoleSpi(null)
    }

    override fun changed(key: String, oldVal: String?, newVal: String?) {
        if (ScoutSettings.KEY_LOG_LEVEL == key) {
            refreshLogLevel()
        }
    }

    protected fun isRunningInSandbox(): Boolean {
        val sandbox = "sandbox"
        return Strings.countMatches(System.getProperty("idea.plugins.path"), sandbox) > 0
                || Strings.countMatches(System.getProperty("idea.config.path"), sandbox) > 0
                || Strings.countMatches(System.getProperty("idea.system.path"), sandbox) > 0
    }

    protected fun refreshLogLevel() {
        SdkLog.setLogLevel(ideaToJulLevel(ScoutSettings.getLogLevel()))
        m_textLog.setLevel(org.apache.log4j.Level.DEBUG) // so that the level filtering of Scout is active
    }

    protected fun ideaToJulLevel(ideaLevel: LogLevel): Level =
            when (ideaLevel) {
                LogLevel.ERROR -> Level.SEVERE
                LogLevel.INFO -> Level.INFO
                LogLevel.DEBUG -> Level.FINE
                else -> Level.WARNING
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
