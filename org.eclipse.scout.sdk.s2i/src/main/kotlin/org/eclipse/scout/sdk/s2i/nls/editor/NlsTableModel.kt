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
package org.eclipse.scout.sdk.s2i.nls.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.nls.*
import org.eclipse.scout.sdk.core.s.nls.TranslationValidator.*
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import java.util.Collections.singleton
import java.util.function.Predicate
import java.util.stream.Collectors.toList
import java.util.stream.Stream
import javax.swing.table.AbstractTableModel
import kotlin.streams.toList

class NlsTableModel(val stack: TranslationStoreStack, val project: Project) : AbstractTableModel() {

    private var m_filter: Predicate<ITranslationEntry>? = null
    private var m_translations: MutableList<ITranslationEntry>? = null
    private var m_languages: MutableList<Language>? = null

    companion object {
        val KEY_COLUMN_HEADER_NAME = EclipseScoutBundle.message("key")
        const val NUM_ADDITIONAL_COLUMNS = 1
        const val KEY_COLUMN_INDEX = 0
        const val DEFAULT_LANGUAGE_COLUMN_INDEX = 1
    }

    init {
        stack.addListener(StackListener())
        buildCache()
    }

    fun translations() = m_translations!!

    fun languages() = m_languages!!

    override fun getRowCount() = translations().size

    override fun getColumnCount() = NUM_ADDITIONAL_COLUMNS + languages().size

    fun setFilter(newFilter: Predicate<ITranslationEntry>?): Boolean {
        m_filter = newFilter
        return buildCache()
    }

    fun languageForColumn(columnIndex: Int) = languages()[columnIndex - NUM_ADDITIONAL_COLUMNS]

    fun translationForRow(rowIndex: Int) = translations()[rowIndex]

    fun rowForTranslation(translation: ITranslationEntry) = translations().indexOf(translation)

    private fun acceptFilter(candidate: ITranslationEntry) = m_filter?.test(candidate) ?: true

    private fun buildCache(forceReload: Boolean = false): Boolean {
        val newTranslations = stack.allEntries().collect(toList())
        val newLanguages = newTranslations.stream()
                .filter { acceptFilter(it) }
                .flatMap { it.store().languages() }
                .distinct()
                .sorted()
                .collect(toList())

        if (forceReload || m_translations == null || m_languages != newLanguages) {
            m_translations = newTranslations
            m_languages = newLanguages
            fireTableStructureChanged()
            return true
        }
        return false
    }

    private fun saveStack() = callInIdeaEnvironment(project, EclipseScoutBundle.message("saving.translations")) { env, progress ->
        stack.flush(env, progress)
    }

    override fun getColumnName(column: Int): String {
        if (KEY_COLUMN_INDEX == column) {
            return KEY_COLUMN_HEADER_NAME
        }
        return languageForColumn(column).displayName()
    }

    override fun getColumnClass(c: Int) = String::class.java

    override fun getValueAt(rowIndex: Int, columnIndex: Int): String {
        val entry = translationForRow(rowIndex)
        if (KEY_COLUMN_INDEX == columnIndex) {
            return entry.key()
        }
        val lang = languageForColumn(columnIndex)
        return entry.text(lang).orElse("")
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        val entry = translationForRow(rowIndex)
        return entry.store().isEditable
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        val validationResult = validate(aValue, rowIndex, columnIndex)
        if (isForbidden(validationResult)) {
            return
        }

        val text = aValue.toString()
        val toUpdate = translationForRow(rowIndex)
        if (KEY_COLUMN_INDEX == columnIndex) {
            val newKey = text.trim()
            if (newKey == toUpdate.key()) {
                // no save necessary
                return
            }
            stack.changeKey(toUpdate.key(), newKey)
        } else {
            val lang = languageForColumn(columnIndex)
            val updated = Translation(toUpdate)
            updated.putText(lang, text)
            stack.updateTranslation(updated)
        }
    }

    fun validate(aValue: Any?, rowIndex: Int, columnIndex: Int): Int {
        if (columnIndex == KEY_COLUMN_INDEX) {
            val selectedTranslation = translationForRow(rowIndex)
            val key = aValue?.toString()?.trim()
            return validateKey(stack, selectedTranslation.store(), key, singleton(selectedTranslation.key()))
        }
        if (columnIndex == DEFAULT_LANGUAGE_COLUMN_INDEX) {
            return validateDefaultText(aValue?.toString())
        }
        return OK
    }

    private inner class StackListener : ITranslationStoreStackListener {

        override fun stackChanged(events: Stream<TranslationStoreStackEvent>) {
            val allEvents = events.toList()
            val application = ApplicationManager.getApplication()
            if (application.isDispatchThread) {
                handleEvents(allEvents)
            } else {
                // Run in EDT. This is necessary e.g. for reload events which might come from a worker thread.
                // Do not wait here for the events to be handled (deadlock)
                ApplicationManager.getApplication().invokeLater {
                    handleEvents(allEvents)
                }
            }
        }

        private fun handleEvents(events: List<TranslationStoreStackEvent>) {
            val containsReloadEvent = events.map { it.type() }.any { it == TranslationStoreStackEvent.TYPE_RELOAD }
            if (containsReloadEvent) {
                buildCache(true)
                return
            }

            events.forEach { handleEvent(it) }
            val needsSave = events.map { it.type() }.any {
                it == TranslationStoreStackEvent.TYPE_REMOVE_TRANSLATION
                        || it == TranslationStoreStackEvent.TYPE_NEW_TRANSLATION
                        || it == TranslationStoreStackEvent.TYPE_KEY_CHANGED
                        || it == TranslationStoreStackEvent.TYPE_UPDATE_TRANSLATION
                        || it == TranslationStoreStackEvent.TYPE_NEW_LANGUAGE
            }
            if (needsSave) {
                SdkLog.debug("About to save translation store stack.")
                saveStack()
            }
        }

        private fun handleEvent(event: TranslationStoreStackEvent) {
            when (event.type()) {
                TranslationStoreStackEvent.TYPE_REMOVE_TRANSLATION -> translationsRemoved(event)
                TranslationStoreStackEvent.TYPE_NEW_TRANSLATION -> translationsAdded(event)
                TranslationStoreStackEvent.TYPE_KEY_CHANGED -> translationsUpdated(event)
                TranslationStoreStackEvent.TYPE_UPDATE_TRANSLATION -> translationsUpdated(event)
                TranslationStoreStackEvent.TYPE_NEW_LANGUAGE -> buildCache()
            }
        }

        private fun translationsUpdated(event: TranslationStoreStackEvent) = event.entry().ifPresent {
            val index = rowForTranslation(it)
            fireTableRowsUpdated(index, index)
        }

        private fun translationsAdded(event: TranslationStoreStackEvent) = event.entry().ifPresent {
            val translations = translations()
            translations.add(it)
            val index = translations.size - 1
            fireTableRowsInserted(index, index)
        }

        private fun translationsRemoved(event: TranslationStoreStackEvent) = event.entry().ifPresent {
            val index = rowForTranslation(it)
            translations().removeAt(index)
            fireTableRowsDeleted(index, index)
        }
    }
}