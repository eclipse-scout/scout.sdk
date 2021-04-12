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
package org.eclipse.scout.sdk.s2i.ui

import com.intellij.ui.table.JBTable
import java.util.*
import javax.swing.JViewport
import javax.swing.RowSorter
import javax.swing.SortOrder
import javax.swing.event.TableModelEvent
import javax.swing.table.TableColumn
import javax.swing.table.TableModel

/**
 * A [JBTable] which tries to preserve selection, column width and sort order when the table data or structure changes.
 */
open class TablePreservingSelection(model: TableModel, private val indexToRowMapper: (Int) -> Any, private val rowToIndexMapper: (Any) -> Int) : JBTable(model) {

    private var m_selectionListenerArmed = true
    private val m_selectedRows = ArrayList<Any>()
    var tableColumnsChangedCallback: ((TableModelEvent?) -> Unit)? = null
    var tableChangedCallback: ((TableModelEvent?) -> Unit)? = null
    var columnWidthSupplier: ((TableColumn) -> Int)? = null
        set(value) {
            if (value != null) {
                (0 until columnModel.columnCount).map { columnModel.getColumn(it) }.map { it.preferredWidth = value(it) }
            }
            field = value
        }

    init {
        selectionModel.addListSelectionListener { backupRowSelection() }
    }

    override fun tableChanged(e: TableModelEvent?) {
        @Suppress("SENSELESS_COMPARISON")
        if (m_selectedRows != null) { // may be null during constructor execution
            val isTableDataChanged = e?.lastRow == Int.MAX_VALUE
            val isTableStructureChanged = e == null || e.firstRow == TableModelEvent.HEADER_ROW
            if (isTableStructureChanged || isTableDataChanged) {
                runPreservingSelectionAndSorting {
                    super.tableChanged(e)
                    tableColumnsChangedCallback?.invoke(e)
                }
                return
            }
        }

        super.tableChanged(e)
        tableChangedCallback?.invoke(e)
    }

    private fun runPreservingSelectionAndSorting(runnable: () -> Unit) {
        val selectedHeaders = backupColumnSelection()
        val columnWidths = backupColumnWidths()
        val sorting = backupSortOrder()

        m_selectionListenerArmed = false
        try {
            runnable()
        } finally {
            m_selectionListenerArmed = true
        }

        // restore sorting before the selection otherwise the view-model-index map in the JTable is wrong
        restoreSortOrder(sorting)

        restoreColumnWidth(columnWidths)

        // compute new indices
        val newRowIndices = m_selectedRows
                .map { rowToIndexMapper(it) }
                .filter { it >= 0 }
                .map { convertRowIndexToView(it) }
        val newColIndices = selectedHeaders
                .mapNotNull { findColumnWithHeaderValue(it) }
                .map { it.modelIndex }
                .map { convertColumnIndexToView(it) }

        restoreSelection(newRowIndices, newColIndices)
        revealSelection(newRowIndices, newColIndices)
    }

    fun scrollToSelection() {
        revealSelection(selectedRows.toList(), selectedColumns.toList())
    }

    private fun backupSortOrder() = rowSorter.sortKeys
            .associate { columnModel.getColumn(it.column).headerValue to it.sortOrder }

    private fun backupColumnSelection() = selectedColumns
            .map { columnModel.getColumn(it).headerValue }

    private fun backupColumnWidths() = (0 until columnModel.columnCount)
        .map { columnModel.getColumn(it) }
        .associate { it.headerValue to it.preferredWidth }

    private fun restoreColumnWidth(columnWidths: Map<Any, Int>) =
            (0 until columnModel.columnCount)
                    .map { columnModel.getColumn(it) }
                    .map { col -> (columnWidths[col.headerValue] ?: columnWidthSupplier?.invoke(col))?.let { col.preferredWidth = it } }

    private fun revealSelection(rowIndices: List<Int>, colIndices: List<Int>) {
        if (rowIndices.isEmpty() || colIndices.isEmpty()) {
            return // nothing to reveal
        }

        val viewport = parent as JViewport
        val rect = getCellRect(rowIndices[0], colIndices[0], true)
        val viewRect = viewport.viewRect

        rect.setLocation(rect.x - viewRect.x, rect.y - viewRect.y)
        var centerY = (viewRect.height - rect.height) / 2
        if (rect.y < centerY) {
            centerY = -centerY
        }
        rect.translate(0, centerY)
        viewport.scrollRectToVisible(rect)
    }

    private fun restoreSortOrder(sorting: Map<Any, SortOrder>) {
        rowSorter.sortKeys = sorting.entries
                .associate { findColumnWithHeaderValue(it.key) to it.value }
                .filter { it.key != null }
                .map { RowSorter.SortKey(it.key!!.modelIndex, it.value) }
    }

    private fun restoreSelection(rowIndices: List<Int>, colIndices: List<Int>) {
        colIndices.forEach { addColumnSelectionInterval(it, it) }
        rowIndices.forEach { addRowSelectionInterval(it, it) }
    }

    private fun findColumnWithHeaderValue(headerValue: Any): TableColumn? {
        for (i in 0 until columnCount) {
            val column = columnModel.getColumn(i)
            if (column.headerValue == headerValue) {
                return column
            }
        }
        return null
    }

    private fun backupRowSelection() {
        if (!m_selectionListenerArmed) {
            return
        }
        m_selectedRows.clear()
        m_selectedRows.addAll(
                selectedRows
                        .map { convertRowIndexToModel(it) }
                        .map { indexToRowMapper(it) })
    }
}