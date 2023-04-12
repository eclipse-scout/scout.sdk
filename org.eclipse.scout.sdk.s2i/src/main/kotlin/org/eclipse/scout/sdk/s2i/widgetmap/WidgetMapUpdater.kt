/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.widgetmap

import com.intellij.lang.ASTFactory
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ThrowableRunnable
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants
import org.eclipse.scout.sdk.core.s.widgetmap.WidgetMapCreateOperation
import org.eclipse.scout.sdk.core.typescript.IWebConstants
import org.eclipse.scout.sdk.core.typescript.builder.imports.ES6ImportCollector
import org.eclipse.scout.sdk.core.typescript.builder.imports.IES6ImportCollector
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule
import org.eclipse.scout.sdk.core.typescript.model.api.IObjectLiteral
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.FunctionSpi
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.model.js.JsModelManager
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModule
import org.eclipse.scout.sdk.s2i.resolveLocalPath
import org.eclipse.scout.sdk.s2i.util.ES6ImportUtils
import java.nio.file.Path
import java.util.regex.Pattern

object WidgetMapUpdater {

    fun update(file: VirtualFile, project: Project) {
        val filePair = file
            .takeIf { it.isValid && it.isInLocalFileSystem }
            ?.let { findFilePair(it, project) }
            ?: return
        val module = filePair.first.containingModule(false) ?: return
        val updateInfo = getWidgetMapUpdatesForModule(module, listOf(filePair)).firstOrNull() ?: return
        writeUpdate(updateInfo, project)
    }

    fun updateAsync(scope: SearchScope, project: Project) {
        object : Task.Backgroundable(project, message("update.widgetMap.in.scope"), true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
            override fun run(indicator: ProgressIndicator) {
                updateScope(scope, project, IdeaProgress(indicator))
            }
        }.queue()
    }

    private fun findFilePair(file: VirtualFile, project: Project): Pair<JSFile /* model */, JSFile? /* consumer */>? {
        val fileName = file.name
        val psiManager = PsiManager.getInstance(project)
        val modelFileSuffix = ScoutJsCoreConstants.MODEL_SUFFIX + IWebConstants.TS_FILE_SUFFIX
        var model: VirtualFile?
        val consumer: VirtualFile?
        val directory = file.parent
        if (fileName.endsWith(modelFileSuffix)) {
            // try to find model-consumer by name convention
            model = file
            val baseName = fileName.removeSuffix(modelFileSuffix)
            consumer = directory.findChild(baseName + IWebConstants.TS_FILE_SUFFIX)?.takeIf { it.isValid }
                ?: directory.findChild(baseName + IWebConstants.JS_FILE_SUFFIX)?.takeIf { it.isValid }
        } else {
            // try to find model by name convention
            model = directory.findChild(fileName.removeSuffix(IWebConstants.TS_FILE_SUFFIX).removeSuffix(IWebConstants.JS_FILE_SUFFIX) + modelFileSuffix)?.takeIf { it.isValid }
            if (model == null) {
                model = file
                consumer = null
            } else {
                consumer = file
            }
        }

        val modelPsi = psiManager.findFile(model) as? JSFile ?: return null
        if (!DialectDetector.isTypeScript(modelPsi)) return null // while the consumer may be a JS file, the model must be a TS file!
        val consumerPsi = consumer?.let { psiManager.findFile(it) } as? JSFile
        return modelPsi to consumerPsi
    }

    private fun updateScope(scope: SearchScope, project: Project, progress: IdeaProgress) {
        progress.init(100, message("update.widgetMap.in.scope"))
        progress.indicator.text2 = message("search.for.models")
        val updateInfos = IdeaEnvironment.computeInReadAction(project) {
            FileTypeIndex.getFiles(TypeScriptFileType.INSTANCE, GlobalSearchScope.EMPTY_SCOPE.union(scope))
                .filter { it.isValid && it.isInLocalFileSystem }
                .mapNotNull { findFilePair(it, project) }
                .groupBy { it.first.containingModule(false) }.entries
                .flatMap { e -> e.key?.let { getWidgetMapUpdatesForModule(it, e.value.distinct()) } ?: emptyList() }
        }
        progress.worked(70)
        writeUpdates(updateInfos, project, progress.newChild(30))
    }

    private fun getWidgetMapUpdatesForModule(module: Module, files: List<Pair<JSFile, JSFile?>>): List<WidgetMapUpdateInfo> {
        val nodeModule = JsModelManager.getOrCreateNodeModule(module)?.spi() as? IdeaNodeModule ?: return emptyList()
        return files
            .mapNotNull { file -> widgetMapInfoFor(file.first, file.second, nodeModule) }
            .toList()
    }

    private fun widgetMapInfoFor(modelFile: JSFile, consumerFile: JSFile?, nodeModule: IdeaNodeModule): WidgetMapUpdateInfo? {
        val modelFunctionPsi = findInChildrenOrGrandChildren(modelFile, JSFunction::class.java) ?: return null
        val modelFunction = nodeModule.elementsByPsi()[modelFunctionPsi] as? FunctionSpi ?: return null
        val objectLiteral = modelFunction.resultingObjectLiteral().orElse(null) ?: return null
        if (SdkLog.isInfoEnabled()) {
            SdkLog.info("Updating WidgetMaps for model '{}'.", modelFile.virtualFile.resolveLocalPath())
        }

        val consumerClassPsi = consumerFile?.let { findInChildrenOrGrandChildren(it, JSClass::class.java) }
        if (consumerClassPsi == null) {
            SdkLog.warning("Could not find a model owner for model '{}'. No declarations will be updated.", modelFile.virtualFile.resolveLocalPath())
        }
        val consumerClass = consumerClassPsi?.let { nodeModule.elementsByPsi()[it] as? ES6ClassSpi }

        val operation = WidgetMapCreateOperation()
        operation.setLiteral(objectLiteral.api())
        operation.setMainWidget(consumerClass?.api())
        operation.execute()
        return WidgetMapUpdateInfo(operation, modelFunctionPsi, consumerClassPsi)
    }

    private fun <T : PsiElement> findInChildrenOrGrandChildren(jsFile: JSFile, type: Class<T>): T? {
        return PsiTreeUtil.findChildOfType(jsFile, type)
            ?: jsFile.children.asSequence()
                .map { PsiTreeUtil.findChildOfType(it, type) }
                .firstOrNull()
    }

    private fun writeUpdates(updates: List<WidgetMapUpdateInfo>, project: Project, progress: IdeaProgress) {
        progress.init(updates.size, message("writing.new.widgetMaps"))
        updates.forEach {
            if (progress.indicator.isCanceled) return
            try {
                ApplicationManager.getApplication().invokeAndWait {
                    WriteAction.run<RuntimeException> {
                        progress.indicator.text2 = it.modelFunction.containingFile?.virtualFile?.name
                        writeUpdate(it, project)
                        progress.worked(1)
                    }
                }
            } catch (e: RuntimeException) {
                val realEx = if (e.cause == null || e.cause == e) e else e.cause
                SdkLog.warning("Error updating WidgetMaps for file '{}'.", it.modelFunction.containingFile?.virtualFile?.resolveLocalPath(), realEx)
            }
        }
    }

    private fun writeUpdate(updateInfo: WidgetMapUpdateInfo, project: Project) {
        WriteCommandAction.writeCommandAction(project).run(ThrowableRunnable<RuntimeException> {
            val operation = updateInfo.operation
            // update model
            updateWidgetMaps(updateInfo.modelFunction, operation.classSources(), operation.importsForModel(), operation.literal().containingModule())

            // update consumer
            updateInfo.modelConsumer
                ?.takeIf { DialectDetector.isTypeScript(it) } // only update widgetMap declaration in TS files for now
                ?.let { updateDeclarations(it, operation.declarationSources(), operation.importNamesForDeclarations(), operation.literal()) }
        })
    }

    private fun updateDeclarations(consumer: JSClass, declarationSources: Map<String, CharSequence>, importsForConsumer: List<IES6ImportCollector.ES6ImportDescriptor>, model: IObjectLiteral) {
        // replace fields
        declarationSources.entries.forEach { replaceField(consumer, it.key, it.value) }

        // add imports
        val modelFile = model.containingFile().orElse(null) ?: return
        val consumerFile = consumer.containingFile?.virtualFile?.resolveLocalPath() ?: return
        val module = model.containingModule()
        val from = if (isFileExportedFromModule(module, modelFile)) module.packageJson().mainLocation().orElseThrow() else modelFile
        val importFrom = ES6ImportCollector.buildRelativeImportPath(consumerFile, from)

        importsForConsumer.forEach { ES6ImportUtils.createOrUpdateImport(it.element.name(), it.alias, false, importFrom, consumer) }
    }

    private fun isFileExportedFromModule(module: INodeModule, modelFile: Path): Boolean {
        val moduleMain = module.packageJson().mainLocation().orElse(null) ?: return false
        val mainSource = module.source().orElse(null)?.asCharSequence() ?: return false
        val rel = ES6ImportCollector.buildRelativeImportPath(moduleMain, modelFile)
        val relPat = Pattern.quote(rel)
        val regex = Pattern.compile("export\\s+\\*\\s+from\\s+['\"]$relPat(?:\\.ts)?['\"]\\s*;")
        return regex.matcher(mainSource).find()
    }

    private fun replaceField(ownerClass: JSClass, fieldName: String, fieldSource: CharSequence) {
        val existingField = ownerClass.findFieldByName(fieldName)?.let { parentStatementOf(it) }
        val newField = parentStatementOf(JSPsiElementFactory.createJSSourceElement("class Dummy {$fieldSource}", ownerClass, JSClass::class.java).fields.first())
        if (existingField != null) {
            existingField.replace(newField)
            return
        }

        val fields = ownerClass.fields.sortedBy { it.textOffset }
        val insertAnchor: JSElement? = (fields.firstOrNull { it.hasModifier(JSAttributeList.ModifierType.DECLARE) }
            ?: fields.firstOrNull()
            ?: ownerClass.functions.minByOrNull { it.textOffset })
            ?.let { parentStatementOf(it) }
        if (insertAnchor == null) {
            ownerClass.addBefore(newField, ownerClass.node.lastChildNode.psi)
        } else {
            insertAnchor.parent.addBefore(newField, insertAnchor)
        }
    }

    private fun parentStatementOf(jsElement: JSElement): JSElement {
        val parent = jsElement.parent ?: return jsElement
        if (parent is JSVarStatement) return parent
        return jsElement
    }

    private fun updateWidgetMaps(modelFunction: JSFunction, newSources: List<CharSequence>, importsForModel: List<IES6ImportCollector.ES6ImportDescriptor>, modelModule: INodeModule) {
        val psiFile = modelFunction.containingFile

        importsForModel.forEach { ES6ImportUtils.createOrUpdateImport(it.element, it.alias, modelModule, modelFunction) }

        val topLevelElements = psiFile.children.toSet()
        val topLevelParent = PsiTreeUtil.findFirstParent(modelFunction, true) { topLevelElements.contains(it) } ?: return
        val container = topLevelParent.parent

        // remove all after our top-level element (all after it must be widget maps)
        var sibling = topLevelParent.nextSibling
        while (sibling != null) {
            sibling.delete()
            sibling = topLevelParent.nextSibling
        }
        container.node.addChild(ASTFactory.whitespace("\n")) // ensure trailing newline
        if (newSources.isEmpty()) return

        // add marker comment
        val markerComment = JSPsiElementFactory.createPsiComment(generatedWidgetMapsMarkerComment("\n"), psiFile)
        var lastInsert = container.addAfter(markerComment, topLevelParent)
        addSpaceBefore(lastInsert)

        // add new widget maps
        newSources.forEach {
            val newWidgetMapPsi = JSPsiElementFactory.createJSSourceElement(it.toString(), psiFile)
            lastInsert = container.addAfter(newWidgetMapPsi, lastInsert)
            addSpaceBefore(lastInsert)
        }
    }

    private fun addSpaceBefore(beforeChild: PsiElement) {
        beforeChild.parent.node.addChild(ASTFactory.whitespace("\n\n"), beforeChild.node)
    }

    fun generatedWidgetMapsMarkerComment(nl: String): String {
        return "/* **************************************************************************" + nl +
                "* GENERATED WIDGET MAPS" + nl +
                "* **************************************************************************/" + nl
    }

    private data class WidgetMapUpdateInfo(val operation: WidgetMapCreateOperation, val modelFunction: JSFunction, val modelConsumer: JSClass?) {
        override fun toString() = modelFunction.containingFile?.virtualFile?.name ?: ""
    }
}