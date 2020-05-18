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
package org.eclipse.scout.sdk.s2i.nls.doc

import com.intellij.psi.PsiElement
import org.eclipse.scout.sdk.core.s.nls.query.TranslationPatterns
import org.eclipse.scout.sdk.core.util.Strings.withoutQuotes
import org.eclipse.scout.sdk.s2i.nls.completion.NlsCompletionContributorForJs.Companion.allNlsPatternsInJs

class NlsDocumentationProviderForJs : AbstractNlsDocumentationProvider() {
    override fun accept(element: PsiElement?) = allNlsPatternsInJs().accepts(element)

    override fun psiElementToKey(element: PsiElement): String? {
        var text = withoutQuotes(element.text).toString()
        val jsonPrefix = TranslationPatterns.JsonTextKeySearch.JSON_TEXT_KEY_PREFIX
        val jsonSuffix = TranslationPatterns.JsonTextKeySearch.JSON_TEXT_KEY_SUFFIX
        if (text.startsWith(jsonPrefix) && text.endsWith(jsonSuffix)) {
            text = text.substring(jsonPrefix.length, text.length - jsonSuffix.length)
        }
        return text
    }
}