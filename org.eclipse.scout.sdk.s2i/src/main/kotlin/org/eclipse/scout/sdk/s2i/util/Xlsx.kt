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
package org.eclipse.scout.sdk.s2i.util

import org.apache.poi.ss.formula.BaseFormulaEvaluator
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.eclipse.scout.sdk.core.util.CoreUtils
import org.eclipse.scout.sdk.core.util.SdkException
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


object Xlsx {

    /**
     * Exports the given table data into an xlsx file
     * @param tableData The data to export
     * @param sheetName The name of the sheet holding the data
     * @param file The file to export to
     */
    fun write(tableData: List<List<String?>>, sheetName: String, file: File) {
        val wb = XSSFWorkbook()
        wb.properties.coreProperties.creator = CoreUtils.getUsername()
        wb.properties.coreProperties.title = "Translation Export"
        wb.properties.extendedProperties.application = "Eclipse Scout Plugin for IntelliJ IDEA"
        val sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(sheetName))
        for (rowIndex in tableData.indices) {
            val row = sheet.createRow(rowIndex)
            val rowData = tableData[rowIndex]
            for (colIndex in rowData.indices) {
                row.createCell(colIndex)
                        .setCellValue(rowData[colIndex])
            }
        }

        BufferedOutputStream(FileOutputStream(file)).use {
            wb.write(it)
        }
    }

    /**
     * Parses the first sheet of the given [File] and returns its cell values.
     * @param file The file to import
     * @return the table content of the first sheet of the workbook.
     * @throws SdkException if there is an error parsing the file.
     */
    fun parse(file: File): List<List<String?>> {
        try {
            WorkbookFactory.create(file, null, true).use {
                val sheets = it.sheetIterator()
                if (!sheets.hasNext()) {
                    return emptyList()
                }
                val formulaEvaluator = it.creationHelper.createFormulaEvaluator() as BaseFormulaEvaluator
                val cfEvaluator = ConditionalFormattingEvaluator(it, formulaEvaluator)
                return processFirstSheet(sheets.next(), formulaEvaluator, cfEvaluator)
            }
        } catch (e: Exception) {
            throw SdkException(e)
        }
    }

    private fun processFirstSheet(sheet: Sheet, formulaEvaluator: FormulaEvaluator, cfEvaluator: ConditionalFormattingEvaluator): List<List<String?>> {
        val firstRowIndex = sheet.firstRowNum
        val lastRowIndex = sheet.lastRowNum
        val data = ArrayList<ArrayList<String?>>(lastRowIndex - firstRowIndex + 1)
        val formatter = DataFormatter()

        for (rowIndex in firstRowIndex..lastRowIndex) {
            val row = sheet.getRow(rowIndex) ?: continue
            val firstCellIndex = row.firstCellNum
            val lastCellIndex = row.lastCellNum
            val rowData = ArrayList<String?>(lastCellIndex - firstCellIndex + 1)
            data.add(rowData)
            for (colIndex in firstCellIndex..lastCellIndex) {
                val text = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                        ?.let { formatter.formatCellValue(it, formulaEvaluator, cfEvaluator) }
                rowData.add(text)
            }
        }
        return data
    }
}