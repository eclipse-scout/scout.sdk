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
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSFunction
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
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi
import org.eclipse.scout.sdk.core.typescript.model.spi.FunctionSpi
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import org.eclipse.scout.sdk.s2i.environment.IdeaProgress
import org.eclipse.scout.sdk.s2i.model.js.JsModelManager
import org.eclipse.scout.sdk.s2i.model.typescript.IdeaNodeModule

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
        // TODO performance tests with lots of large models
        object : Task.Backgroundable(project, message("update.widgetMap.in.scope"), true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
            override fun run(indicator: ProgressIndicator) {
                updateScope(scope, project, IdeaProgress(indicator))
            }
        }.queue()
    }

    private fun findFilePair(file: VirtualFile, project: Project): Pair<JSFile /* model */, JSFile /* consumer */>? {
        val fileName = file.name
        val psiManager = PsiManager.getInstance(project)
        val modelFileSuffix = ScoutJsCoreConstants.MODEL_SUFFIX + IWebConstants.TS_FILE_SUFFIX
        val model: VirtualFile?
        val consumer: VirtualFile?
        if (fileName.endsWith(modelFileSuffix)) {
            // try to find model-consumer by name convention
            model = file
            consumer = file.parent.findChild(fileName.removeSuffix(modelFileSuffix) + IWebConstants.TS_FILE_SUFFIX)?.takeIf { it.isValid }
        } else {
            // try to find model by name convention
            model = file.parent.findChild(fileName.removeSuffix(IWebConstants.TS_FILE_SUFFIX) + modelFileSuffix)?.takeIf { it.isValid }
            consumer = file
        }

        val modelPsi = model?.let { psiManager.findFile(it) } as? JSFile
        val consumerPsi = consumer?.let { psiManager.findFile(it) } as? JSFile
        if (modelPsi != null && consumerPsi != null) {
            return modelPsi to consumerPsi
        }
        return null
    }

    private fun updateScope(scope: SearchScope, project: Project, progress: IdeaProgress) {
        progress.init(100, message("update.widgetMap.in.scope"))
        progress.indicator.text2 = message("search.for.models")
        val updates = IdeaEnvironment.computeInReadAction(project) {
            FileTypeIndex.getFiles(TypeScriptFileType.INSTANCE, GlobalSearchScope.EMPTY_SCOPE.union(scope))
                .filter { it.name.endsWith(ScoutJsCoreConstants.MODEL_SUFFIX + IWebConstants.TS_FILE_SUFFIX) }
                .filter { it.isValid && it.isInLocalFileSystem }
                .mapNotNull { findFilePair(it, project) }
                .groupBy { it.first.containingModule(false) }.entries
                .flatMap { e -> e.key?.let { getWidgetMapUpdatesForModule(it, e.value) } ?: emptyList() }
        }
        progress.worked(70)
        updateFileContentsInBlocks(updates, project, progress.newChild(30))
    }

    private fun getWidgetMapUpdatesForModule(module: Module, files: List<Pair<JSFile, JSFile>>): List<WidgetMapUpdateInfo> {
        val nodeModule = JsModelManager.getOrCreateNodeModule(module)?.spi() as? IdeaNodeModule ?: return emptyList()
        return files
            .mapNotNull { file -> widgetMapInfoFor(file.first, file.second, nodeModule) }
            .toList()
    }

    private fun widgetMapInfoFor(modelFile: JSFile, consumerFile: JSFile, nodeModule: IdeaNodeModule): WidgetMapUpdateInfo? {
        val modelFunctionPsi = findInChildrenOrGrandChildren(modelFile, JSFunction::class.java) ?: return null
        val consumerClassPsi = findInChildrenOrGrandChildren(consumerFile, JSClass::class.java) ?: return null

        val modelFunction = nodeModule.elementsByPsi()[modelFunctionPsi] as? FunctionSpi ?: return null
        val objectLiteral = modelFunction.resultingObjectLiteral().orElse(null) ?: return null
        val consumerClass = nodeModule.elementsByPsi()[consumerClassPsi] as? ES6ClassSpi ?: return null

        val operation = WidgetMapCreateOperation()
        operation.setLiteral(objectLiteral.api())
        operation.isPage = consumerClass.api().supers().withSuperInterfaces(false).stream().anyMatch { ScoutJsCoreConstants.CLASS_NAME_PAGE == it.name() }
        operation.execute()
        return WidgetMapUpdateInfo(operation, modelFunctionPsi, consumerClassPsi)
    }

    private fun <T : PsiElement> findInChildrenOrGrandChildren(jsFile: JSFile, type: Class<T>): T? {
        return PsiTreeUtil.findChildOfType(jsFile, type)
            ?: jsFile.children.asSequence()
                .map { PsiTreeUtil.findChildOfType(it, type) }
                .firstOrNull()
    }

    private fun updateFileContentsInBlocks(updates: List<WidgetMapUpdateInfo>, project: Project, progress: IdeaProgress) {
        progress.init(updates.size, message("writing.new.widgetMaps"))
        updates.forEach {
            ApplicationManager.getApplication().invokeAndWait {
                WriteAction.run<RuntimeException> {
                    progress.indicator.text2 = it.modelFunction.containingFile.virtualFile.name
                    writeUpdate(it, project)
                    progress.worked(1)
                }
            }
        }
    }

    private fun writeUpdate(updateInfo: WidgetMapUpdateInfo, project: Project) {
        WriteCommandAction.writeCommandAction(project).run(ThrowableRunnable<RuntimeException> {
            try {
                val operation = updateInfo.operation
                updateModel(updateInfo.modelFunction, operation.classSources(), operation.importsForModel())
                updateConsumer(updateInfo.modelConsumer, operation.declarationSources(), operation.importNamesForDeclarations())
            } catch (e: RuntimeException) {
                SdkLog.warning("Error updating WidgetMaps for file '{}'.", updateInfo.modelFunction.containingFile.virtualFile, e)
            }
        })
    }

    private fun updateConsumer(consumer: JSClass, declarationSources: MutableMap<String, CharSequence>, importsForConsumer: MutableList<String>) {
        // TODO: update declaration in consumer & add imports
    }

    private fun updateModel(modelFunction: JSFunction, newSources: List<CharSequence>, importsForModel: MutableList<IES6Class>) {
        // TODO: add imports to model
        val psiFile = modelFunction.containingFile
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

    private data class WidgetMapUpdateInfo(val operation: WidgetMapCreateOperation, val modelFunction: JSFunction, val modelConsumer: JSClass) {
        override fun toString() = modelFunction.containingFile.virtualFile.name
    }
}