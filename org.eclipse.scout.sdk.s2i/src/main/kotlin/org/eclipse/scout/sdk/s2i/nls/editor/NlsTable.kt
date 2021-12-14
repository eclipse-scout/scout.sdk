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

import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.ActionButtonLook
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.keymap.KeymapUtil.getKeystrokeText
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.openapi.util.registry.Registry
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.PopupHandler
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.PositionTracker
import org.eclipse.scout.sdk.core.s.nls.Language
import org.eclipse.scout.sdk.core.s.nls.TranslationValidator.*
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.nls.editor.NlsTableModel.Companion.KEY_COLUMN_INDEX
import org.eclipse.scout.sdk.s2i.nls.editor.NlsTableModel.Companion.NUM_ADDITIONAL_COLUMNS
import org.eclipse.scout.sdk.s2i.ui.TablePreservingSelection
import org.eclipse.scout.sdk.s2i.ui.TextAreaWithContentSize
import java.awt.*
import java.awt.event.*
import java.util.*
import javax.swing.*
import javax.swing.KeyStroke.getKeyStroke
import javax.swing.event.DocumentEvent
import javax.swing.plaf.UIResource
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableRowSorter
import javax.swing.text.DefaultEditorKit

class NlsTable(manager: TranslationManager, project: Project) : JBScrollPane() {

    private val m_model: NlsTableModel = NlsTableModel(manager, project)
    private val m_table: TablePreservingSelection = TablePreservingSelection(m_model,
            { index -> m_model.translationForRow(index) },
            { row -> m_model.rowForTranslation(row as IStackedTranslation) })
    private val m_tableSorterFilter = TableRowSorter(m_model)
    private val m_cellMargin = Insets(1, 4, 2, 2)
    private val m_editStartEvent = EventObject(this)
    private val m_fontHeight = FinalValue<Int>()

    private var m_balloon: Balloon? = null
    private var m_balloonContent: JBLabel? = null
    var contextMenu: JPopupMenu? = null

    init {
        m_model.addDataChangedListener {
            // re-apply filter as the filtered rows might have changed
            // this must be executed before computing the new row heights as rows might become visible here
            m_tableSorterFilter.sort()
            adjustRowHeights(it)
        }

        m_table.tableColumnsChangedCallback = { adjustView() }
        m_table.columnWidthSupplier = { if (it.modelIndex == KEY_COLUMN_INDEX) 250 else 350 }
        m_table.fillsViewportHeight = true
        m_table.autoResizeMode = JTable.AUTO_RESIZE_OFF
        m_table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
        m_table.rowSelectionAllowed = true
        m_table.columnSelectionAllowed = false
        m_table.cellSelectionEnabled = true
        m_table.setEnableAntialiasing(true)
        m_table.putClientProperty("terminateEditOnFocusLost", true)
        m_table.setShowGrid(true)
        m_table.gridColor = JBColor.border()
        m_table.tableHeader.reorderingAllowed = false
        m_table.addKeyListener(TableKeyListener())
        m_table.addMouseListener(object : PopupHandler() {
            override fun invokePopup(comp: Component?, x: Int, y: Int) {
                val selectedTranslations = selectedTranslations()
                if (selectedTranslations.size > 1) {
                    val hasReadOnlyRows = selectedTranslations.any { !it.hasEditableStores() }
                    // ActionPopupMenuImpl show a 'Nothing here' menu if all menu items are invisible
                    // Currently this is the case if it is a multi-selection and includes read-only stores
                    // in that case don't show the popup
                    if (hasReadOnlyRows) {
                        return
                    }
                }
                contextMenu?.show(comp, x, y)
            }
        })
        m_table.addPropertyChangeListener {
            // reset row height on cell editor cancel
            if ("tableCellEditor" == it.propertyName && it.newValue == null && it.oldValue != null) {
                adjustEditingRowHeight()
                hideBalloon()
            }
        }
        m_table.rowSorter = m_tableSorterFilter
        m_tableSorterFilter.sortKeys = listOf(RowSorter.SortKey(0, SortOrder.ASCENDING), RowSorter.SortKey(1, SortOrder.ASCENDING))
        border = null

        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS)
        setViewportView(m_table)
    }

    private fun adjustView() {
        val columnModel = m_table.columnModel
        for (i in 0 until columnModel.columnCount) {
            val column = columnModel.getColumn(i)
            if (column.cellRenderer !is MultiLineTextCellRenderer) {
                column.cellRenderer = MultiLineTextCellRenderer()
                column.cellEditor = MultiLineTextCellEditor(i != KEY_COLUMN_INDEX)
            }
        }
        adjustRowHeights()
    }

    private fun adjustRowHeights(modelIndices: Collection<Int>) {
        val fontHeight = fontHeight()
        modelIndices.forEach {
            val viewIndex = m_table.convertRowIndexToView(it)
            adjustRowHeight(viewIndex, fontHeight, null)
        }
    }

    private fun adjustRowHeights() {
        val fontHeight = fontHeight()
        for (i in 0 until m_model.rowCount) {
            val viewIndex = m_table.convertRowIndexToView(i)
            adjustRowHeight(viewIndex, fontHeight)
        }
    }

    private fun fontHeight() = m_fontHeight.computeIfAbsentAndGet {
        getFontMetrics(m_table.font).height
    }

    private fun adjustRowHeight(rowIndex: Int, fontHeight: Int, additionalText: String? = null): Int {
        if (rowIndex < 0 || rowIndex >= m_table.rowCount) return -1
        val rowsRequired = maxLinesForRow(rowIndex, additionalText)
        val height = (rowsRequired * fontHeight) + 6
        m_table.setRowHeight(rowIndex, height)
        return rowsRequired
    }

    private fun adjustEditingRowHeight(additionalText: String? = null): Int {
        val editingRowIndexView = m_table.editingRow
        return adjustRowHeight(editingRowIndexView, fontHeight(), additionalText)
    }

    private fun maxLinesForRow(rowIndex: Int, additionalText: String? = null): Int {
        val entry = m_model.translationForRow(m_table.convertRowIndexToModel(rowIndex))
        val add = additionalText ?: ""
        return (sequenceOf(add) + entry.texts().values.asSequence())
                .map { Strings.countMatches(it, "\n") + 1 }
                .maxOrNull() ?: 1
    }

    fun selectedLanguages() = m_table.selectedColumns
            .filter { it >= NUM_ADDITIONAL_COLUMNS }
            .map { m_table.convertColumnIndexToModel(it) }
            .map { m_model.languageForColumn(it) }

    fun selectedTranslations() = m_table.selectedRows
            .map { m_table.convertRowIndexToModel(it) }
            .map { m_model.translationForRow(it) }

    fun visibleRowCount() = m_table.rowCount

    fun visibleData(): List<List<String>> {
        val numAdditionalRows = 1 // header row
        val data = ArrayList<List<String>>(m_table.rowCount + numAdditionalRows)

        // header
        val headerRow = ArrayList<String>(m_table.columnCount)
        headerRow.add(NlsTableModel.KEY_COLUMN_HEADER_NAME)
        for (lang in m_model.languages()) {
            headerRow.add(lang.locale().toString())
        }
        data.add(headerRow)

        // data
        for (row in 0 until m_table.rowCount) {
            val dataRow = ArrayList<String>(m_table.columnCount)
            for (col in 0 until m_table.columnCount) {
                dataRow.add(m_table.getValueAt(row, col).toString())
            }
            data.add(dataRow)
        }
        return data
    }

    fun selectTranslation(translation: IStackedTranslation) {
        val modelRow = m_model.rowForTranslation(translation)
        if (modelRow < 0) {
            return
        }

        val row = m_table.convertRowIndexToView(modelRow)
        if (row < 0) {
            return
        }
        m_table.setRowSelectionInterval(row, row)
        m_table.scrollToSelection()
    }

    fun setFilter(rowFilter: ((IStackedTranslation) -> Boolean)?, columnFilter: ((Language) -> Boolean)?) {
        val filter = object : RowFilter<NlsTableModel, Int>() {
            override fun include(entry: Entry<out NlsTableModel, out Int>): Boolean {
                return rowFilter?.invoke(m_model.translationForRow(entry.identifier)) ?: true
            }
        }
        m_tableSorterFilter.rowFilter = filter
        val tableStructureChanged = m_model.setFilter(rowFilter, columnFilter)
        if (!tableStructureChanged) {
            adjustView() // only if the structure did not change. Because on structure change it is done anyway
        }
        m_table.scrollToSelection()
    }

    private fun showBalloon(message: String, owner: Component, severity: MessageType) {
        val existingBalloonContent = m_balloonContent
        if (existingBalloonContent != null) {
            existingBalloonContent.text = message
            m_balloon?.revalidate() // adapt balloon size to new text
            return
        }

        val lbl = JBLabel(message)
        lbl.putClientProperty("html.disable", true)
        val builder = JBPopupFactory.getInstance().createBalloonBuilder(lbl)
        val balloon = builder
                .setFillColor(severity.popupBackground)
                .setBorderColor(severity.borderColor)
                .setHideOnAction(false)
                .setHideOnKeyOutside(false)
                .setAnimationCycle(Registry.intValue("ide.tooltip.animationCycle"))
                .setBlockClicksThroughBalloon(true)
                .createBalloon()
        balloon.show(object : PositionTracker<Balloon>(owner) {
            override fun recalculateLocation(element: Balloon): RelativePoint {
                return RelativePoint(owner, Point((owner.size.width * 0.75).toInt(), -4))
            }
        }, Balloon.Position.above)
        balloon.addListener(object : JBPopupListener {
            override fun onClosed(event: LightweightWindowEvent) {
                hideBalloon()
            }
        })
        m_balloon = balloon
        m_balloonContent = lbl
    }

    private fun hideBalloon() {
        m_balloon?.hide()
        m_balloon = null
        m_balloonContent = null
    }

    private inner class TableKeyListener : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            if (e.keyCode == KeyEvent.VK_F2 || e.keyCode == KeyEvent.VK_ENTER) {
                val editStarted = m_table.editCellAt(m_table.selectedRow, m_table.selectedColumn, m_editStartEvent)
                if (editStarted) {
                    e.consume()
                    val cellEditor = m_table.cellEditor
                    if (cellEditor is MultiLineTextCellEditor) {
                        cellEditor.focus()
                    }
                }
            }
        }
    }

    private inner class MultiLineTextCellRenderer : TableCellRenderer {
        override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            val text = value.toString()
            val txt = TextAreaWithContentSize(m_table.font, text)
            txt.margin = m_cellMargin

            if (isSelected) {
                txt.foreground = table.selectionForeground
                txt.background = table.selectionBackground
            } else {
                var background = table.background
                if (background == null || background is UIResource) {
                    val alternateColor = UIManager.getColor("Table.alternateRowColor")
                    if (alternateColor != null && row % 2 != 0) {
                        background = alternateColor
                    }
                }
                val translationForRow = m_model.translationForRow(m_table.convertRowIndexToModel(row))
                val isCellEditable = if (column == KEY_COLUMN_INDEX) translationForRow.hasOnlyEditableStores() else translationForRow.hasEditableStores()
                if (isCellEditable) {
                    txt.foreground = table.foreground
                } else {
                    txt.foreground = UIManager.getColor("Button.disabledText")
                }
                txt.background = background
            }
            return txt
        }
    }

    private inner class NewLineAction(private val txt: JBTextArea) : DumbAwareAction(
            null, message(
            "insert.new.line.x",
            getKeystrokeText(getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK))
    ), AllIcons.Actions.SearchNewLine
    ) {
        init {
            templatePresentation.hoveredIcon = AllIcons.Actions.SearchNewLineHover
        }

        override fun actionPerformed(e: AnActionEvent) {
            DefaultEditorKit.InsertBreakAction().actionPerformed(ActionEvent(txt, 0, "action"))
        }
    }

    private inner class MultiLineTextCellEditor(val supportMultiLine: Boolean) : AbstractCellEditor(), TableCellEditor {

        private val m_cellContentPanel = JBPanel<JBPanel<*>>()
        private val m_txt = TextAreaWithContentSize(m_table.font)
        private val m_scrollPane = JBScrollPane(m_txt, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)

        init {
            val borderWidth = 1
            m_cellContentPanel.border = BorderFactory.createLineBorder(JBColor.border(), borderWidth)
            m_cellContentPanel.background = m_txt.background
            m_scrollPane.border = null
            m_txt.margin = Insets(m_cellMargin.top - borderWidth, m_cellMargin.left - borderWidth, m_cellMargin.bottom, m_cellMargin.right)
            m_txt.document.addDocumentListener(object : DocumentAdapter() {
                override fun textChanged(e: DocumentEvent) {
                    onTextChanged()
                }
            })
            m_txt.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    onKeyPressed(e)
                }
            })
            if (!supportMultiLine) {
                m_txt.document.putProperty("filterNewlines", true)
            }

            m_cellContentPanel.layout = GridBagLayout()
            m_cellContentPanel.add(
                    m_scrollPane, GridBagConstraints(
                    0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.FIRST_LINE_START,
                    GridBagConstraints.BOTH, Insets(0, 0, 0, 0), 0, 0
            )
            )

            if (supportMultiLine) {
                val newLineHelpButton = createButton(NewLineAction(m_txt))
                m_cellContentPanel.add(
                        newLineHelpButton, GridBagConstraints(
                        1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.FIRST_LINE_END,
                        GridBagConstraints.NONE, Insets(0, 0, 0, 0), 0, 0
                )
                )
            }
        }

        private fun onKeyPressed(e: KeyEvent) {
            // alt+enter: add new line
            if (supportMultiLine && e.keyCode == KeyEvent.VK_ENTER && (e.isAltDown || e.isAltGraphDown)) {
                m_txt.insert(System.lineSeparator(), m_txt.caretPosition)
                e.consume()
                return
            }

            // enter or tab: finish editing
            if (e.keyCode == KeyEvent.VK_ENTER || e.keyCode == KeyEvent.VK_TAB) {
                stopCellEditing()
                e.consume()
                if (e.keyCode == KeyEvent.VK_TAB) moveSelectionOneRightIfPossible()
            }
        }

        private fun moveSelectionOneRightIfPossible() {
            val selectedColumns = m_table.columnModel.selectionModel.selectedIndices
            if (selectedColumns.size == 1 && selectedColumns[0] < m_table.columnCount - 1) {
                val nextIndex = selectedColumns[0] + 1
                m_table.columnModel.selectionModel.setSelectionInterval(nextIndex, nextIndex)
            }
        }

        private fun onTextChanged() {
            if (supportMultiLine) {
                adjustEditingRowHeight(m_txt.text)
            }
            val editingRowViewIndex = m_table.editingRow
            val editingColumnViewIndex = m_table.editingColumn
            if (editingRowViewIndex >= 0 && editingColumnViewIndex >= 0) {
                validateEdit(m_txt.text, editingRowViewIndex, editingColumnViewIndex)
            }
        }

        fun focus() {
            m_txt.requestFocus()
        }

        fun validateEdit(aValue: Any?, rowIndex: Int, columnIndex: Int) {
            val table = m_table
            val validationResult = m_model.validate(aValue, table.convertRowIndexToModel(rowIndex), table.convertColumnIndexToModel(columnIndex))
            showValidationResult(validationResult)
        }

        private fun showValidationResult(result: Int) {
            val severity = if (OK == result) {
                MessageType.INFO
            } else if (!isForbidden(result)) {
                MessageType.WARNING
            } else {
                MessageType.ERROR
            }
            val borderColor = if (OK == result) JBColor.border() else severity.popupBackground
            val msg = when (result) {
                OK -> ""
                DEFAULT_TRANSLATION_MISSING_ERROR -> message("default.text.mandatory")
                KEY_EMPTY_ERROR -> message("please.specify.key")
                KEY_ALREADY_EXISTS_ERROR -> message("key.already.exists")
                KEY_OVERRIDES_OTHER_STORE_WARNING -> message("key.would.override")
                KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING -> message("key.would.be.overridden")
                KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING -> message("key.overrides.and.is.overridden")
                TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING -> message("text.replaced.with.inherited")
                else -> message("key.contains.invalid.chars")
            }

            if (Strings.isBlank(msg)) {
                hideBalloon()
            } else {
                showBalloon(msg, m_txt, severity)
            }
            m_cellContentPanel.border = BorderFactory.createLineBorder(borderColor)
        }

        override fun getTableCellEditorComponent(table: JTable, value: Any?, isSelected: Boolean, row: Int, column: Int): Component {
            m_txt.text = value.toString()
            validateEdit(value, row, column)
            return m_cellContentPanel
        }

        private fun createButton(action: AnAction): ActionButton {
            val presentation = action.templatePresentation
            val d = JBDimension(16, 16)
            val button = object : ActionButton(action, presentation, ActionPlaces.UNKNOWN, d) {
                override fun getDataContext(): DataContext {
                    return DataManager.getInstance().getDataContext(this)
                }
            }
            button.setLook(ActionButtonLook.INPLACE_LOOK)
            button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            button.updateIcon()
            return button
        }

        override fun isCellEditable(e: EventObject?): Boolean {
            // edit mode only on our own event from the key listener or on double click
            return e == m_editStartEvent || (e is MouseEvent && e.clickCount > 1)
        }

        override fun getCellEditorValue(): Any? = m_txt.text
    }
}