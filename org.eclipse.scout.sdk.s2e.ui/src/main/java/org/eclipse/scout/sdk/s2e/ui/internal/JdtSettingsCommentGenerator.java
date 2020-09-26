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
package org.eclipse.scout.sdk.s2e.ui.internal;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.manipulation.CodeTemplateContext;
import org.eclipse.jdt.internal.core.manipulation.CodeTemplateContextType;
import org.eclipse.jdt.internal.core.manipulation.StubUtility;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.GlobalTemplateVariables.User;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.ICommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IDefaultElementCommentGeneratorSpi;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.PropertySupport;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link JdtSettingsCommentGenerator}</h3>
 *
 * @since 3.10.0 2013-07-12
 */
@SuppressWarnings("pmd:NPathComplexity")
public class JdtSettingsCommentGenerator implements IDefaultElementCommentGeneratorSpi {
  private static final String UNDEFINED_VAR_VALUE = "undefined";

  private static final int METHOD_TYPE_NORMAL = 1 << 1;
  private static final int METHOD_TYPE_GETTER = 1 << 2;
  private static final int METHOD_TYPE_SETTER = 1 << 3;

  private static final TemplateVariableResolver USERNAME_RESOLVER = new UsernameResolver();

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createCompilationUnitComment(ICompilationUnitGenerator<?> target) {
    return b -> {
      PropertySupport builderCtx = b.context().properties();
      if (!isAutomaticallyAddComments(builderCtx)) {
        return;
      }
      IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
      Template template = getCodeTemplate(CodeTemplateContextType.FILECOMMENT_ID, ownerProject);
      if (template != null) {
        TemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, b.context().lineDelimiter());
        context.setVariable(CodeTemplateContextType.FILENAME, target.fileName().orElse(null));

        String packageName = target.packageName().orElse("");
        context.setVariable(CodeTemplateContextType.PACKAGENAME, packageName);

        if (JdtUtils.exists(ownerProject)) {
          context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
        }
        context.setVariable(CodeTemplateContextType.TYPENAME, JavaCore.removeJavaLikeExtension(target.elementName().orElse(null)));
        String comment = evaluateTemplate(context, template);
        if (comment != null) {
          b.append(comment);
          ensureEndsWithNewline(b, comment);
        }
      }
    };
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createTypeComment(ITypeGenerator<?> target) {
    return b -> {
      PropertySupport builderCtx = b.context().properties();
      if (!isAutomaticallyAddComments(builderCtx)) {
        return;
      }
      IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
      Template template = getCodeTemplate(CodeTemplateContextType.TYPECOMMENT_ID, ownerProject);
      if (template == null) {
        return;
      }
      TemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, b.context().lineDelimiter());
      context.setVariable(CodeTemplateContextType.FILENAME, UNDEFINED_VAR_VALUE);
      context.setVariable(CodeTemplateContextType.PACKAGENAME, JavaTypes.qualifier(target.fullyQualifiedName()));
      if (JdtUtils.exists(ownerProject)) {
        context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
      }
      context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, target.getDeclaringFullyQualifiedName().orElse(null));
      context.setVariable(CodeTemplateContextType.TYPENAME, target.elementName().orElse(null));

      evaluateTemplate(context, template, b, emptyList(), emptyList(), null);
    };
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createMethodComment(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> target) {
    return createMethodCommentInternal(target, METHOD_TYPE_NORMAL);
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createGetterMethodComment(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> target) {
    return createMethodCommentInternal(target, METHOD_TYPE_GETTER);
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createSetterMethodComment(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> target) {
    return createMethodCommentInternal(target, METHOD_TYPE_SETTER);
  }

  private static void ensureEndsWithNewline(ICommentBuilder<?> b, String comment) {
    if (comment.endsWith(b.context().lineDelimiter())) {
      return;
    }
    b.nl();
  }

  private static ISourceGenerator<ICommentBuilder<?>> createMethodCommentInternal(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> target, int type) {
    return b -> {
      PropertySupport builderCtx = b.context().properties();
      if (!isAutomaticallyAddComments(builderCtx)) {
        return;
      }
      IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
      List<String> paramNames = target.parameters()
          .map(IMethodParameterGenerator::elementName)
          .flatMap(Optional::stream)
          .collect(toList());
      List<String> exceptionNames = target.exceptions()
          .collect(toList());
      Optional<String> returnTypeName = target.returnType();

      String fieldTypeSimpleName = UNDEFINED_VAR_VALUE;
      String templateName;
      switch (type) {
        case METHOD_TYPE_GETTER:
          templateName = CodeTemplateContextType.GETTERCOMMENT_ID;
          if (returnTypeName.isPresent()) {
            fieldTypeSimpleName = JavaTypes.simpleName(returnTypeName.get());
          }
          break;
        case METHOD_TYPE_SETTER:
          templateName = CodeTemplateContextType.SETTERCOMMENT_ID;
          Optional<String> firstParam = target.parameters().findAny()
              .flatMap(IMethodParameterGenerator::dataType);
          if (firstParam.isPresent()) {
            fieldTypeSimpleName = JavaTypes.simpleName(firstParam.get());
          }
          break;
        default:
          templateName = returnTypeName
              .map(s -> CodeTemplateContextType.METHODCOMMENT_ID)
              .orElse(CodeTemplateContextType.CONSTRUCTORCOMMENT_ID);
          break;
      }

      Template template = getCodeTemplate(templateName, ownerProject);
      if (template == null) {
        return;
      }
      TemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, b.context().lineDelimiter());
      String getterSetterName = UNDEFINED_VAR_VALUE;
      Matcher matcher = PropertyBean.BEAN_METHOD_NAME.matcher(target.elementName().orElse(""));
      if (matcher.find()) {
        getterSetterName = matcher.group(2);
      }
      if (JdtUtils.exists(ownerProject)) {
        context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
      }
      context.setVariable(CodeTemplateContextType.FILENAME, UNDEFINED_VAR_VALUE);
      context.setVariable(CodeTemplateContextType.PACKAGENAME, UNDEFINED_VAR_VALUE);
      context.setVariable(CodeTemplateContextType.ENCLOSING_METHOD, target.elementName().orElse(null));
      context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, UNDEFINED_VAR_VALUE);
      context.setVariable(CodeTemplateContextType.FIELD, getterSetterName);
      context.setVariable(CodeTemplateContextType.FIELD_TYPE, fieldTypeSimpleName);
      context.setVariable(CodeTemplateContextType.BARE_FIELD_NAME, getterSetterName);
      if (!paramNames.isEmpty()) {
        context.setVariable(CodeTemplateContextType.PARAM, paramNames.get(0));
      }
      else {
        context.setVariable(CodeTemplateContextType.PARAM, UNDEFINED_VAR_VALUE);
      }
      returnTypeName.ifPresent(s -> context.setVariable(CodeTemplateContextType.RETURN_TYPE, s));

      evaluateTemplate(context, template, b, paramNames, exceptionNames, returnTypeName.orElse(null));
    };
  }

  private static void evaluateTemplate(TemplateContext context, Template template, ICommentBuilder<?> b, Iterable<String> paramNames, Iterable<String> exceptionNames, String returnType) {
    try {
      TemplateBuffer buffer = context.evaluate(template);
      if (buffer == null) {
        return;
      }

      String str = buffer.getString();
      if (Strings.isBlank(str)) {
        return;
      }
      TemplateVariable position = findVariable(buffer, CodeTemplateContextType.TAGS); // look if Javadoc tags have to be added
      if (position == null) {
        b.append(str);
        ensureEndsWithNewline(b, str);
        return;
      }

      IDocument document = new Document(str);
      int[] tagOffsets = position.getOffsets();
      for (int i = tagOffsets.length - 1; i >= 0; i--) { // from last to first
        insertTag(document, tagOffsets[i], position.getLength(), paramNames, exceptionNames, returnType, emptyList(), false, b.context().lineDelimiter());
      }
      String comment = document.get();
      if (comment != null) {
        b.append(comment);
        ensureEndsWithNewline(b, comment);
      }
    }
    catch (BadLocationException | TemplateException e) {
      throw new SdkException(e);
    }
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createFieldComment(IFieldGenerator<?> target) {
    return b -> {
      PropertySupport builderCtx = b.context().properties();
      if (!isAutomaticallyAddComments(builderCtx)) {
        return;
      }
      IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
      Template template = getCodeTemplate(CodeTemplateContextType.FIELDCOMMENT_ID, ownerProject);
      if (template != null) {
        TemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, b.context().lineDelimiter());
        context.setVariable(CodeTemplateContextType.FIELD_TYPE, JavaTypes.simpleName(target.dataType().orElse(null)));
        context.setVariable(CodeTemplateContextType.FIELD, target.elementName().orElse(null));
        if (JdtUtils.exists(ownerProject)) {
          context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
        }
        context.setVariable(CodeTemplateContextType.PACKAGENAME, UNDEFINED_VAR_VALUE);
        context.setVariable(CodeTemplateContextType.FILENAME, UNDEFINED_VAR_VALUE);
        String comment = evaluateTemplate(context, template);
        if (comment != null) {
          b.append(comment);
          ensureEndsWithNewline(b, comment);
        }
      }
    };
  }

  private static boolean isAutomaticallyAddComments(PropertySupport map) {
    if (map == null) {
      return false;
    }
    IJavaProject jp = map.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
    if (jp == null) {
      return false;
    }

    IScopeContext[] contexts = {new ProjectScope(jp.getProject()), InstanceScope.INSTANCE, DefaultScope.INSTANCE};
    for (IScopeContext context : contexts) {
      IEclipsePreferences node = context.getNode(JavaUI.ID_PLUGIN);
      if (node != null) {
        String val = node.get(PreferenceConstants.CODEGEN_ADD_COMMENTS, null);
        return "true".equals(val);
      }
    }
    return true;
  }

  private static String evaluateTemplate(TemplateContext context, Template template) {
    // replace the user name resolver with our own to ensure we can respect the scout specific user names.
    Iterator<TemplateVariableResolver> resolvers = context.getContextType().resolvers();
    while (resolvers.hasNext()) {
      TemplateVariableResolver resolver = resolvers.next();
      if (resolver instanceof User) {
        context.getContextType().removeResolver(resolver); // remove the JDT resolver
        context.getContextType().addResolver(USERNAME_RESOLVER); // add our own
        break;
      }
    }

    TemplateBuffer buffer;
    try {
      buffer = context.evaluate(template);
    }
    catch (BadLocationException | TemplateException e) {
      throw new SdkException(e);
    }
    if (buffer == null) {
      return null;
    }
    String str = buffer.getString();

    if (Strings.isBlank(str)) {
      return null;
    }
    return str;
  }

  private static Template getCodeTemplate(String id, IJavaProject project) {
    return StubUtility.getCodeTemplate(id, project);
  }

  private static TemplateVariable findVariable(TemplateBuffer buffer, String variable) {
    TemplateVariable[] positions = buffer.getVariables();
    for (TemplateVariable curr : positions) {
      if (variable.equals(curr.getType())) {
        return curr;
      }
    }
    return null;
  }

  @SuppressWarnings({"squid:S00107", "pmd:NPathComplexity"})
  private static void insertTag(IDocument textBuffer, int offset, int length, Iterable<String> paramNames, Iterable<String> exceptionNames,
      String returnType, Iterable<String> typeParameterNames, boolean isDeprecated, String lineDelimiter) throws BadLocationException {
    IRegion region = textBuffer.getLineInformationOfOffset(offset);
    if (region == null) {
      return;
    }
    String lineStart = textBuffer.get(region.getOffset(), offset - region.getOffset());

    StringBuilder buf = new StringBuilder();
    for (String typeParameterName : typeParameterNames) {
      if (buf.length() > 0) {
        buf.append(lineDelimiter).append(lineStart);
      }
      buf.append("@param ").append(JavaTypes.C_GENERIC_START).append(typeParameterName).append(JavaTypes.C_GENERIC_END);
    }
    for (String paramName : paramNames) {
      if (buf.length() > 0) {
        buf.append(lineDelimiter).append(lineStart);
      }
      buf.append("@param ").append(paramName);
    }
    if (returnType != null && !"void".equals(returnType)) {
      if (buf.length() > 0) {
        buf.append(lineDelimiter).append(lineStart);
      }
      buf.append("@return");
    }
    if (exceptionNames != null) {
      for (String exceptionName : exceptionNames) {
        if (buf.length() > 0) {
          buf.append(lineDelimiter).append(lineStart);
        }
        buf.append("@throws ").append(exceptionName);
      }
    }
    if (isDeprecated) {
      if (buf.length() > 0) {
        buf.append(lineDelimiter).append(lineStart);
      }
      buf.append("@deprecated");
    }
    if (buf.length() == 0 && isAllCommentWhitespace(lineStart)) {
      int prevLine = textBuffer.getLineOfOffset(offset) - 1;
      if (prevLine > 0) {
        IRegion prevRegion = textBuffer.getLineInformation(prevLine);
        int prevLineEnd = prevRegion.getOffset() + prevRegion.getLength();
        // clear full line
        textBuffer.replace(prevLineEnd, offset + length - prevLineEnd, "");
        return;
      }
    }
    textBuffer.replace(offset, length, buf.toString());
  }

  private static boolean isAllCommentWhitespace(CharSequence lineStart) {
    for (int i = 0; i < lineStart.length(); i++) {
      char ch = lineStart.charAt(i);
      if (!Character.isWhitespace(ch) && ch != '*') {
        return false;
      }
    }
    return true;
  }

  private static final class UsernameResolver extends User {
    @Override
    protected String resolve(TemplateContext context) {
      return CoreUtils.getUsername();
    }
  }
}
