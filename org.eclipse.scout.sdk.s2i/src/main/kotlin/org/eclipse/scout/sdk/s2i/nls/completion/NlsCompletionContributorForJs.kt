/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.nls.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PsiJavaPatterns.psiElement
import org.eclipse.scout.sdk.s2i.nls.PsiTranslationPatternsForJs
import org.eclipse.scout.sdk.s2i.nls.completion.NlsCompletionContributorForJava.DefaultNlsCompletionProvider

class NlsCompletionContributorForJs : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, psiElement().withParent(PsiTranslationPatternsForJs.ANY_JS_PATTERN), DefaultNlsCompletionProvider())
    }
}