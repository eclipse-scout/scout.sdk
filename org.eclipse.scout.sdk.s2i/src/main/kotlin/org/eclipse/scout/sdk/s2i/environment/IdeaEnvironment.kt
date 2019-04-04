package org.eclipse.scout.sdk.s2i.environment

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import org.eclipse.scout.sdk.core.builder.BuilderContext
import org.eclipse.scout.sdk.core.builder.ISourceBuilder
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext
import org.eclipse.scout.sdk.core.generator.ISourceGenerator
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.environment.Future
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.util.*
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.s2i.containingModule
import org.eclipse.scout.sdk.s2i.environment.model.JavaEnvironmentWithIdea
import org.eclipse.scout.sdk.s2i.isJavaModule
import org.eclipse.scout.sdk.s2i.toIdea
import org.jetbrains.annotations.NotNull
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


open class IdeaEnvironment(val project: Project) : IEnvironment, AutoCloseable {

    companion object Factory {
        fun <T> callInIdeaEnvironment(task: (IdeaEnvironment, IdeaProgress) -> T, project: Project, @NotNull title: String): IFuture<T> {
            val result = FinalValue<T>()
            val name = Strings.notBlank(title).orElseGet { CoreUtils.toStringIfOverwritten(task).orElse("Unnamed") }
            val job = OperationTask({ p ->
                IdeaEnvironment(project).use {
                    result.setIfAbsent(task.invoke(it, p))
                }
            }, name, project)
            return job.schedule { result.get() }
        }

        fun <T> computeInReadAction(project: Project, callable: () -> T): T =
                DumbService.getInstance(project).runReadActionInSmartMode(callable)

        fun toIdeaProgress(progress: IProgress?): IdeaProgress = progress?.toIdea() ?: IdeaProgress(null)

        fun <T> computeInWriteAction(project: Project, callable: () -> T): T {
            val result = FinalValue<T>()
            TransactionGuard.getInstance().submitTransactionAndWait {
                // this is executed in the UI thread! keep short to prevent freezes!
                // write operations are only allowed in the UI thread
                // see http://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/general_threading_rules.html
                result.computeIfAbsent {
                    WriteAction.compute<T, RuntimeException> { computeInCommandProcessor(project, callable) }
                }
            }
            return result.get()
        }

        private fun <T> computeInCommandProcessor(project: Project, callable: () -> T): T {
            val result = FinalValue<T>()
            CommandProcessor.getInstance().executeCommand(project, { result.computeIfAbsent(callable) }, null, null)
            return result.get()
        }
    }

    private val m_envs = HashMap<String, JavaEnvironmentWithIdea>()

    override fun close() {
        m_envs.values.forEach(AutoCloseable::close)
        m_envs.clear()
    }

    override fun findJavaEnvironment(root: Path?): Optional<IJavaEnvironment> =
            Optional.ofNullable(
                    root?.toFile()
                            ?.let { LocalFileSystem.getInstance().findFileByIoFile(it) }
                            ?.takeIf { it.isValid }
                            ?.let { ProjectFileIndex.getInstance(project).getModuleForFile(it) }
                            ?.let { toScoutJavaEnvironment(it) })


    fun toScoutJavaEnvironment(module: Module?): IJavaEnvironment? =
            module
                    ?.takeIf { it.isJavaModule() }
                    ?.let { getOrCreateEnv(it) }
                    ?.wrap()

    fun findClasspathEntry(classpathRoot: VirtualFile?): IClasspathEntry? =
            classpathRoot
                    ?.let { ProjectRootManager.getInstance(project).fileIndex.getModuleForFile(it) }
                    ?.let { toScoutJavaEnvironment(it) }
                    ?.let { findClasspathEntry(classpathRoot, it) }

    protected fun findClasspathEntry(file: VirtualFile, env: IJavaEnvironment): IClasspathEntry? {
        val path = Paths.get(file.path)
        return env.sourceFolders()
                .filter { it.path().startsWith(path) }
                .findAny()
                .orElse(null)
    }

    protected fun getOrCreateEnv(module: Module): JavaEnvironmentWithIdea = m_envs.computeIfAbsent(module.name) { createNewJavaEnvironmentFor(module) }

    protected fun createNewJavaEnvironmentFor(module: Module): JavaEnvironmentWithIdea = JavaEnvironmentWithIdea(module)

    protected fun psiClassToScoutType(type: PsiClass, env: IJavaEnvironment): IType? {
        val fqn = computeInReadAction(type.project) { type.qualifiedName }
        return env.findType(fqn).orElse(null)
    }

    fun psiClassToScoutType(type: PsiClass): IType? {
        return type
                .containingModule()
                ?.let { toScoutJavaEnvironment(it) }
                ?.let { psiClassToScoutType(type, it) }
    }

    override fun writeCompilationUnit(generator: ICompilationUnitGenerator<*>, targetFolder: IClasspathEntry): IType? =
            writeCompilationUnit(generator, targetFolder, null)

    override fun writeCompilationUnit(generator: ICompilationUnitGenerator<*>, targetFolder: IClasspathEntry, progress: IProgress?): IType? =
            doWriteCompilationUnit(generator, targetFolder, toIdeaProgress(progress), true).result()

    override fun writeCompilationUnitAsync(generator: ICompilationUnitGenerator<*>, targetFolder: IClasspathEntry, progress: IProgress?): IFuture<IType?> =
            doWriteCompilationUnit(generator, targetFolder, toIdeaProgress(progress), false)

    fun createResource(generator: ISourceGenerator<ISourceBuilder<*>>, filePath: Path): StringBuilder =
            createResource(generator, findJavaEnvironment(filePath).orElseThrow { newFail("Cannot find Java environment for path '{}'.", filePath) })

    override fun createResource(generator: ISourceGenerator<ISourceBuilder<*>>, context: IJavaEnvironment): StringBuilder {
        val nl = CodeStyle.getSettings(project).lineSeparator
        val ctx = BuilderContext(nl, PropertySupport(0))
        val builder = MemorySourceBuilder(JavaBuilderContext(ctx, context))
        generator.generate(builder)
        return builder.source()
    }

    override fun writeResource(content: CharSequence, filePath: Path, progress: IProgress?) {
        doWriteResource(filePath, content, toIdeaProgress(progress), true).awaitDoneThrowingOnErrorOrCancel()
        return
    }

    override fun writeResource(generator: ISourceGenerator<ISourceBuilder<*>>, filePath: Path, progress: IProgress?) =
            writeResource(createResource(generator, filePath), filePath, progress)

    override fun writeResourceAsync(generator: ISourceGenerator<ISourceBuilder<*>>, filePath: Path, progress: IProgress?): IFuture<Void> =
            writeResourceAsync(createResource(generator, filePath), filePath, progress)

    override fun writeResourceAsync(content: CharSequence, filePath: Path, progress: IProgress?): IFuture<Void> =
            doWriteResource(filePath, content, toIdeaProgress(progress), false)

    protected fun doWriteResource(filePath: Path, content: CharSequence, progress: IdeaProgress, sync: Boolean): IFuture<Void> {
        val writer = FileWriter(filePath, content, project)
        if (sync) {
            writer.run(progress)
            return Future.completed(null)
        }
        return writer.schedule()
    }

    protected fun doWriteCompilationUnit(generator: ICompilationUnitGenerator<*>, targetFolder: IClasspathEntry, progress: IdeaProgress, sync: Boolean): IFuture<IType?> {
        Ensure.isTrue(targetFolder.isSourceFolder)
        val pck = generator.packageName().orElse("")
        val name = generator.elementName().orElseThrow { newFail("File name missing in generator") }
        val code = createResource(generator, targetFolder.javaEnvironment())
        val javaEnv = targetFolder.javaEnvironment()

        val writer = CompilationUnitWriter(project, code, pck, name, targetFolder.path())
        val supplier = lambda@{
            val formattedCode = writer.formattedCode ?: return@lambda null

            val reloadRequired = javaEnv.registerCompilationUnitOverride(pck, name + JavaTypes.JAVA_FILE_SUFFIX, formattedCode)
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