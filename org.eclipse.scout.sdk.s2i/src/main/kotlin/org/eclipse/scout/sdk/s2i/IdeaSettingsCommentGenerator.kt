package org.eclipse.scout.sdk.s2i

import com.intellij.copyright.CopyrightManager
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.Disposable
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.maddyhome.idea.copyright.pattern.EntityUtil
import com.maddyhome.idea.copyright.pattern.VelocityHelper
import com.maddyhome.idea.copyright.util.FileTypeUtil
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder
import org.eclipse.scout.sdk.core.builder.java.comment.ICommentBuilder
import org.eclipse.scout.sdk.core.builder.java.comment.IDefaultElementCommentGeneratorSpi
import org.eclipse.scout.sdk.core.builder.java.comment.JavaElementCommentBuilder
import org.eclipse.scout.sdk.core.generator.ISourceGenerator
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator
import org.eclipse.scout.sdk.core.s.ISdkProperties
import org.eclipse.scout.sdk.core.util.CoreUtils
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment
import java.nio.file.Path
import java.util.*

open class IdeaSettingsCommentGenerator : IDefaultElementCommentGeneratorSpi, StartupActivity, DumbAware, Disposable {

    private var m_previousCommentGenerator: IDefaultElementCommentGeneratorSpi? = null

    /**
     * Executed on [Project] open
     */
    override fun runActivity(project: Project) {
        val existingCommentGenerator = JavaElementCommentBuilder.getCommentGeneratorSpi()
        if (existingCommentGenerator == this) {
            return
        }

        Disposer.register(project, this)
        m_previousCommentGenerator = existingCommentGenerator
        JavaElementCommentBuilder.setCommentGeneratorSpi(this)
    }

    /**
     * Executed on [Project] close
     */
    override fun dispose() {
        JavaElementCommentBuilder.setCommentGeneratorSpi(m_previousCommentGenerator)
        m_previousCommentGenerator = null
    }

    override fun createCompilationUnitComment(target: ICompilationUnitGenerator<*>): ISourceGenerator<ICommentBuilder<*>> {
        return ISourceGenerator {
            val module: Module = it.context().properties().getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, Module::class.java) ?: return@ISourceGenerator
            val project = module.project
            val fileTemplateManager = FileTemplateManager.getInstance(project)
            val copyrightManager = CopyrightManager.getInstance(project)
            val psiManager = PsiManager.getInstance(project)

            val targetPath: Path? = it.context().properties().getProperty(ISdkProperties.CONTEXT_PROPERTY_TARGET_PATH, Path::class.java)
            val copyrightNotice = computeCopyrightNoticeFor(targetPath, psiManager, module, copyrightManager)
            val fileHeader = computeFileHeaderTemplateFor(target, fileTemplateManager)

            val cuComment = StringBuilder()
            Strings.notBlank(copyrightNotice).ifPresent { cuComment.append(copyrightNotice) }
            Strings.notBlank(fileHeader).ifPresent {
                if (cuComment.isNotBlank()) {
                    cuComment.append("\n<p>\n")
                }
                cuComment.append(fileHeader)
            }

            if (cuComment.isNotBlank()) {
                val fileType = FileTypeUtil.getInstance().getFileTypeByName(JavaLanguage.INSTANCE.id)
                val opts = copyrightManager.options.getMergedOptions(fileType.name)
                val cmt = FileTypeUtil.buildComment(fileType, cuComment.toString(), opts)
                val commentText = StringUtil.convertLineSeparators(cmt)
                it.append(commentText)
            }
        }
    }

    protected fun computeFileHeaderTemplateFor(target: ICompilationUnitGenerator<*>, fileTemplateManager: FileTemplateManager): String? {
        val pat = fileTemplateManager.getPattern(FileTemplateManager.FILE_HEADER_TEMPLATE_NAME) ?: return null
        val pckName = target.packageName().orElse("")
        val props = defaultProperties(fileTemplateManager)
        props[FileTemplate.ATTRIBUTE_DIR_PATH] = pckName.replace('.', '/')
        props[FileTemplate.ATTRIBUTE_PACKAGE_NAME] = pckName
        props[FileTemplate.ATTRIBUTE_FILE_NAME] = target.fileName().orElse("")
        props[FileTemplate.ATTRIBUTE_NAME] = target.elementName().orElse("")
        return pat.getText(props)
    }

    protected fun computeCopyrightNoticeFor(path: Path?, psiManager: PsiManager, module: Module, copyrightManager: CopyrightManager): String? {
        if (path == null) {
            return null
        }

        val project = module.project
        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(path.toFile()) ?: return null
        val psiFile = IdeaEnvironment.computeInReadAction(project) { psiManager.findFile(virtualFile) } ?: return null

        val raw = copyrightManager.getCopyrightOptions(psiFile)?.notice ?: return null
        return VelocityHelper.evaluate(psiFile, project, module, EntityUtil.decode(raw))
    }

    override fun createTypeComment(target: ITypeGenerator<*>): ISourceGenerator<ICommentBuilder<*>> {
        // currently IntelliJ only has templates for types including type declaration and body
        return ISourceGenerator.empty()
    }

    override fun createMethodComment(target: IMethodGenerator<*, out IMethodBodyBuilder<*>>): ISourceGenerator<ICommentBuilder<*>> {
        // currently IntelliJ only has templates for method bodies. See JavaTemplateUtil
        return ISourceGenerator.empty()
    }

    override fun createGetterMethodComment(target: IMethodGenerator<*, out IMethodBodyBuilder<*>>): ISourceGenerator<ICommentBuilder<*>> {
        // currently IntelliJ only has templates for method bodies. See JavaTemplateUtil
        return ISourceGenerator.empty()
    }

    override fun createSetterMethodComment(target: IMethodGenerator<*, out IMethodBodyBuilder<*>>): ISourceGenerator<ICommentBuilder<*>> {
        // currently IntelliJ only has templates for method bodies. See JavaTemplateUtil
        return ISourceGenerator.empty()
    }

    override fun createFieldComment(target: IFieldGenerator<*>): ISourceGenerator<ICommentBuilder<*>> {
        // currently IntelliJ only has templates for fields. See JavaTemplateUtil
        return ISourceGenerator.empty()
    }

    protected fun defaultProperties(fileTemplateManager: FileTemplateManager): Properties {
        val props = fileTemplateManager.defaultProperties
        props["USER"] = CoreUtils.getUsername()
        return props
    }
}
