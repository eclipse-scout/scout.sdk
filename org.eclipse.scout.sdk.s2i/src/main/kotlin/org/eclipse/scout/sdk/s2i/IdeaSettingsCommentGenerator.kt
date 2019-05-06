package org.eclipse.scout.sdk.s2i

import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder
import org.eclipse.scout.sdk.core.builder.java.comment.ICommentBuilder
import org.eclipse.scout.sdk.core.builder.java.comment.IDefaultElementCommentGeneratorSpi
import org.eclipse.scout.sdk.core.builder.java.comment.JavaElementCommentBuilder
import org.eclipse.scout.sdk.core.generator.ISourceGenerator
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator
import org.eclipse.scout.sdk.core.util.CoreUtils
import org.eclipse.scout.sdk.core.util.Strings
import java.util.*

open class IdeaSettingsCommentGenerator(val project: Project) : IDefaultElementCommentGeneratorSpi, ProjectComponent {

    private var m_fileTemplateManager: FileTemplateManager? = null
    private var m_origCommentGenerator: IDefaultElementCommentGeneratorSpi? = null

    override fun disposeComponent() {
        JavaElementCommentBuilder.setCommentGeneratorSpi(m_origCommentGenerator)
        m_fileTemplateManager = null
        m_origCommentGenerator = null
    }

    override fun initComponent() {
        m_origCommentGenerator = JavaElementCommentBuilder.getCommentGeneratorSpi()
        m_fileTemplateManager = FileTemplateManager.getInstance(project)
        JavaElementCommentBuilder.setCommentGeneratorSpi(this)
    }

    override fun createCompilationUnitComment(target: ICompilationUnitGenerator<*>): ISourceGenerator<ICommentBuilder<*>> {
        val pat = m_fileTemplateManager?.getPattern(FileTemplateManager.FILE_HEADER_TEMPLATE_NAME)
                ?: return ISourceGenerator.empty()

        return ISourceGenerator {
            val pckName = target.packageName().orElse("")
            val props = defaultProperties()
            props[FileTemplate.ATTRIBUTE_DIR_PATH] = pckName.replace('.', '/')
            props[FileTemplate.ATTRIBUTE_PACKAGE_NAME] = pckName
            props[FileTemplate.ATTRIBUTE_FILE_NAME] = target.fileName().orElse("")
            props[FileTemplate.ATTRIBUTE_NAME] = target.elementName().orElse("")
            appendComment(it, pat.getText(props))
        }
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

    protected fun appendComment(builder: ICommentBuilder<*>, comment: String?) {
        if (!Strings.hasText(comment)) {
            return
        }

        builder.append(comment)
        ensureEndsWithNewline(builder, comment!!)
    }

    protected fun defaultProperties(): Properties {
        val props = m_fileTemplateManager?.defaultProperties ?: return Properties()
        props["USER"] = CoreUtils.getUsername()
        return props
    }

    private fun ensureEndsWithNewline(b: ICommentBuilder<*>, comment: String) {
        if (comment.endsWith(b.context().lineDelimiter())) {
            return
        }
        b.nl()
    }
}