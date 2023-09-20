/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.nls.editor

import com.intellij.find.impl.RegExHelpPopup
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.BalloonImpl
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.nls.ITranslationImportInfo
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.OperationTask
import org.eclipse.scout.sdk.s2i.nls.TranslationManagerLoader
import org.eclipse.scout.sdk.s2i.resolveLocalPath
import org.eclipse.scout.sdk.s2i.resolveProperty
import org.eclipse.scout.sdk.s2i.resolvePsi
import org.eclipse.scout.sdk.s2i.ui.IndexedFocusTraversalPolicy
import org.eclipse.scout.sdk.s2i.ui.TextFieldWithMaxLen
import org.eclipse.scout.sdk.s2i.util.Xlsx
import java.awt.Desktop
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Point
import java.io.File
import java.text.NumberFormat
import java.util.concurrent.TimeUnit
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.stream.Collectors.toList
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.event.DocumentEvent

class NlsEditorContent(val project: Project, val translationManager: TranslationManager, val primaryStore: ITranslationStore) : JBPanel<NlsEditorContent>(GridBagLayout()) {

    private val m_table = NlsTable(translationManager, project)
    private val m_textFilter = TextFieldWithMaxLen(maxLength = 2000)
    private val m_regexHelpButton = RegExHelpPopup.createRegExLink("<html><body><b>?</b></body></html>", this)
    private val m_hideInherited = JBCheckBox(message("hide.inherited.rows"), true)
    private val m_hideRowsWithOnlyReadOnly = JBCheckBox(message("hide.dependency.translations"), true)
    private val m_hideLanguagesWithOnlyReadOnly = JBCheckBox(message("hide.dependency.languages"), true)

    private var m_searchPattern: Predicate<String>? = null

    init {
        val typeFilterLayout = GridBagConstraints()
        typeFilterLayout.gridx = 0
        typeFilterLayout.gridy = 0
        typeFilterLayout.gridwidth = 1
        typeFilterLayout.gridheight = 1
        typeFilterLayout.fill = GridBagConstraints.HORIZONTAL
        typeFilterLayout.insets = JBUI.insets(15, 7, 0, 0)
        add(TranslationFilterPanel(), typeFilterLayout)

        val tableLayout = GridBagConstraints()
        tableLayout.gridx = 0
        tableLayout.gridy = 1
        tableLayout.gridwidth = 1
        tableLayout.gridheight = 1
        tableLayout.fill = GridBagConstraints.BOTH
        tableLayout.insets = JBUI.insets(8, 8, 0, 0)
        tableLayout.weightx = 1.0
        tableLayout.weighty = 1.0
        m_table.contextMenu = createContextMenu()
        add(m_table, tableLayout)

        val actionsLayout = GridBagConstraints()
        actionsLayout.gridx = 1
        actionsLayout.gridy = 1
        actionsLayout.gridwidth = 1
        actionsLayout.gridheight = 1
        actionsLayout.fill = GridBagConstraints.VERTICAL
        actionsLayout.insets = JBUI.insetsTop(8)
        val toolbar = createToolbar()
        add(toolbar, actionsLayout)

        isFocusTraversalPolicyProvider = true
        isFocusCycleRoot = true
        val focusPolicy = IndexedFocusTraversalPolicy()
        focusPolicy.addComponent(m_textFilter)
        focusPolicy.addComponent(m_regexHelpButton)
        focusPolicy.addComponent(m_hideInherited)
        focusPolicy.addComponent(m_hideRowsWithOnlyReadOnly)
        focusPolicy.addComponent(m_hideLanguagesWithOnlyReadOnly)
        focusTraversalPolicy = focusPolicy

        filterChanged()
    }

    fun textFilterField() = m_textFilter

    private fun filterChanged() {
        m_searchPattern = toPredicate(m_textFilter.text)
        m_table.setFilter(this::acceptTranslation, this::acceptLanguage)
    }

    private fun acceptLanguage(candidate: Language): Boolean {
        val isHideReadOnlyLanguages = m_hideLanguagesWithOnlyReadOnly.isSelected
        return !isHideReadOnlyLanguages || translationManager.allEditableStores().anyMatch { it.containsLanguage(candidate) }
    }

    private fun acceptTranslation(candidate: IStackedTranslation): Boolean {
        val isHideReadOnlyRows = m_hideRowsWithOnlyReadOnly.isSelected
        if (isHideReadOnlyRows && !candidate.hasEditableStores()) {
            return false
        }

        val isHideInheritedRows = m_hideInherited.isSelected
        if (isHideInheritedRows && !candidate.stores().anyMatch { it == primaryStore }) {
            return false
        }

        val textFilter = m_searchPattern ?: return true
        if (textFilter.test(candidate.key())) {
            return true
        }
        return candidate.texts().values.any { textFilter.test(it) }
    }

    private fun toPredicate(searchText: String): Predicate<String>? {
        if (Strings.isBlank(searchText)) {
            return null
        }

        return try {
            Pattern.compile(searchText, Pattern.CASE_INSENSITIVE)
        } catch (e: Exception) {
            Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE)
        }.asPredicate()
    }

    private fun createContextMenu() = ActionManager.getInstance()
        .createActionPopupMenu("ScoutNlsEditorContextMenu", createContextMenuActionGroup())
        .component

    private fun createContextMenuActionGroup(): DefaultActionGroup {
        val group = DefaultActionGroup()
        group.add(TranslationEditAction(true))
        group.addSeparator()
        group.add(TranslationLocateActionGroup(true))
        group.addSeparator()
        group.add(TranslationRemoveAction(true))
        return group
    }

    private fun createToolbar(): JComponent {
        val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, createToolbarActionGroup(), false)
        toolbar.targetComponent = this
        return toolbar.component
    }

    private fun createToolbarActionGroup(): ActionGroup {
        val result = DefaultActionGroup()
        result.add(TranslationEditAction())
        result.add(TranslationNewAction())
        result.addSeparator()
        result.add(TranslationRemoveAction())
        if (translationManager.allEditableStores().count() > 1) {
            result.add(TranslationNewActionGroup())
        }
        result.addSeparator()
        result.add(TranslationLocateActionGroup())
        result.add(ReloadAction())
        result.addSeparator()
        result.add(LanguageNewAction())
        result.addSeparator()
        result.add(ImportFromExcelAction())
        result.add(ExportToExcelAction())
        return result
    }

    private fun showBalloon(text: String, severity: MessageType) {
        val lbl = JBLabel(text)
        val balloon = JBPopupFactory.getInstance()
            .createBalloonBuilder(lbl)
            .setShowCallout(false)
            .setAnimationCycle(Registry.intValue("ide.tooltip.animationCycle"))
            .setBlockClicksThroughBalloon(true)
            .setFillColor(severity.popupBackground)
            .setBorderColor(severity.borderColor)
            .createBalloon()
        if (balloon is BalloonImpl) {
            balloon.startSmartFadeoutTimer(TimeUnit.SECONDS.toMillis(30).toInt())
        }
        balloon.show(RelativePoint(m_table, Point(m_table.visibleRect.width / 2, 0)), Balloon.Position.above)
    }

    private inner class TranslationEditAction(val hideWhenDisabled: Boolean = false) : DumbAwareAction(message("edit.translation"), null, AllIcons.Actions.Edit) {
        override fun update(e: AnActionEvent) {
            val selectedTranslations = m_table.selectedTranslations()
            e.presentation.isEnabled = selectedTranslations.size == 1
                    && selectedTranslations[0].hasEditableStores()
            e.presentation.isVisible = !hideWhenDisabled || e.presentation.isEnabled
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun actionPerformed(e: AnActionEvent) {
            val translation = m_table.selectedTranslations().firstOrNull() ?: return
            val language = m_table.selectedLanguages().firstOrNull()
            val dialog = TranslationEditDialog(project, translation, translationManager, language)
            dialog.showAndGet()
        }
    }

    private inner class TranslationNewAction : DumbAwareAction(message("create.new.translation.in.x", primaryStore.service().type().elementName()), null, AllIcons.General.Add) {
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = primaryStore.isEditable
        }

        override fun getActionUpdateThread() = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) = TranslationNewDialogOpenAction(primaryStore).actionPerformed(e)
    }

    private inner class TranslationNewActionGroup : AbstractStoresAction(message("create.new.translation.in.service"), message("create.new.translation.in"),
        AllIcons.CodeStyle.AddNewSectionRule, translationManager.allEditableStores().collect(toList()), { TranslationNewDialogOpenAction(it) })

    private inner class TranslationNewDialogOpenAction(private val m_store: ITranslationStore) : DumbAwareAction(m_store.service().type().elementName()) {
        override fun actionPerformed(e: AnActionEvent) {
            val dialog = TranslationNewDialog(project, m_store, translationManager)
            val ok = dialog.showAndGet()
            if (ok) {
                val createdTranslation = dialog.createdTranslation() ?: return
                m_table.selectTranslation(createdTranslation)
            }
        }
    }

    private inner class TranslationRemoveAction(val hideWhenDisabled: Boolean = false) : DumbAwareAction(message("remove.selected.rows"), null, AllIcons.General.Remove) {
        override fun update(e: AnActionEvent) {
            val selectedTranslations = m_table.selectedTranslations()
            e.presentation.isEnabled = selectedTranslations.isNotEmpty() && selectedTranslations.all { it.hasOnlyEditableStores() }
            e.presentation.isVisible = !hideWhenDisabled || e.presentation.isEnabled
            if (selectedTranslations.size > 1) {
                e.presentation.text = message("remove.selected.rows")
            } else {
                e.presentation.text = message("remove.selected.row")
            }
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun actionPerformed(e: AnActionEvent) {
            val toDelete = m_table.selectedTranslations()
                .map { it.key() }
                .stream()
            translationManager.removeTranslations(toDelete)
        }
    }

    private inner class TranslationLocateActionGroup(val hideWhenDisabled: Boolean = false) : DumbAwareAction(message("jump.to.declaration"), null, AllIcons.General.Locate) {
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = m_table.selectedTranslations().size == 1
            e.presentation.isVisible = !hideWhenDisabled || e.presentation.isEnabled
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun actionPerformed(e: AnActionEvent) {
            val selection = m_table.selectedTranslations()
            if (selection.size != 1) {
                return
            }
            val selectedTranslation = selection[0]
            val selectedLanguages = m_table.selectedLanguages()
            var stores = selectedLanguages
                .mapNotNull { selectedTranslation.entry(it).orElse(null) }
                .map { it.store() }
                .toList()
            if (stores.isEmpty()) {
                stores = selectedTranslation.stores().collect(toList())
            }

            val servicesLocateAction = if (stores.size == 1) TranslationServiceLocateAction(stores[0]) else TranslationServicesLocateAction(stores)
            if (selectedLanguages.size != 1 || !selectedTranslation.text(selectedLanguages[0]).isPresent) {
                servicesLocateAction.actionPerformed(e)
                return
            }

            val textLocateAction = TranslationTextLocateAction(selectedTranslation, selectedLanguages[0])
            val group = DefaultActionGroup(listOf(servicesLocateAction, textLocateAction))
            val popup = JBPopupFactory.getInstance().createActionGroupPopup(templatePresentation.text, group, e.dataContext, JBPopupFactory.ActionSelectionAid.NUMBERING, false)
            e.inputEvent?.component?.let { popup.showUnderneathOf(it) }
        }
    }

    private inner class TranslationTextLocateAction(val translation: IStackedTranslation, val language: Language) : DumbAwareAction(message("jump.to.property"), null, AllIcons.Nodes.ResourceBundle) {
        override fun actionPerformed(e: AnActionEvent) {
            translation.resolveProperty(language, project)?.navigate(true)
        }
    }

    private inner class TranslationServicesLocateAction(stores: List<ITranslationStore>) : AbstractStoresAction(message("jump.to.text.service"), message("choose.text.service"),
        AllIcons.Nodes.Services, stores, { TranslationServiceLocateAction(it) })

    private inner class TranslationServiceLocateAction(private val m_store: ITranslationStore) : DumbAwareAction(m_store.service().type().elementName(), null, AllIcons.Nodes.Services) {
        override fun actionPerformed(e: AnActionEvent) {
            m_store.service().type().resolvePsi()?.navigate(true)
        }
    }

    private inner class LanguageNewAction : DumbAwareAction(message("add.new.language"), null, AllIcons.ToolbarDecorator.AddLink) {
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = primaryStore.isEditable
        }

        override fun getActionUpdateThread() = ActionUpdateThread.BGT

        override fun actionPerformed(e: AnActionEvent) = LanguageNewDialog(project, primaryStore, translationManager).show()
    }

    private inner class ReloadAction : DumbAwareAction(message("reload.from.filesystem"), null, AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            FileDocumentManager.getInstance().saveAllDocuments()
            TranslationManagerLoader.scheduleManagerReload(project, translationManager)
        }
    }

    private inner class ImportFromExcelAction : DumbAwareAction(message("import.translations.from.excel"), null, AllIcons.ToolbarDecorator.Import) {

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = primaryStore.isEditable
        }

        override fun getActionUpdateThread() = ActionUpdateThread.BGT


        override fun actionPerformed(e: AnActionEvent) {
            val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
                .withDescription(message("please.choose.the.xlsx.file.to.import"))
                .withTitle(message("import.translations.from.excel"))
                .withFileFilter { it.isValid && it.exists() && it.extension == "xlsx" }
            val vFile = FileChooser.chooseFile(descriptor, project, null) ?: return
            try {
                val file = vFile.resolveLocalPath()?.toFile()
                if (file?.exists() != true) {
                    showBalloon(message("file.not.found"), MessageType.ERROR)
                    return
                }
                val data = Xlsx.parse(file)
                if (data.isEmpty()) {
                    showBalloon(message("file.no.valid.content"), MessageType.ERROR)
                    return
                }
                handleResult(translationManager.importTranslations(data, NlsTableModel.KEY_COLUMN_HEADER_NAME, primaryStore))
            } catch (e: RuntimeException) {
                SdkLog.error("Unable to import xlsx file '{}'.", vFile, e)
                showBalloon(message("error.importing.translations"), MessageType.ERROR)
            }
        }

        private fun handleResult(importInfo: ITranslationImportInfo) {
            val result = importInfo.result()
            if (result < 1) {
                showBalloon(message("file.content.no.mapping"), MessageType.ERROR)
                return
            }
            val listSeparator = ", "
            val listPrefix = "["
            val listPostfix = "]"
            val balloonMessages = ArrayList<String>()
            val logMessages = ArrayList<String>()
            val duplicateKeys = importInfo.duplicateKeys()
            val maxNumItemsInBalloon = 3
            balloonMessages.add(message("import.successful.x.rows", NumberFormat.getInstance().format(result)))
            if (duplicateKeys.isNotEmpty()) {
                val balloonList = duplicateKeys.joinToString(listSeparator, listPrefix, listPostfix, maxNumItemsInBalloon)
                balloonMessages.add(message("import.duplicate.keys", balloonList))
                logMessages.add(message("import.duplicate.keys", duplicateKeys))
            }
            val ignoredColumns = importInfo.ignoredColumns()
            if (ignoredColumns.isNotEmpty()) {
                val messages = ignoredColumns.entries.map { message("column.x", it.key + 1) + if (Strings.hasText(it.value)) "=" + it.value else "" }
                val balloonList = messages.joinToString(listSeparator, listPrefix, listPostfix, maxNumItemsInBalloon)
                balloonMessages.add(message("import.columns.not.mapped", balloonList))
                logMessages.add(message("import.columns.not.mapped", messages))
            }
            val invalidRows = importInfo.invalidRowIndices()
                .map { it + 1 } // convert to row number
                .toList()
            if (invalidRows.isNotEmpty()) {
                val balloonList = invalidRows.joinToString(listSeparator, listPrefix, listPostfix, maxNumItemsInBalloon)
                balloonMessages.add(message("import.rows.invalid", balloonList))
                logMessages.add(message("import.rows.invalid", invalidRows))
            }

            val balloonMessage = balloonMessages.joinToString("<br>", "<html>", "</html>", transform = Strings::escapeHtml)
            val hasWarnings = logMessages.isNotEmpty()
            if (hasWarnings) {
                showBalloon(balloonMessage, MessageType.WARNING)
                logMessages.forEach { SdkLog.warning(it) }
            } else {
                showBalloon(balloonMessage, MessageType.INFO)
            }
        }
    }

    private inner class ExportToExcelAction : DumbAwareAction(message("export.table.to.excel"), null, AllIcons.ToolbarDecorator.Export) {

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = m_table.visibleRowCount() > 0
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun actionPerformed(e: AnActionEvent) {
            val fileSaverDescriptor = FileSaverDescriptor(message("export.translations"), message("export.translations.desc"), "xlsx")
            val file = FileChooserFactory.getInstance().createSaveFileDialog(fileSaverDescriptor, project)
                .save(null as VirtualFile? /* cast required for IJ 2020.3 compatibility (overloads) */, null)
                ?.file ?: return
            val tableData = m_table.visibleData()
            OperationTask(message("export.translations"), project) { doExport(tableData, file) }.schedule()
        }

        private fun doExport(tableData: List<List<String>>, file: File) {
            try {
                Xlsx.write(tableData, message("nls.export.sheet.name"), file)
                showBalloon(message("table.data.successfully.exported"), MessageType.INFO)
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file)
            } catch (e: Exception) {
                SdkLog.warning("Unable to export to xlsx file '{}'.", file, e)
                showBalloon(message("error.exporting.translations"), MessageType.ERROR)
            }
        }
    }

    private abstract class AbstractStoresAction(text: String, val groupTitle: String, icon: Icon?, val stores: List<ITranslationStore>, val task: (ITranslationStore) -> AnAction) : DumbAwareAction(text, null, icon) {
        override fun actionPerformed(e: AnActionEvent) {
            if (stores.isEmpty()) return

            val storesWithoutDuplicates = stores.distinct()
            if (storesWithoutDuplicates.size == 1) {
                task(storesWithoutDuplicates[0]).actionPerformed(e)
            } else {
                val popupActions = storesWithoutDuplicates.map { task(it) }
                val group = DefaultActionGroup(popupActions)
                val popup = JBPopupFactory.getInstance().createActionGroupPopup(groupTitle, group, e.dataContext, JBPopupFactory.ActionSelectionAid.NUMBERING, false)
                e.inputEvent?.component?.let { popup.showUnderneathOf(it) }
            }
        }
    }

    private inner class TranslationFilterPanel : JBPanel<TranslationFilterPanel>(GridBagLayout()) {
        init {
            val filterLayout = GridBagConstraints()
            filterLayout.gridx = 0
            filterLayout.gridy = 0
            filterLayout.fill = GridBagConstraints.HORIZONTAL
            filterLayout.weightx = 1.0
            filterLayout.insets = JBUI.emptyInsets()
            m_textFilter.document.addDocumentListener(object : DocumentAdapter() {
                override fun textChanged(e: DocumentEvent) {
                    filterChanged()
                }
            })
            m_textFilter.isFocusable = true
            add(m_textFilter, filterLayout)

            val regexHelpLayout = GridBagConstraints()
            regexHelpLayout.gridx = 1
            regexHelpLayout.gridy = 0
            regexHelpLayout.insets = JBUI.insetsLeft(4)
            m_regexHelpButton.isFocusable = true
            add(m_regexHelpButton, regexHelpLayout)

            val inheritedLayout = GridBagConstraints()
            inheritedLayout.gridx = 2
            inheritedLayout.gridy = 0
            inheritedLayout.insets = JBUI.insetsLeft(16)
            m_hideInherited.addActionListener { filterChanged() }
            m_hideInherited.toolTipText = message("hide.inherited.rows.desc", primaryStore.service().type().name())
            m_hideInherited.isFocusable = true
            add(m_hideInherited, inheritedLayout)

            val readOnlyRowsLayout = GridBagConstraints()
            readOnlyRowsLayout.gridx = 3
            readOnlyRowsLayout.gridy = 0
            readOnlyRowsLayout.insets = JBUI.insetsLeft(12)
            m_hideRowsWithOnlyReadOnly.addActionListener { filterChanged() }
            m_hideRowsWithOnlyReadOnly.toolTipText = message("hide.dependency.translations.desc")
            m_hideRowsWithOnlyReadOnly.isFocusable = true
            add(m_hideRowsWithOnlyReadOnly, readOnlyRowsLayout)

            val readOnlyLanguagesLayout = GridBagConstraints()
            readOnlyLanguagesLayout.gridx = 4
            readOnlyLanguagesLayout.gridy = 0
            readOnlyLanguagesLayout.insets = JBUI.insets(0, 12, 0, 4)
            m_hideLanguagesWithOnlyReadOnly.addActionListener { filterChanged() }
            m_hideLanguagesWithOnlyReadOnly.toolTipText = message("hide.dependency.languages.desc")
            m_hideLanguagesWithOnlyReadOnly.isFocusable = true
            add(m_hideLanguagesWithOnlyReadOnly, readOnlyLanguagesLayout)
        }
    }
}