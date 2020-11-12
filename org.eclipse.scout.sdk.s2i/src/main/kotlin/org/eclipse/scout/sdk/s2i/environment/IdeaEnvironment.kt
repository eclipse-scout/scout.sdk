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
package org.eclipse.scout.sdk.s2i.environment

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.progress.util.ProgressIndicatorUtils.runWithWriteActionPriority
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope.allScope
import org.eclipse.scout.sdk.core.builder.BuilderContext
import org.eclipse.scout.sdk.core.builder.IBuilderContext
import org.eclipse.scout.sdk.core.builder.ISourceBuilder
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext
import org.eclipse.scout.sdk.core.generator.ISourceGenerator
import org.eclipse.scout.sdk.core.generator.compilationunit.CompilationUnitPath
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.environment.SdkFuture
import org.eclipse.scout.sdk.core.util.*
import org.eclipse.scout.sdk.core.util.CoreUtils.toStringIfOverwritten
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.s2i.*
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.TransactionManager.Companion.repeatUntilPassesWithIndex
import org.eclipse.scout.sdk.s2i.environment.model.JavaEnvironmentWithIdea
import org.eclipse.scout.sdk.s2i.util.getNioPath
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.streams.asStream


open class IdeaEnvironment private constructor(val project: Project) : IEnvironment, AutoCloseable {

    companion object Factory {

        fun <T> callInIdeaEnvironmentSync(project: Project, progressIndicator: IdeaProgress, task: (IdeaEnvironment, IdeaProgress) -> T): T =
                IdeaEnvironment(project).use {
                    return task.invoke(it, progressIndicator)
                }

        fun <T> callInIdeaEnvironment(project: Project, title: String, task: (IdeaEnvironment, IdeaProgress) -> T): IFuture<T> {
            val result = FinalValue<T>()
            val name = Strings.notBlank(title).orElseGet { toStringIfOverwritten(task).orElse(message("unnamed.task.x", task)) }
            val job = OperationTask(name, project) { pr ->
                callInIdeaEnvironmentSync(project, pr) { e, p ->
                    result.setIfAbsent(task.invoke(e, p))
                }
            }
            return job.schedule({ result.get() })
        }

        fun <T> computeInReadAction(project: Project, callable: () -> T): T = repeatUntilPassesWithIndex(project, true, callable)

        fun <T> computeInLongReadAction(project: Project, progressIndicator: ProgressIndicator, callable: () -> T): T {
            if (ApplicationManager.getApplication().isReadAccessAllowed) {
                return callable.invoke()
            }

            val result = FinalValue<T>()
            var success = false
            while (!success && !progressIndicator.isCanceled) {
                /* do not pass outer progress indicator. otherwise it might be canceled inside but then it is not possible to perform a retry (it is canceled already) */
                /* therefore use a fresh indicator for each retry and use the outer indicator to stop retrying */
                success = runWithWriteActionPriority({ result.set(computeInReadAction(project, callable)) }, EmptyProgressIndicator())
                if (!success) {
                    ProgressIndicatorUtils.yieldToPendingWriteActions()
                }
            }
            return result.get()
        }

        fun toIdeaProgress(progress: IProgress?): IdeaProgress = progress?.toIdea() ?: IdeaProgress(null)
    }

    private val m_envs = ConcurrentHashMap<String, JavaEnvironmentWithIdea>()

    override fun close() {
        m_envs.values.forEach(AutoCloseable::close)
        m_envs.clear()
    }

    override fun findType(fqn: String) = project
            .findTypesByName(Ensure.notBlank(fqn), allScope(project))
            .mapNotNull { it.toScoutType(this) }
            .asStream()

    override fun findJavaEnvironment(root: Path?): Optional<IJavaEnvironment> {
        var path = root
        while (path != null) {
            val env = path.toVirtualFile()?.containingModule(project)?.let { toScoutJavaEnvironment(it) }
            if (env != null) {
                return Optional.of(env)
            }
            path = path.parent
        }
        return Optional.empty()
    }

    override fun rootOfJavaEnvironment(environment: IJavaEnvironment?): Path {
        val spi = environment?.unwrap() ?: throw newFail("Java environment must not be null")
        val ideaEnv = spi as JavaEnvironmentWithIdea
        val root = ideaEnv.module.rootManager.contentRoots.firstOrNull() ?: throw newFail("Java environment '{}' has no root directory.", ideaEnv)
        return root.getNioPath()
    }

    fun toScoutJavaEnvironment(module: Module?): IJavaEnvironment? = module
            ?.takeIf { it.isJavaModule() }
            ?.let { getOrCreateEnv(it) }
            ?.wrap()

    fun findClasspathEntry(classpathRoot: VirtualFile?): IClasspathEntry? = classpathRoot
            ?.let { ProjectRootManager.getInstance(project).fileIndex.getModuleForFile(it) }
            ?.let { toScoutJavaEnvironment(it) }
            ?.let { findClasspathEntry(classpathRoot, it) }

    protected fun findClasspathEntry(file: VirtualFile, env: IJavaEnvironment): IClasspathEntry? = env.sourceFolders()
            .filter { it.path().startsWith(file.getNioPath()) }
            .findAny()
            .orElse(null)

    protected fun getOrCreateEnv(module: Module): JavaEnvironmentWithIdea = m_envs.computeIfAbsent(module.name) { createNewJavaEnvironmentFor(module) }

    protected fun createNewJavaEnvironmentFor(module: Module): JavaEnvironmentWithIdea = JavaEnvironmentWithIdea(module)

    override fun writeCompilationUnit(generator: ICompilationUnitGenerator<*>, targetFolder: IClasspathEntry): IType? =
            writeCompilationUnit(generator, targetFolder, null)

    override fun writeCompilationUnit(generator: ICompilationUnitGenerator<*>, targetFolder: IClasspathEntry, progress: IProgress?): IType? =
            doWriteCompilationUnit(generator, targetFolder, toIdeaProgress(progress), true).result()

    override fun writeCompilationUnitAsync(generator: ICompilationUnitGenerator<*>, targetFolder: IClasspathEntry, progress: IProgress?): IFuture<IType?> =
            doWriteCompilationUnit(generator, targetFolder, toIdeaProgress(progress), false)

    fun createResource(generator: ISourceGenerator<ISourceBuilder<*>>, filePath: Path): StringBuilder =
            createResource(generator, findJavaEnvironment(filePath).orElseThrow { newFail("Cannot find Java environment for path '{}'.", filePath) }, filePath)

    override fun createResource(generator: ISourceGenerator<ISourceBuilder<*>>, targetFolder: IClasspathEntry): StringBuilder =
            createResource(generator, targetFolder.javaEnvironment(), targetFolder.path())

    protected fun createResource(generator: ISourceGenerator<ISourceBuilder<*>>, context: IJavaEnvironment, filePath: Path): StringBuilder {
        val env = context.unwrap() as JavaEnvironmentWithIdea
        val props = PropertySupport(2)
        props.setProperty(IBuilderContext.PROPERTY_JAVA_MODULE, env.module)
        props.setProperty(IBuilderContext.PROPERTY_TARGET_PATH, filePath)

        // must be \n! see https://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/modifying_psi.html
        // do not use CodeStyle.getSettings(project).lineSeparator because the CompilationUnitWriter uses createFileFromText
        val nl = "\n"
        val ctx = BuilderContext(nl, props)
        val builder = MemorySourceBuilder(JavaBuilderContext(ctx, context))
        generator.generate(builder)
        return builder.source()
    }

    override fun writeResource(content: CharSequence, filePath: Path, progress: IProgress?) {
        doWriteResource(filePath, content).awaitDoneThrowingOnErrorOrCancel()
        return
    }

    override fun writeResource(generator: ISourceGenerator<ISourceBuilder<*>>, filePath: Path, progress: IProgress?) =
            writeResource(createResource(generator, filePath), filePath, progress)

    override fun writeResourceAsync(generator: ISourceGenerator<ISourceBuilder<*>>, filePath: Path, progress: IProgress?): IFuture<Void> =
            writeResourceAsync(createResource(generator, filePath), filePath, progress)

    override fun writeResourceAsync(content: CharSequence, filePath: Path, progress: IProgress?): IFuture<Void> =
            doWriteResource(filePath, content)

    protected fun doWriteResource(filePath: Path, content: CharSequence): IFuture<Void> {
        // No need for async support here as the FIleWriter is just registered and no actual action is performed yet
        // The real write is done on transaction commit
        TransactionManager.current().register(FileWriter(filePath, content, project))
        return SdkFuture.completed(null)
    }

    protected fun doWriteCompilationUnit(generator: ICompilationUnitGenerator<*>, targetFolder: IClasspathEntry, progress: IdeaProgress, sync: Boolean): IFuture<IType?> {
        Ensure.isTrue(targetFolder.isSourceFolder)
        val pck = generator.packageName().orElse("")
        val name = generator.elementName().orElseThrow { newFail("File name missing in generator") }
        val path = CompilationUnitPath(generator, targetFolder)
        val code = createResource(generator, targetFolder.javaEnvironment(), path.targetFile())
        val javaEnv = targetFolder.javaEnvironment()
        val writer = CompilationUnitWriteOperation(project, code, path)
        val supplier = lambda@{
            val createdUnit = writer.createdPsi ?: return@lambda null

            val reloadRequired = javaEnv.registerCompilationUnitOverride(pck, name + JavaTypes.JAVA_FILE_SUFFIX, createdUnit.text)
            if (reloadRequired) {
                javaEnv.reload()
            }

            val fqn = StringBuilder()
            if (Strings.hasText(pck)) {
                fqn.append(pck).append(JavaTypes.C_DOT)
            }
            fqn.append(name)

            return@lambda javaEnv.findType(fqn.toString()).orElse(null)
        }

        if (sync) {
            return writer.run(progress, supplier)
        }
        return writer.schedule(supplier)
    }
}
