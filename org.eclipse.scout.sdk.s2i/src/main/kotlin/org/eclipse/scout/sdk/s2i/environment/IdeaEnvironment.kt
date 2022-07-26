/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.environment

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.NonBlockingReadAction
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope.allScope
import com.intellij.util.concurrency.AppExecutorUtil
import org.eclipse.scout.sdk.core.builder.BuilderContext
import org.eclipse.scout.sdk.core.builder.IBuilderContext
import org.eclipse.scout.sdk.core.builder.ISourceBuilder
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext
import org.eclipse.scout.sdk.core.generator.ISourceGenerator
import org.eclipse.scout.sdk.core.log.SdkLog
import org.eclipse.scout.sdk.core.model.CompilationUnitInfoWithClasspath
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.model.spi.JavaEnvironmentSpi
import org.eclipse.scout.sdk.core.s.environment.AbstractEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.s.environment.SdkFuture
import org.eclipse.scout.sdk.core.s.js.element.gen.IJsSourceBuilder
import org.eclipse.scout.sdk.core.s.js.element.gen.IJsSourceGenerator
import org.eclipse.scout.sdk.core.s.js.element.gen.JsSourceBuilder
import org.eclipse.scout.sdk.core.util.CoreUtils.toStringIfOverwritten
import org.eclipse.scout.sdk.core.util.Ensure
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.PropertySupport
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.*
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import org.eclipse.scout.sdk.s2i.environment.model.JavaEnvironmentWithIdea
import org.jetbrains.concurrency.CancellablePromise
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.streams.asStream


open class IdeaEnvironment private constructor(val project: Project) : AbstractEnvironment() {

    companion object Factory {

        /**
         * Executes the [task] synchronously in the current thread
         */
        fun <T> callInIdeaEnvironmentSync(project: Project, progressIndicator: IdeaProgress, task: (IdeaEnvironment, IdeaProgress) -> T): T =
            IdeaEnvironment(project).use {
                return task(it, progressIndicator)
            }

        /**
         * Asynchronously executes the given [task] in a [OperationTask]. The task is executed with an active [TransactionManager].
         * @param title The description of the async task. Will be displayed to the user. If empty, the [toString] method of the task is used (if available).
         */
        fun <T> callInIdeaEnvironment(project: Project, title: String, task: (IdeaEnvironment, IdeaProgress) -> T): IFuture<T> {
            val result = FinalValue<T>()
            val name = Strings.notBlank(title).orElseGet { toStringIfOverwritten(task).orElse(message("unnamed.task.x", task)) }
            val job = OperationTask(name, project) { progress ->
                callInIdeaEnvironmentSync(project, progress) { e, p ->
                    result.setIfAbsent(task(e, p))
                }
            }
            return job.schedule({ result.get() })
        }

        /**
         * Like [computeInReadActionAsync] but the calling thread is blocked until the result is available.
         */
        fun <T> computeInReadAction(project: Project, requireSmartMode: Boolean = true, progress: ProgressIndicator? = null, callable: () -> T): T {
            if (ApplicationManager.getApplication().isReadAccessAllowed) {
                // already in read action: don't submit non-blocking read-action (could end up in a dead-lock). Instead, directly execute
                // also don't repeat until indexes are ready. If here the read-lock is already held, it must be released so that the dump mode can end
                return callable()
            }
            return createReadAction(project, requireSmartMode, progress, callable).executeSynchronously()
        }


        /**
         * Executes the given [callable] in a read action. If the method is invoked when already holding the read lock, it is directly executed in the calling thread.
         * Otherwise, it is executed in an asynchronous action bound to the given [Project].
         * @param requireSmartMode If true, the read action waits until smart mode is available and is repeated until successful while in smart mode.
         * @param progress An optional [ProgressIndicator] to use if executed asynchronously.
         * @return A [CancellablePromise] representing the asynchronous computation.
         */
        fun <T> computeInReadActionAsync(project: Project, requireSmartMode: Boolean = true, progress: ProgressIndicator? = null, callable: () -> T): CancellablePromise<T> {
            val action = createReadAction(project, requireSmartMode, progress, callable)
            // use WrappingCancellablePromise to unwrap ExecutionExceptions.
            // this is required so that ControlFlowExceptions are correctly rethrown.
            return WrappingCancellablePromise(action.submit(AppExecutorUtil.getAppExecutorService()))
        }

        private fun <T> createReadAction(project: Project, requireSmartMode: Boolean = true, progress: ProgressIndicator? = null, callable: () -> T): NonBlockingReadAction<T> {
            var action = ReadAction.nonBlocking(callable).expireWith(project)
            if (progress != null) {
                action = action.wrapProgress(progress)
            }
            if (requireSmartMode) {
                action = action.inSmartMode(project)
            }
            return action
        }
    }

    private val m_envs = ConcurrentHashMap<String, JavaEnvironmentWithIdea>()

    override fun close() {
        super.close()
        m_envs.values.forEach(this::closeSafe)
        m_envs.clear()
    }

    private fun closeSafe(closable: AutoCloseable) {
        try {
            closable.close()
        } catch (e: Exception) {
            SdkLog.info("Unable to close java environment.", e)
        }
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
        return ideaEnv.module.moduleDirPath()
    }

    fun toScoutJavaEnvironment(module: Module?): IJavaEnvironment? = module
        ?.takeIf { it.isJavaModule() }
        ?.let { getOrCreateEnv(it) }
        ?.wrap()

    fun findClasspathEntry(classpathRoot: VirtualFile?): IClasspathEntry? = classpathRoot
        ?.let { ProjectRootManager.getInstance(project).fileIndex.getModuleForFile(it) }
        ?.let { toScoutJavaEnvironment(it) }
        ?.let { findClasspathEntry(classpathRoot, it) }

    protected fun findClasspathEntry(file: VirtualFile, env: IJavaEnvironment) = file.resolveLocalPath()?.let { filePath ->
        env.sourceFolders()
            .filter { it.path().startsWith(filePath) }
            .findAny()
            .orElse(null)
    }

    protected fun getOrCreateEnv(module: Module): JavaEnvironmentWithIdea = m_envs.computeIfAbsent(module.name) { createNewJavaEnvironmentFor(module) }

    protected fun createNewJavaEnvironmentFor(module: Module): JavaEnvironmentWithIdea = initNewJavaEnvironment(JavaEnvironmentWithIdea(module))

    override fun runGenerator(generator: ISourceGenerator<ISourceBuilder<*>>, context: IJavaEnvironment, filePath: Path): StringBuilder {
        val env = context.unwrap() as JavaEnvironmentWithIdea
        val props = PropertySupport(2)
        props.setProperty(IBuilderContext.PROPERTY_JAVA_MODULE, env.module)
        props.setProperty(IBuilderContext.PROPERTY_TARGET_PATH, filePath)

        // must be \n! see https://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/modifying_psi.html
        // do not use CodeStyle.getSettings(project).lineSeparator because the CompilationUnitWriter uses createFileFromText
        val ctx = BuilderContext("\n", props)
        val builder = MemorySourceBuilder.create(JavaBuilderContext(ctx, context))
        generator.generate(builder)
        return builder.source()
    }

    override fun doWriteResource(content: CharSequence, filePath: Path, progress: IProgress?, sync: Boolean): IFuture<Void> {
        // No need for async support here as the FileWriter is just registered and no actual action is performed yet
        // The real write is done on transaction commit
        TransactionManager.current().register(FileWriter(filePath, content, project))
        return SdkFuture.completed(null)
    }

    override fun deleteIfExists(file: Path) {
        val toDelete = file.toVirtualFile() ?: return
        TransactionManager.current().register(object : TransactionMember {
            override fun file() = file

            override fun commit(progress: IdeaProgress): Boolean {
                toDelete.delete(this@IdeaEnvironment)
                return true
            }
        })
    }

    override fun doWriteCompilationUnit(code: CharSequence, cuInfo: CompilationUnitInfoWithClasspath, progress: IProgress?, sync: Boolean): IFuture<IType?> {
        val writer = CompilationUnitWriteOperation(project, code, cuInfo)
        val supplier = { registerCompilationUnit(writer.formattedSource /* use formatted source! better performance in array equals of JavaEnvironmentWithEcj.isLoadedInCompiler */, cuInfo) }
        if (sync) {
            return writer.run(progress.toIdea(), supplier)
        }
        return writer.schedule(supplier)
    }

    override fun javaEnvironments(): MutableCollection<out JavaEnvironmentSpi> = m_envs.values

    override fun runJsSourceGenerator(generator: IJsSourceGenerator<IJsSourceBuilder<*>>): String {
        val builder = JsSourceBuilder.create()
            .withLineSeparator("\n")
        generator.generate(builder)
        return builder.source()
    }

    override fun doWriteJsSource(source: CharSequence, filePath: Path, progress: IProgress?, sync: Boolean): IFuture<Void?> {
        val writer = JsWriteOperation(project, source, filePath)
        if (sync) {
            return writer.run(progress.toIdea())
        }
        return writer.schedule()
    }
}
