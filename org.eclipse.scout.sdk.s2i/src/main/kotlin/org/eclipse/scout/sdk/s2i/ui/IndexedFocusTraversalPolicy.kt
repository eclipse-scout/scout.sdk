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

import java.awt.Component
import java.awt.Container
import java.awt.FocusTraversalPolicy
import java.util.*

/**
 * A [FocusTraversalPolicy] that allows to apply a tab index to a component.
 */
open class IndexedFocusTraversalPolicy : FocusTraversalPolicy() {

    private val m_components = TreeMap<Int, Component>()

    fun addComponent(component: Component) {
        val highest = if (m_components.isEmpty()) -1 else m_components.lastKey()
        addComponent(component, highest + 1)
    }

    fun addComponent(component: Component, position: Int) {
        m_components[position] = component
    }

    override fun getComponentAfter(aContainer: Container?, aComponent: Component?): Component? = advanceToNextVisibleComponent(aComponent, true)

    override fun getComponentBefore(aContainer: Container?, aComponent: Component?): Component? = advanceToNextVisibleComponent(aComponent, false)

    private fun advanceToNextVisibleComponent(aComponent: Component?, forward: Boolean): Component? {
        val start = m_components.entries
                .filter { it.value == aComponent }
                .map { it.key }.firstOrNull() ?: return null
        var candidate: Component?
        var position: Int = start
        do {
            var entry = if (forward) m_components.higherEntry(position) else m_components.lowerEntry(position)
            if (entry == null) {
                entry = if (forward) m_components.firstEntry() else m_components.lastEntry()
            }
            candidate = entry?.value
            position = entry.key
        } while (candidate != null && candidate != aComponent && (!candidate.isShowing || !candidate.isEnabled))
        return candidate
    }

    override fun getDefaultComponent(aContainer: Container?): Component? = getFirstComponent(aContainer)

    override fun getLastComponent(aContainer: Container?): Component? = m_components.lastEntry().value

    override fun getFirstComponent(aContainer: Container?): Component? = m_components.firstEntry().value
}