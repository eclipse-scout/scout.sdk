/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContext;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.ProjectTemplateStore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.ICommentSourceBuilderSpi;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.IMethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;

/**
 * <h3>{@link JavaElementCommentBuilderService}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 2013-07-12
 */
public class JdtSettingsCommentSourceBuilderDelegate implements ICommentSourceBuilderSpi {
  private static final String[] EMPTY = new String[0];
  private static final String UNDEFINED_VAR_VALUE = "undefined";

  private static final int METHOD_TYPE_NORMAL = 1 << 1;
  private static final int METHOD_TYPE_GETTER = 1 << 2;
  private static final int METHOD_TYPE_SETTER = 1 << 3;

  private static final UsernameResolver USERNAME_RESOLVER = new UsernameResolver();

  @Override
  public ISourceBuilder createCompilationUnitComment(final ICompilationUnitSourceBuilder target) {
    return new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap builderCtx, IImportValidator validator) {
        if (!isAutomaticallyAddComments(builderCtx)) {
          return;
        }
        IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
        Template template = getCodeTemplate(CodeTemplateContextType.FILECOMMENT_ID, ownerProject);
        if (template != null) {
          CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
          context.setVariable(CodeTemplateContextType.FILENAME, target.getElementName());

          String packageName = target.getPackageName();
          if (packageName == null) {
            packageName = "";
          }
          context.setVariable(CodeTemplateContextType.PACKAGENAME, packageName);

          context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
          context.setVariable(CodeTemplateContextType.TYPENAME, JavaCore.removeJavaLikeExtension(target.getElementName()));
          String comment = evaluateTemplate(context, template);
          if (comment != null) {
            source.append(comment);
          }
        }
      }
    };
  }

  @Override
  public ISourceBuilder createTypeComment(final ITypeSourceBuilder target) {
    return new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap builderCtx, IImportValidator validator) {
        if (!isAutomaticallyAddComments(builderCtx)) {
          return;
        }
        IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
        Template template = getCodeTemplate(CodeTemplateContextType.TYPECOMMENT_ID, ownerProject);
        if (template == null) {
          return;
        }
        CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
        context.setVariable(CodeTemplateContextType.FILENAME, UNDEFINED_VAR_VALUE);
        context.setVariable(CodeTemplateContextType.PACKAGENAME, Signature.getQualifier(target.getFullyQualifiedName()));
        context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
        context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, Signature.getQualifier(target.getElementName()));
        context.setVariable(CodeTemplateContextType.TYPENAME, Signature.getSimpleName(target.getElementName()));

        TemplateBuffer buffer;
        try {
          buffer = context.evaluate(template);
        }
        catch (BadLocationException e) {
          throw new SdkException(e);
        }
        catch (TemplateException e) {
          throw new SdkException(e);
        }
        String str = buffer.getString();
        if (Strings.containsOnlyWhitespaces(str)) {
          return;
        }

        TemplateVariable position = findVariable(buffer, CodeTemplateContextType.TAGS); // look if Javadoc tags have to be added
        if (position == null) {
          if (str != null) {
            source.append(str);
          }
          return;
        }

        IDocument document = new Document(str);
        int[] tagOffsets = position.getOffsets();
        for (int i = tagOffsets.length - 1; i >= 0; i--) { // from last to first
          try {
            insertTag(document, tagOffsets[i], position.getLength(), EMPTY, EMPTY, null, EMPTY, false, lineDelimiter);
          }
          catch (BadLocationException e) {
            throw new SdkException(e);
          }
        }

        String comment = document.get();
        if (comment != null) {
          source.append(comment);
        }
      }
    };
  }

  @Override
  public ISourceBuilder createOverrideMethodComment(final IMethodSourceBuilder target, final String interfaceFqn) {
    return new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap builderCtx, IImportValidator validator) {
        if (!isAutomaticallyAddComments(builderCtx)) {
          return;
        }
        IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
        Template template = getCodeTemplate(CodeTemplateContextType.OVERRIDECOMMENT_ID, ownerProject);
        if (template != null) {
          CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
          context.setVariable(CodeTemplateContextType.PACKAGENAME, UNDEFINED_VAR_VALUE);
          context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
          context.setVariable(CodeTemplateContextType.FILENAME, UNDEFINED_VAR_VALUE);
          context.setVariable(CodeTemplateContextType.ENCLOSING_METHOD, target.getElementName());
          context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, UNDEFINED_VAR_VALUE);

          // @see
          StringBuilder seeBuilder = new StringBuilder("@see ");
          seeBuilder.append(interfaceFqn).append('#').append(target.getElementName()).append('(');
          Iterator<IMethodParameterSourceBuilder> parameterIterator = target.getParameters().iterator();
          if (parameterIterator.hasNext()) {
            seeBuilder.append(SignatureUtils.toFullyQualifiedName(parameterIterator.next().getDataTypeSignature()));
            while (parameterIterator.hasNext()) {
              seeBuilder.append(", ").append(SignatureUtils.toFullyQualifiedName(parameterIterator.next().getDataTypeSignature()));
            }
          }
          seeBuilder.append(')');
          context.setVariable(CodeTemplateContextType.SEE_TO_OVERRIDDEN_TAG, seeBuilder.toString());
          String comment = evaluateTemplate(context, template);
          if (comment != null) {
            source.append(comment);
          }
        }
      }
    };
  }

  @Override
  public ISourceBuilder createMethodComment(IMethodSourceBuilder target) {
    return createMethodCommentInternal(target, METHOD_TYPE_NORMAL);
  }

  @Override
  public ISourceBuilder createGetterMethodComment(IMethodSourceBuilder target) {
    return createMethodCommentInternal(target, METHOD_TYPE_GETTER);
  }

  @Override
  public ISourceBuilder createSetterMethodComment(IMethodSourceBuilder target) {
    return createMethodCommentInternal(target, METHOD_TYPE_SETTER);
  }

  @SuppressWarnings("pmd:NPathComplexity")
  private static ISourceBuilder createMethodCommentInternal(final IMethodSourceBuilder target, final int type) {
    return new ISourceBuilder() {
      @Override
      @SuppressWarnings("pmd:NPathComplexity")
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap builderCtx, IImportValidator validator) {
        if (!isAutomaticallyAddComments(builderCtx)) {
          return;
        }
        IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
        List<IMethodParameterSourceBuilder> parameters = target.getParameters();
        String[] paramSignatures = new String[parameters.size()];
        String[] paramNames = new String[parameters.size()];
        int j = 0;
        for (IMethodParameterSourceBuilder param : parameters) {
          paramSignatures[j] = param.getDataTypeSignature();
          paramNames[j] = param.getElementName();
          j++;
        }

        List<String> exceptionSignatures = target.getExceptionSignatures();
        String returnTypeSignature = target.getReturnTypeSignature();

        String fieldTypeSimpleName = UNDEFINED_VAR_VALUE;
        String templateName = null;
        switch (type) {
          case METHOD_TYPE_GETTER:
            templateName = CodeTemplateContextType.GETTERCOMMENT_ID;
            fieldTypeSimpleName = Signature.getSignatureSimpleName(target.getReturnTypeSignature());
            break;
          case METHOD_TYPE_SETTER:
            templateName = CodeTemplateContextType.SETTERCOMMENT_ID;
            if (target.getParameters().size() > 0) {
              fieldTypeSimpleName = Signature.getSignatureSimpleName(target.getParameters().get(0).getDataTypeSignature());
            }
            break;
          default:
            if (returnTypeSignature == null) {
              templateName = CodeTemplateContextType.CONSTRUCTORCOMMENT_ID;
            }
            else {
              templateName = CodeTemplateContextType.METHODCOMMENT_ID;
            }
            break;
        }

        Template template = getCodeTemplate(templateName, ownerProject);
        if (template == null) {
          return;
        }
        CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
        String getterSetterName = UNDEFINED_VAR_VALUE;
        Matcher matcher = CoreUtils.BEAN_METHOD_NAME.matcher(target.getElementName());
        if (matcher.find()) {
          getterSetterName = matcher.group(2);
        }
        context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
        context.setVariable(CodeTemplateContextType.FILENAME, UNDEFINED_VAR_VALUE);
        context.setVariable(CodeTemplateContextType.PACKAGENAME, UNDEFINED_VAR_VALUE);
        context.setVariable(CodeTemplateContextType.ENCLOSING_METHOD, target.getElementName());
        context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, UNDEFINED_VAR_VALUE);
        context.setVariable(CodeTemplateContextType.FIELD, getterSetterName);
        context.setVariable(CodeTemplateContextType.FIELD_TYPE, fieldTypeSimpleName);
        context.setVariable(CodeTemplateContextType.BARE_FIELD_NAME, getterSetterName);

        if (parameters.size() > 0) {
          context.setVariable(CodeTemplateContextType.PARAM, parameters.get(0).getElementName());
        }
        else {
          context.setVariable(CodeTemplateContextType.PARAM, UNDEFINED_VAR_VALUE);
        }

        if (returnTypeSignature != null) {
          context.setVariable(CodeTemplateContextType.RETURN_TYPE, Signature.toString(returnTypeSignature));
        }
        TemplateBuffer buffer;
        try {
          buffer = context.evaluate(template);
        }
        catch (BadLocationException e) {
          throw new SdkException(e);
        }
        catch (TemplateException e) {
          throw new SdkException(e);
        }
        if (buffer == null) {
          return;
        }

        String str = buffer.getString();
        if (Strings.containsOnlyWhitespaces(str)) {
          return;
        }
        TemplateVariable position = findVariable(buffer, CodeTemplateContextType.TAGS); // look if Javadoc tags have to be added
        if (position == null) {
          if (str != null) {
            source.append(str);
          }
          return;
        }

        IDocument document = new Document(str);
        String[] exceptionNames = new String[exceptionSignatures.size()];
        for (int i = 0; i < exceptionNames.length; i++) {
          exceptionNames[i] = Signature.toString(exceptionSignatures.get(i));
        }
        String returnType = returnTypeSignature != null ? Signature.toString(returnTypeSignature) : null;
        int[] tagOffsets = position.getOffsets();
        for (int i = tagOffsets.length - 1; i >= 0; i--) { // from last to first
          try {
            insertTag(document, tagOffsets[i], position.getLength(), paramNames, exceptionNames, returnType, EMPTY, false, lineDelimiter);
          }
          catch (BadLocationException e) {
            throw new SdkException(e);
          }
        }
        String comment = document.get();
        if (comment != null) {
          source.append(comment);
        }
      }
    };
  }

  @Override
  public ISourceBuilder createFieldComment(final IFieldSourceBuilder target) {
    return new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap builderCtx, IImportValidator validator) {
        if (!isAutomaticallyAddComments(builderCtx)) {
          return;
        }
        IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
        Template template = getCodeTemplate(CodeTemplateContextType.FIELDCOMMENT_ID, ownerProject);
        if (template != null) {
          CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
          context.setVariable(CodeTemplateContextType.FIELD_TYPE, Signature.getSignatureSimpleName(target.getSignature()));
          context.setVariable(CodeTemplateContextType.FIELD, target.getElementName());
          context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
          context.setVariable(CodeTemplateContextType.PACKAGENAME, UNDEFINED_VAR_VALUE);
          context.setVariable(CodeTemplateContextType.FILENAME, UNDEFINED_VAR_VALUE);
          String comment = evaluateTemplate(context, template);
          if (comment != null) {
            source.append(comment);
          }
        }
      }
    };
  }

  private static boolean isAutomaticallyAddComments(PropertyMap map) {
    if (map == null) {
      return false;
    }
    IJavaProject jp = map.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
    if (jp == null) {
      return false;
    }

    IScopeContext[] contexts = new IScopeContext[]{new ProjectScope(jp.getProject()), InstanceScope.INSTANCE, DefaultScope.INSTANCE};
    for (IScopeContext context : contexts) {
      IEclipsePreferences node = context.getNode(JavaUI.ID_PLUGIN);
      if (node != null) {
        String val = node.get(PreferenceConstants.CODEGEN_ADD_COMMENTS, null);
        return "true".equals(val);
      }
    }
    return true;
  }

  private static String evaluateTemplate(CodeTemplateContext context, Template template) {
    // replace the user name resolver with our own to ensure we can respect the scout specific user names.
    Iterator<TemplateVariableResolver> resolvers = context.getContextType().resolvers();
    while (resolvers.hasNext()) {
      TemplateVariableResolver resolver = resolvers.next();
      if (resolver instanceof GlobalTemplateVariables.User) {
        context.getContextType().removeResolver(resolver); // remove the JDT resolver
        context.getContextType().addResolver(USERNAME_RESOLVER); // add our own
        break;
      }
    }

    TemplateBuffer buffer;
    try {
      buffer = context.evaluate(template);
    }
    catch (BadLocationException e) {
      throw new SdkException(e);
    }
    catch (TemplateException e) {
      throw new SdkException(e);
    }
    if (buffer == null) {
      return null;
    }
    String str = buffer.getString();

    if (Strings.containsOnlyWhitespaces(str)) {
      return null;
    }
    return str;
  }

  private static Template getCodeTemplate(String id, IJavaProject project) {
    if (project == null) {
      return JavaPlugin.getDefault().getCodeTemplateStore().findTemplateById(id);
    }
    ProjectTemplateStore projectStore = new ProjectTemplateStore(project.getProject());
    try {
      projectStore.load();
    }
    catch (IOException e) {
      SdkLog.error(e);
    }
    return projectStore.findTemplateById(id);
  }

  private static TemplateVariable findVariable(TemplateBuffer buffer, String variable) {
    TemplateVariable[] positions = buffer.getVariables();
    for (int i = 0; i < positions.length; i++) {
      TemplateVariable curr = positions[i];
      if (variable.equals(curr.getType())) {
        return curr;
      }
    }
    return null;
  }

  @SuppressWarnings({"squid:S00107", "pmd:NPathComplexity"})
  private static void insertTag(IDocument textBuffer, int offset, int length, String[] paramNames, String[] exceptionNames,
      String returnType, String[] typeParameterNames, boolean isDeprecated, String lineDelimiter) throws BadLocationException {
    IRegion region = textBuffer.getLineInformationOfOffset(offset);
    if (region == null) {
      return;
    }
    String lineStart = textBuffer.get(region.getOffset(), offset - region.getOffset());

    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < typeParameterNames.length; i++) {
      if (buf.length() > 0) {
        buf.append(lineDelimiter).append(lineStart);
      }
      buf.append("@param <").append(typeParameterNames[i]).append('>');
    }
    for (int i = 0; i < paramNames.length; i++) {
      if (buf.length() > 0) {
        buf.append(lineDelimiter).append(lineStart);
      }
      buf.append("@param ").append(paramNames[i]);
    }
    if (returnType != null && !"void".equals(returnType)) {
      if (buf.length() > 0) {
        buf.append(lineDelimiter).append(lineStart);
      }
      buf.append("@return");
    }
    if (exceptionNames != null) {
      for (int i = 0; i < exceptionNames.length; i++) {
        if (buf.length() > 0) {
          buf.append(lineDelimiter).append(lineStart);
        }
        buf.append("@throws ").append(exceptionNames[i]);
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

  private static boolean isAllCommentWhitespace(String lineStart) {
    for (int i = 0; i < lineStart.length(); i++) {
      char ch = lineStart.charAt(i);
      if (!Character.isWhitespace(ch) && ch != '*') {
        return false;
      }
    }
    return true;
  }

  private static final class UsernameResolver extends GlobalTemplateVariables.User {
    @Override
    protected String resolve(TemplateContext context) {
      return CoreUtils.getUsername();
    }
  }
}
