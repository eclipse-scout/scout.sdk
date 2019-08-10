package org.eclipse.scout.sdk.s2i

import com.intellij.copyright.CopyrightManager
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
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

open class IdeaSettingsCommentGenerator(val project: Project) : IDefaultElementCommentGeneratorSpi, ProjectComponent {

    private var m_fileTemplateManager: FileTemplateManager? = null
    private var m_origCommentGenerator: IDefaultElementCommentGeneratorSpi? = null
    private var m_copyrightManager: CopyrightManager? = null
    private var m_psiManager: PsiManager? = null

    override fun disposeComponent() {
        JavaElementCommentBuilder.setCommentGeneratorSpi(m_origCommentGenerator)
        m_fileTemplateManager = null
        m_copyrightManager = null
        m_origCommentGenerator = null
        m_psiManager = null
    }

    override fun initComponent() {
        m_origCommentGenerator = JavaElementCommentBuilder.getCommentGeneratorSpi()
        m_fileTemplateManager = FileTemplateManager.getInstance(project)
        m_copyrightManager = CopyrightManager.getInstance(project)
        m_psiManager = PsiManager.getInstance(project)
        JavaElementCommentBuilder.setCommentGeneratorSpi(this)
    }

    override fun createCompilationUnitComment(target: ICompilationUnitGenerator<*>): ISourceGenerator<ICommentBuilder<*>> {
        return ISourceGenerator {
            val targetPath: Path? = it.context().properties().getProperty(ISdkProperties.CONTEXT_PROPERTY_TARGET_PATH, Path::class.java)
            val copyrightNotice = computeCopyrightNoticeFor(targetPath)
            val fileHeader = computeFileHeaderTemplateFor(target)
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
                val opts = CopyrightManager.getInstance(project).options.getMergedOptions(fileType.name)
                val cmt = FileTypeUtil.buildComment(fileType, cuComment.toString(), opts)
                val commentText = StringUtil.convertLineSeparators(cmt)
                it.append(commentText)
            }
        }
    }

    protected fun computeFileHeaderTemplateFor(target: ICompilationUnitGenerator<*>): String? {
        val pat = m_fileTemplateManager?.getPattern(FileTemplateManager.FILE_HEADER_TEMPLATE_NAME)
                ?: return null
        val pckName = target.packageName().orElse("")
        val props = defaultProperties()
        props[FileTemplate.ATTRIBUTE_DIR_PATH] = pckName.replace('.', '/')
        props[FileTemplate.ATTRIBUTE_PACKAGE_NAME] = pckName
        props[FileTemplate.ATTRIBUTE_FILE_NAME] = target.fileName().orElse("")
        props[FileTemplate.ATTRIBUTE_NAME] = target.elementName().orElse("")
        return pat.getText(props)
    }

    protected fun computeCopyrightNoticeFor(path: Path?): String? {
        if (path == null) {
            return null
        }

        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(path.toFile()) ?: return null
        val psiFile = IdeaEnvironment.computeInReadAction(project) { m_psiManager?.findFile(virtualFile) }
                ?: return null
        val module = psiFile.containingModule() ?: return null
        val raw = m_copyrightManager?.getCopyrightOptions(psiFile)?.notice ?: return null
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

    protected fun defaultProperties(): Properties {
        val props = m_fileTemplateManager?.defaultProperties ?: return Properties()
        props["USER"] = CoreUtils.getUsername()
        return props
    }
}
