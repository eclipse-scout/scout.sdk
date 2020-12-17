/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.ui

import com.intellij.ui.components.JBTextField
import org.eclipse.scout.sdk.core.util.FinalValue
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

/**
 * A [JBTextField] that allows to specify a max length
 */
class TextFieldWithMaxLen(text: String = "", columns: Int = 0, maxLength: Int = UNLIMITED) : JBTextField(text, columns) {

    companion object {
        const val UNLIMITED = Int.MAX_VALUE
    }

    init {
        setMaxLength(maxLength)
    }

    fun setMaxLength(maxLength: Int) = withDocument {
        if (maxLength > 0) {
            it.documentFilter = MaxLengthDocumentFilter(maxLength)
        } else {
            it.documentFilter = null
        }
    }

    @Suppress("unused")
    fun getMaxLength(): Int {
        val length = FinalValue<Int>()
        withDocument {
            val filter = it.documentFilter
            if (filter is MaxLengthDocumentFilter) {
                length.set(filter.maxLength)
            }
        }
        return length.opt().orElse(UNLIMITED)
    }

    private fun withDocument(runnable: (AbstractDocument) -> Unit) {
        val myDocument = document
        if (myDocument is AbstractDocument) {
            return runnable.invoke(myDocument)
        }
    }

    class MaxLengthDocumentFilter(val maxLength: Int) : DocumentFilter() {
        override fun insertString(fb: FilterBypass, offset: Int, textToInsert: String, attr: AttributeSet?) {
            replace(fb, offset, 0, textToInsert, attr)
        }

        override fun replace(fb: FilterBypass, offset: Int, lengthOfTextToDelete: Int, textToInsert: String, attrs: AttributeSet?) {
            val currentLength = fb.document.length
            val overLimit = currentLength - lengthOfTextToDelete + textToInsert.length - maxLength
            var limitedTextToInsert = textToInsert
            if (overLimit > 0) {
                limitedTextToInsert = textToInsert.substring(0, textToInsert.length - overLimit)
            }
            super.replace(fb, offset, lengthOfTextToDelete, limitedTextToInsert, attrs)
        }
    }
}