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
package org.eclipse.scout.sdk.s2i.nls.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.ProcessingContext
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.nls.PsiNlsPatterns.Companion.textsGetPattern
import org.eclipse.scout.sdk.s2i.nls.completion.NlsCompletionHelper.Companion.computeLookupElements
import kotlin.streams.toList

class NlsCompletionContributorForJava : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, textsGetPattern(), DefaultNlsCompletionProvider())
    }

    internal class DefaultNlsCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val module = parameters.position.containingModule() ?: return
            result.addAllElements(computeLookupElements(module).toList())
            result.stopHere()
        }
    }
}