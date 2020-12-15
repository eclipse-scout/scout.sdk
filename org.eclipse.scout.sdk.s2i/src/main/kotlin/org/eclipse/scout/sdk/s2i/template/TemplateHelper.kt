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
package org.eclipse.scout.sdk.s2i.template

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key
import org.eclipse.scout.sdk.core.util.Strings

object TemplateHelper {

    val SCOUT_LOOKUP_ELEMENT_MARKER = Key.create<Boolean>("ScoutLookupElement.marker")

    fun removePrefix(editor: Editor, prefix: CharSequence) {
        if (Strings.isEmpty(prefix)) {
            return
        }
        val document = editor.document
        val offset = editor.caretModel.offset
        var start = offset - prefix.length - 1
        val limit = 0.coerceAtLeast(start - 5)
        val chars = document.immutableCharSequence
        // reduce start index of removal to any preceding alphabet characters
        // this is required for fast typing where the prefix is "older" than the current content of the document
        while (start >= limit && isAlphaChar(chars[start])) {
            start--
        }
        document.replaceString(start + 1, offset, "")
    }

    private fun isAlphaChar(char: Char) = char in 'a'..'z' || char in 'A'..'Z'
}