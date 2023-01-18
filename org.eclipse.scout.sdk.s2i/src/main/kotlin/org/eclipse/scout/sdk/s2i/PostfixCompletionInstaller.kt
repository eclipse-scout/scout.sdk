/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i

import com.intellij.codeInsight.template.postfix.settings.PostfixTemplateStorage
import com.intellij.codeInsight.template.postfix.templates.LanguagePostfixTemplate
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.codeInsight.template.postfix.templates.editable.JavaEditablePostfixTemplate
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.pom.java.LanguageLevel
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi

class PostfixCompletionInstaller : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        val beansGetPostfixId = "org.eclipse.scout.sdk.s2i.beans"
        val storage = PostfixTemplateStorage.getInstance() // already get instance here so that the storage is initialized
        val javaPostfixTemplateProvider = LanguagePostfixTemplate.EP_NAME.extensionList
            .firstOrNull { it.language == JavaLanguage.INSTANCE.id }
            ?.instance as? PostfixTemplateProvider ?: return
        val existingTemplates = storage.getTemplates(javaPostfixTemplateProvider)
        val exists = existingTemplates.any { it.id == beansGetPostfixId }
        if (exists) return

        val beansApi = ScoutApi.latest().BEANS()
        val latestBeansFqn = beansApi.fqn()
        val beansGetMethodName = beansApi.methodName
        val template = JavaEditablePostfixTemplate(
            beansGetPostfixId, "beans", "$latestBeansFqn.$beansGetMethodName(\$EXPR\$.class)", "",
            emptySet(), LanguageLevel.JDK_1_8, true, javaPostfixTemplateProvider
        )
        existingTemplates.add(template)
        storage.setTemplates(javaPostfixTemplateProvider, existingTemplates)
    }
}