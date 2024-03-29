/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.ui

import com.intellij.ui.components.JBTextArea
import java.awt.Dimension
import java.awt.Font
import kotlin.math.max

/**
 * A [JBTextArea] which calculates its preferred size based on the text content:
 *
 * Width: If the textarea is empty, the default width otherwise the with of the longest line
 *
 * Height: The number of lines
 */
open class TextAreaWithContentSize(font: Font, text: String = "") : JBTextArea(text) {

    init {
        this.font = font
    }

    override fun getPreferredSize(): Dimension {
        val metrics = getFontMetrics(font)
        val content = text
        val lines = content.split("\n")
        val inset = insets
        val width: Int
        if (content.isEmpty()) {
            // default width if empty
            width = super.getPreferredSize().width
        } else {
            var maxTextWidth = 0
            for (line in lines) {
                maxTextWidth = max(maxTextWidth, metrics.stringWidth(line))
            }
            width = maxTextWidth + inset.left + inset.right
        }

        return Dimension(width, lines.size * rowHeight + inset.top + inset.bottom)
    }
}