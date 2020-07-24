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

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import org.eclipse.scout.sdk.s2i.nls.PsiTranslationPatterns

class NlsCompletionContributorForHtml : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PsiTranslationPatterns.HTML_KEY_PATTERN, NlsCompletionContributorForJava.DefaultNlsCompletionProvider())
    }
}