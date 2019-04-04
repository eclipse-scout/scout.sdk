package org.eclipse.scout.sdk.s2i.settings

import java.util.*

@FunctionalInterface
interface SettingsChangedListener : EventListener {
    fun changed(key: String, oldVal: String?, newVal: String?)
}