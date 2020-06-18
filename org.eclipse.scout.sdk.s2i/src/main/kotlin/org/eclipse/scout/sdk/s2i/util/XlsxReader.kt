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
package org.eclipse.scout.sdk.s2i.util

import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.util.XMLHelper
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler
import org.apache.poi.xssf.model.SharedStrings
import org.apache.poi.xssf.model.Styles
import org.apache.poi.xssf.usermodel.XSSFComment
import org.eclipse.scout.sdk.core.util.SdkException
import org.xml.sax.InputSource
import java.io.File
import java.io.InputStream


class XlsxReader {

    /**
     * Parses the first sheet of the given [File] and returns its cell values.
     * @param file The file to import
     * @return the table content of the first sheet of the workbook.
     * @throws SdkException if there is an error parsing the file.
     */
    fun parse(file: File): List<List<String>> {
        try {
            OPCPackage.open(file, PackageAccess.READ).use {
                val reader = XSSFReader(it)
                return processFirstSheet(ReadOnlySharedStringsTable(it), reader.stylesTable, reader.sheetsData)
            }
        } catch (e: Exception) {
            throw SdkException(e)
        }
    }

    private fun processFirstSheet(strings: SharedStrings, styles: Styles, sheets: Iterator<InputStream>): List<List<String>> {
        if (!sheets.hasNext()) {
            return emptyList()
        }
        sheets.next().use { sheetInputStream ->
            val formatter = DataFormatter()
            val dataCollector = SheetDataCollector()
            val sheetParser = XMLHelper.newXMLReader()
            sheetParser.contentHandler = XSSFSheetXMLHandler(styles, null, strings, dataCollector, formatter, false)
            sheetParser.parse(InputSource(sheetInputStream))
            return dataCollector.tableData()
        }
    }

    private class SheetDataCollector : XSSFSheetXMLHandler.SheetContentsHandler {

        private val m_table: MutableList<List<String>> = ArrayList()
        private var m_currentRow: MutableList<String>? = null

        override fun startRow(rowNum: Int) {
            val nextRow = ArrayList<String>()
            m_currentRow = nextRow
            m_table.add(nextRow)
        }

        override fun endRow(rowNum: Int) {
            m_currentRow = null
        }

        override fun cell(cellReference: String?, formattedValue: String?, comment: XSSFComment?) {
            m_currentRow!!.add(formattedValue ?: "")
        }

        fun tableData() = m_table
    }
}