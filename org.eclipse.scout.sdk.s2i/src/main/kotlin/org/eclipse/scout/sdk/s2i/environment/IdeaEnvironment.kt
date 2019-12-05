package org.eclipse.scout.sdk.s2i.environment

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.scout.sdk.core.builder.BuilderContext
import org.eclipse.scout.sdk.core.builder.ISourceBuilder
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext
import org.eclipse.scout.sdk.core.generator.ISourceGenerator
import org.eclipse.scout.sdk.core.generator.compilationunit.CompilationUnitPath
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.model.api.IType
import org.eclipse.scout.sdk.core.s.ISdkProperties
import org.eclipse.scout.sdk.core.s.environment.Future
import org.eclipse.scout.sdk.core.s.environment.IEnvironment
import org.eclipse.scout.sdk.core.s.environment.IFuture
import org.eclipse.scout.sdk.core.s.environment.IProgress
import org.eclipse.scout.sdk.core.util.*
import org.eclipse.scout.sdk.core.util.CoreUtils.toStringIfOverwritten
import org.eclipse.scout.sdk.core.util.Ensure.newFail
import org.eclipse.scout.sdk.s2i.*
import org.eclipse.scout.sdk.s2i.environment.model.JavaEnvironmentWithIdea
import java.nio.file.Path
import java.util.*


open class IdeaEnvironment private constructor(val project: Project) : IEnvironment, AutoCloseable {

    companion object Factory {

        fun createUnsafeFor(project: Project, registerCloseCallback: (IdeaEnvironment) -> Unit): IdeaEnvironment {
            val ideaEnvironment = IdeaEnvironment(project)
            registerCloseCallback.invoke(ideaEnvironment)
            return ideaEnvironment
        }

        fun <T> callInIdeaEnvironmentSync(project: Project, progressIndicator: IdeaProgress, task: (IdeaEnvironment, IdeaProgress) -> T): T =
                IdeaEnvironment(project).use {
                    return task.invoke(it, progressIndicator)
                }

        fun <T> callInIdeaEnvironment(project: Project, title: String, task: (IdeaEnvironment, IdeaProgress) -> T): IFuture<T> {
            val result = FinalValue<T>()
            val name = Strings.notBlank(title).orElseGet { toStringIfOverwritten(task).orElse("Unnamed Task: $task") }
            val job = OperationTask(name, project) { pr ->
                callInIdeaEnvironmentSync(project, pr) { e, p ->
                    result.setIfAbsent(task.invoke(e, p))
                }
            }
            return job.schedule({ result.get() })
        }

        fun <T> computeInReadAction(project: Project, callable: () -> T): T =
                DumbService.getInstance(project).runReadActionInSmartMode(callable)

        fun toIdeaProgress(progress: IProgress?): IdeaProgress = progress?.toIdea() ?: IdeaProgress(null)
    }

    private val m_envs = HashMap<String, JavaEnvironmentWithIdea>()

    override fun close() {
        m_envs.values.forEach(AutoCloseable::close)
        m_envs.clear()
    }

    override fun findJavaEnvironment(root: Path?): Optional<IJavaEnvironment> =
            Optional.ofNullable(
                    root?.toVirtualFile()
                            ?.containingModuleOf(project)
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

    protected fun findClasspathEntry(file: VirtualFile, env: IJavaEnvironment): IClasspathEntry? =
            env.sourceFolders()
                    .filter { it.path().startsWith(file.toNioPath()) }
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
        props.setProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, env.module)
        props.setProperty(ISdkProperties.CONTEXT_PROPERTY_TARGET_PATH, filePath)

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
        val writer = FileWriter(filePath, content, project)
        TransactionManager.current().register(writer)
        return Future.completed(null)
    }

    protected fun doWriteCompilationUnit(generator: ICompilationUnitGenerator<*>, targetFolder: IClasspathEntry, progress: IdeaProgress, sync: Boolean): IFuture<IType?> {
        Ensure.isTrue(targetFolder.isSourceFolder)
        val pck = generator.packageName().orElse("")
        val name = generator.elementName().orElseThrow { newFail("File name missing in generator") }
        val path = CompilationUnitPath(generator, targetFolder)
        val code = createResource(generator, targetFolder.javaEnvironment(), path.targetFile())
        val javaEnv = targetFolder.javaEnvironment()
        val writer = CompilationUnitWriter(project, code, path)
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
