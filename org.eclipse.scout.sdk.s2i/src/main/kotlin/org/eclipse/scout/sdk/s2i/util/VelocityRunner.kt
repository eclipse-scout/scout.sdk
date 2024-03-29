/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.util

import org.apache.velocity.VelocityContext
import org.eclipse.scout.sdk.core.util.Strings
import org.jetbrains.java.generate.velocity.VelocityFactory
import java.io.StringWriter
import java.util.regex.Pattern

class VelocityRunner {

    companion object {
        const val DOLLAR = "dollar"
    }

    private val m_properties = HashMap<String, Any>()
    private val m_postProcessors = HashMap<Pattern, (java.util.regex.MatchResult) -> String>()

    init {
        withProperty(DOLLAR, "$")
    }

    fun withPostProcessor(pattern: Pattern, replacer: (java.util.regex.MatchResult) -> String): VelocityRunner {
        m_postProcessors[pattern] = replacer
        return this
    }

    fun withProperty(name: String, value: Any): VelocityRunner {
        m_properties[name] = value
        return this
    }

    fun withProperties(props: Map<String, Any>): VelocityRunner {
        m_properties.putAll(props)
        return this
    }

    fun eval(template: String): CharSequence {
        var evaluated = evalVelocity(template)
        for (postProcessor in m_postProcessors) {
            val pattern = postProcessor.key
            val replacer = postProcessor.value
            evaluated = pattern.matcher(evaluated).replaceAll(replacer)
        }
        return evaluated
    }

    private fun evalVelocity(template: String): CharSequence {
        val out = StringWriter()
        val context = VelocityContext(m_properties)
        VelocityFactory.getVelocityEngine().evaluate(context, out, VelocityRunner::class.java.name, template)
        return Strings.replace(out.buffer, "\r", "")
    }
}