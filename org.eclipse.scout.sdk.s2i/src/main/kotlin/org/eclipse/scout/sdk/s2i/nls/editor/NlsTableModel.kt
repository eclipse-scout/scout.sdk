/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.nls.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.nls.ITranslationManagerListener
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.s.nls.Translation
import org.eclipse.scout.sdk.core.s.nls.TranslationValidator.*
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManagerEvent
import org.eclipse.scout.sdk.core.util.EventListenerList
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment.Factory.callInIdeaEnvironment
import java.util.Collections.singleton
import java.util.stream.Collectors.toList
import java.util.stream.Stream
import javax.swing.table.AbstractTableModel
import kotlin.streams.toList

class NlsTableModel(val translationManager: TranslationManager, val project: Project) : AbstractTableModel() {

    private var m_translationFilter: ((IStackedTranslation) -> Boolean)? = null
    private var m_languageFilter: ((Language) -> Boolean)? = null
    private var m_translations: MutableList<IStackedTranslation>? = null
    private var m_languages: MutableList<Language>? = null
    private val m_managerListener = ManagerListener() // store as member because the listener is weak!
    private val m_dataChangedListeners = ArrayList<(List<Int>) -> Unit>()

    companion object {
        val KEY_COLUMN_HEADER_NAME = EclipseScoutBundle.message("key")
        const val NUM_ADDITIONAL_COLUMNS = 1
        const val KEY_COLUMN_INDEX = 0
        const val DEFAULT_LANGUAGE_COLUMN_INDEX = 1
    }

    init {
        translationManager.addListener(m_managerListener)
        buildCache()
    }

    fun translations() = m_translations!!

    fun languages() = m_languages!!

    internal fun addDataChangedListener(listener: (List<Int>) -> Unit) {
        m_dataChangedListeners.add(listener)
    }

    private fun fireDataChangedListeners(affectedRows: List<Int>) = m_dataChangedListeners.forEach { it(affectedRows) }

    override fun getRowCount() = translations().size

    override fun getColumnCount() = NUM_ADDITIONAL_COLUMNS + languages().size

    fun setFilter(rowFilter: ((IStackedTranslation) -> Boolean)?, columnFilter: ((Language) -> Boolean)?): Boolean {
        m_translationFilter = rowFilter
        m_languageFilter = columnFilter
        return buildCache()
    }

    fun languageForColumn(columnIndex: Int) = languages()[columnIndex - NUM_ADDITIONAL_COLUMNS]

    fun translationForRow(rowIndex: Int) = translations()[rowIndex]

    fun rowForTranslation(translation: IStackedTranslation) = translations().indexOf(translation)

    fun rowForTranslationWithKey(key: String): Int {
        val translations = translations()
        for (i in 0 until translations.size) {
            if (translations[i].key() == key) {
                return i
            }
        }
        return -1
    }

    private fun acceptTranslationFilter(candidate: IStackedTranslation) = m_translationFilter?.invoke(candidate) ?: true

    private fun acceptLanguageFilter(candidate: Language) = m_languageFilter?.invoke(candidate) ?: true

    private fun buildCache(forceReload: Boolean = false): Boolean {
        val newTranslations = translationManager.allTranslations().collect(toList())
        val newLanguages = newTranslations.stream()
                .filter { acceptTranslationFilter(it) }
                .flatMap { it.languagesOfAllStores() }
                .distinct()
                .filter { acceptLanguageFilter(it) }
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
        if (columnIndex == KEY_COLUMN_INDEX) {
            return entry.hasOnlyEditableStores()
        }
        return entry.hasEditableStores()
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        val validationResult = validate(aValue, rowIndex, columnIndex)
        if (isForbidden(validationResult)) {
            return
        }

        val text = aValue.toString()
        val toUpdate = translationForRow(rowIndex)
        try {
            if (KEY_COLUMN_INDEX == columnIndex) {
                val newKey = Strings.trim(text).toString()
                if (newKey == toUpdate.key()) {
                    // no save necessary
                    return
                }
                translationManager.changeKey(toUpdate.key(), newKey)
            } else {
                val lang = languageForColumn(columnIndex)
                val updated = Translation(toUpdate)
                updated.putText(lang, text)
                translationManager.setTranslation(updated)
            }
        } catch (e: RuntimeException) {
            SdkLog.error("Unable to save value.", e)
        }
    }

    fun validate(aValue: Any?, rowIndex: Int, columnIndex: Int): Int {
        val selectedTranslation = translationForRow(rowIndex)
        val newCellValue = Strings.trim(aValue?.toString())?.toString()
        if (columnIndex == KEY_COLUMN_INDEX) {
            return selectedTranslation.stores()
                    .mapToInt { validateKey(translationManager, it, newCellValue, singleton(selectedTranslation.key())) }
                    .max().orElse(OK)
        }
        if (columnIndex == DEFAULT_LANGUAGE_COLUMN_INDEX) {
            return validateDefaultText(newCellValue, selectedTranslation)
        }
        val selectedLanguage = languageForColumn(columnIndex)
        return validateText(newCellValue, selectedTranslation, selectedLanguage)
    }

    private inner class ManagerListener : ITranslationManagerListener, EventListenerList.IWeakEventListener {

        override fun managerChanged(events: Stream<TranslationManagerEvent>) {
            val allEvents = events.toList()
            val application = ApplicationManager.getApplication()
            if (application.isDispatchThread) {
                handleEvents(allEvents)
            } else {
                // Run in EDT. This is necessary e.g. for reload events which might come from a worker thread.
                // Do not wait here for the events to be handled (deadlock)
                application.invokeLater {
                    handleEvents(allEvents)
                }
            }
        }

        private fun handleEvents(events: List<TranslationManagerEvent>) {
            val doFullReload = events.size > 100 || events.any { it.type() == TranslationManagerEvent.TYPE_RELOAD }
            val needsSave = events.any {
                it.type() == TranslationManagerEvent.TYPE_REMOVE_TRANSLATION
                        || it.type() == TranslationManagerEvent.TYPE_NEW_TRANSLATION
                        || it.type() == TranslationManagerEvent.TYPE_KEY_CHANGED
                        || it.type() == TranslationManagerEvent.TYPE_UPDATE_TRANSLATION
                        || it.type() == TranslationManagerEvent.TYPE_NEW_LANGUAGE
            }

            val affectedRowIndices = updateViewModel(doFullReload, events)

            if (doFullReload || needsSave) {
                fireDataChangedListeners(affectedRowIndices)
            }

            if (needsSave) {
                SdkLog.debug("About to save translation manager.")
                saveManager()
            }
        }

        private fun saveManager() = callInIdeaEnvironment(project, EclipseScoutBundle.message("saving.translations")) { env, progress ->
            translationManager.flush(env, progress)
        }

        /**
         * Updates the view model with the events given
         * @return The affected model row indices
         */
        private fun updateViewModel(doFullReload: Boolean, events: List<TranslationManagerEvent>): List<Int> {
            if (doFullReload) {
                buildCache(true)
                return emptyList() // full reload. No single rows affected
            }
            return events.stream()
                    .mapToInt { updateViewModel(it) }
                    .filter { it >= 0 }
                    .distinct()
                    .toList()
        }

        private fun updateViewModel(event: TranslationManagerEvent) = when (event.type()) {
            TranslationManagerEvent.TYPE_REMOVE_TRANSLATION -> translationsRemoved(event)
            TranslationManagerEvent.TYPE_NEW_TRANSLATION -> translationsAdded(event)
            TranslationManagerEvent.TYPE_KEY_CHANGED -> translationKeyChanged(event)
            TranslationManagerEvent.TYPE_UPDATE_TRANSLATION -> translationsUpdated(event)
            TranslationManagerEvent.TYPE_NEW_LANGUAGE -> {
                buildCache()
                -1
            }
            else -> -1
        }

        private fun translationKeyChanged(event: TranslationManagerEvent): Int {
            val index = rowForTranslationWithKey(event.key().orElse(null))
            if (index < 0) {
                return -1
            }
            translations()[index] = event.translation().orElseThrow()
            fireTableRowsUpdated(index, index)
            return index
        }

        private fun translationsUpdated(event: TranslationManagerEvent) = event.translation()
                .map { rowForTranslationWithKey(it.key()) }
                .filter { it >= 0 }
                .map {
                    fireTableRowsUpdated(it, it)
                    return@map it
                }.orElse(-1)

        private fun translationsAdded(event: TranslationManagerEvent) = event.translation().map {
            val translations = translations()
            translations.add(it)
            val index = translations.size - 1
            fireTableRowsInserted(index, index)
            return@map index
        }.orElse(-1)

        private fun translationsRemoved(event: TranslationManagerEvent) = event.translation().map {
            val index = rowForTranslationWithKey(it.key())
            if (index < 0) {
                return@map -1
            }
            translations().removeAt(index)
            fireTableRowsDeleted(index, index)
            return@map index
        }.orElse(-1)
    }
}