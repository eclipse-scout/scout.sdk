/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key
import org.eclipse.scout.sdk.core.util.Strings

object TemplateHelper {

    val SCOUT_LOOKUP_ELEMENT_MARKER = Key.create<Boolean>("ScoutLookupElement.marker")

    fun removePrefix(editor: Editor, prefix: CharSequence): Int {
        val document = editor.document
        val offset = editor.caretModel.offset
        var start = offset - 1 // move to the position before the caret
        if (Strings.hasText(prefix)) start -= prefix.length // remove the prefix
        val chars = document.immutableCharSequence

        // reduce start index of removal to any preceding alphabet characters
        // this is required for fast typing where the prefix is "older" than the current content of the document
        val limit = 0.coerceAtLeast(start - 10 /* not more than 10 chars back */)
        while (start >= limit && isAlphaChar(chars[start])) {
            start--
        }
        start++
        document.replaceString(start, offset, "")
        return offset - start
    }

    private fun isAlphaChar(char: Char) = char in 'a'..'z' || char in 'A'..'Z'
}