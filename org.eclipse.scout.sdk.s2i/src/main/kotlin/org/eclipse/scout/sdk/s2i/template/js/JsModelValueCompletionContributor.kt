/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template.js

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.javascript.patterns.JSPatterns.jsProperty
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.extractMethod.newImpl.ExtractMethodHelper.addSiblingAfter
import com.intellij.util.ProcessingContext
import com.intellij.util.ThrowableRunnable
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsEnumPropertyValue
import org.eclipse.scout.sdk.core.typescript.IWebConstants
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.resolveLocalPath
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.PropertyCompletionInfo
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.SELECTED_ELEMENT
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.getPropertyValueInfo
import org.eclipse.scout.sdk.s2i.template.js.JsModelCompletionHelper.propertyElementPattern

class JsModelValueCompletionContributor : CompletionContributor() {

    init {
        // normal value completion
        extend(CompletionType.BASIC, propertyElementPattern(), JsModelValueCompletionProvider())

        // value completion when within an array (to add an array element)
        extend(CompletionType.BASIC, psiElement().withSuperParent(2, psiElement(JSArrayLiteralExpression::class.java).withParent(jsProperty())), JsModelValueCompletionProvider())
    }

    private class JsModelValueCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val start = System.currentTimeMillis()
            val completionInfo = getPropertyValueInfo(parameters, result) ?: return
            val elements = getPropertyValueElements(completionInfo) ?: return
            SdkLog.info("Scout JS model value completion took {}ms", System.currentTimeMillis() - start)
            if (elements.isNotEmpty()) {
                result.addAllElements(elements)
                result.stopHere()
            }
        }

        private fun getPropertyValueElements(completionInfo: PropertyCompletionInfo): List<LookupElement>? {
            val scoutJsProperty = if (ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE == completionInfo.propertyName) {
                completionInfo.parentScoutProperty()
            } else {
                completionInfo.scoutObjects().flatMap {
                    it.findProperties()
                        .withSuperClasses(true)
                        .withName(completionInfo.propertyName)
                        .stream()
                }.findAny().orElse(null)
            } ?: return emptyList()

            return scoutJsProperty.computePossibleValues(completionInfo.scoutJsModel).map {
                val lookupElement = JsModelCompletionHelper.createPropertyValueLookupElement(it, completionInfo)
                if (!completionInfo.isInLiteral) {
                    withJsImportIfNecessary(lookupElement, completionInfo.scoutJsModel, completionInfo.propertyPsi.containingFile.originalFile)
                } else {
                    lookupElement
                }
            }.toList()
        }

        private fun withJsImportIfNecessary(lookupElement: LookupElementBuilder, editModel: ScoutJsModel, place: PsiElement): LookupElementBuilder {
            val originalInsertHandler = lookupElement.insertHandler
            return lookupElement.withInsertHandler { context, item ->
                originalInsertHandler?.handleInsert(context, item)

                val elementToImport = findElementToImport(lookupElement) ?: return@withInsertHandler
                var importName = elementToImport.exportAlias().orElse(elementToImport.name())
                val firstDot = importName.indexOf('.')
                if (firstDot > 0) importName = importName.take(firstDot)

                val importFrom = if (elementToImport.containingModule() == editModel.nodeModule()) {
                    // relative path inside same module
                    val editDir = place.containingFile.virtualFile.parent.resolveLocalPath() ?: return@withInsertHandler
                    val editModelPackageJson = editModel.nodeModule().packageJson()
                    val main = editModelPackageJson.directory().resolve(editModelPackageJson.main().orElse(""))
                    val rel = editDir.relativize(main).toString().replace('\\', '/')
                    Strings.removeSuffix(Strings.removeSuffix(rel, IWebConstants.TS_FILE_SUFFIX), IWebConstants.JS_FILE_SUFFIX)
                } else {
                    // import directly to other module
                    elementToImport.containingModule().name()
                }

                if (!ApplicationManager.getApplication().isWriteAccessAllowed) {
                    WriteCommandAction.writeCommandAction(context.project).run(ThrowableRunnable<RuntimeException> {
                        createOrUpdateImport(importName, importFrom, place)
                    })
                } else {
                    createOrUpdateImport(importName, importFrom, place)
                }
            }
        }

        /**
         * Own implementation as JSImportAction is internal API and ES6ImportPsiUtil does not work correctly in TypeScript
         */
        private fun createOrUpdateImport(importName: String, importFrom: String, place: PsiElement) {
            val psiFile = place.containingFile
            val imports = PsiTreeUtil.getChildrenOfTypeAsList(psiFile, ES6ImportDeclaration::class.java)
            val existingImport = imports.firstOrNull { Strings.withoutQuotes(it.fromClause?.referenceText) == importFrom }
            if (existingImport != null) {
                // check if already imported
                val existingSpecifiers = existingImport.importSpecifiers.toList()
                if (existingSpecifiers.any { it.declaredName == importName }) return // already exists

                // add new specifier to existing import declaration
                val specifiers = existingSpecifiers.map { it.text }.toMutableList()
                specifiers.add(importName)
                val newSpecifiers = specifiers.sorted().joinToString(", ")
                val newImport = JSPsiElementFactory.createJSSourceElement("import {$newSpecifiers} from '$importFrom';", place, existingImport.javaClass)
                existingImport.replace(newImport)
                return
            } else {
                // add new import declaration
                val importToAdd = JSPsiElementFactory.createJSSourceElement("import {$importName} from '$importFrom';", place, ES6ImportDeclaration::class.java)
                if (imports.isEmpty()) {
                    val firstNotComment = psiFile.children.firstOrNull { it !is PsiComment } ?: psiFile.children.firstOrNull()
                    psiFile.addBefore(importToAdd, firstNotComment)
                } else {
                    imports.last().addSiblingAfter(importToAdd)
                }
            }
        }

        private fun findElementToImport(lookupElement: LookupElementBuilder): INodeElement? {
            val lookupElementViewModel = lookupElement.getUserData(SELECTED_ELEMENT) ?: return null
            val property = lookupElementViewModel.property()
            if (property.type().isEnumLike && lookupElementViewModel is JsModelCompletionHelper.JsValueLookupElement && lookupElementViewModel.propertyValue is ScoutJsEnumPropertyValue) {
                return lookupElementViewModel.propertyValue.scoutJsEnum.topLevelReference()
            }
            if (property.scoutJsObject().scoutJsModel().supportsClassReference() && (property.type().hasClasses() || property.isObjectType)) {
                val objectLookupElement = lookupElementViewModel as? JsModelCompletionHelper.JsObjectValueLookupElement ?: return null
                return objectLookupElement.propertyValue.scoutJsObject.declaringClass()
            }
            return null
        }
    }
}