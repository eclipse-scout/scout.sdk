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
import org.eclipse.scout.sdk.core.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodParameterDescription;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;

/**
 * <h3>{@link JavaElementCommentBuilderService}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 12.07.2013
 */
public class JdtSettingsCommentBuilder implements IJavaElementCommentBuilder {
  private static final String[] EMPTY = new String[0];
  private static final String UNDEFINED_VAR_VALUE = "undefined";

  private static final int METHOD_TYPE_NORMAL = 1 << 1;
  private static final int METHOD_TYPE_GETTER = 1 << 2;
  private static final int METHOD_TYPE_SETTER = 1 << 3;

  private static final UsernameResolver USERNAME_RESOLVER = new UsernameResolver();

  private static final ICommentSourceBuilder COMPILATION_UNIT_COMMENT_BUILDER = new ICommentSourceBuilder() {
    @Override
    public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, PropertyMap builderCtx, IImportValidator validator) {
      if (!(sourceBuilder instanceof ICompilationUnitSourceBuilder)) {
        throw new IllegalArgumentException("to create an compilation unit comment the source builder must be an instance of 'ICompilationUnitSourceBuilder'.");
      }
      if (!isAutomaticallyAddComments(builderCtx)) {
        return;
      }
      IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
      ICompilationUnitSourceBuilder icuSourceBuilder = (ICompilationUnitSourceBuilder) sourceBuilder;
      Template template = getCodeTemplate(CodeTemplateContextType.FILECOMMENT_ID, ownerProject);
      if (template != null) {
        CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
        context.setVariable(CodeTemplateContextType.FILENAME, icuSourceBuilder.getElementName());
        context.setVariable(CodeTemplateContextType.PACKAGENAME, icuSourceBuilder.getPackageFragmentName());
        context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
        context.setVariable(CodeTemplateContextType.TYPENAME, JavaCore.removeJavaLikeExtension(icuSourceBuilder.getElementName()));
        String comment = evaluateTemplate(context, template);
        if (comment != null) {
          source.append(comment);
        }
      }
    }
  };

  private static final ICommentSourceBuilder TYPE_COMMENT_SOURCE_BUILDER = new ICommentSourceBuilder() {
    @Override
    public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, PropertyMap builderCtx, IImportValidator validator) {
      if (!(sourceBuilder instanceof ITypeSourceBuilder)) {
        throw new IllegalArgumentException("to create a type comment the source builder must be an instance of 'ITypeSourceBuilder'.");
      }
      if (!isAutomaticallyAddComments(builderCtx)) {
        return;
      }
      IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
      ITypeSourceBuilder typeSourceBuilder = (ITypeSourceBuilder) sourceBuilder;
      Template template = getCodeTemplate(CodeTemplateContextType.TYPECOMMENT_ID, ownerProject);
      if (template == null) {
        return;
      }
      CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
      context.setVariable(CodeTemplateContextType.FILENAME, UNDEFINED_VAR_VALUE);
      context.setVariable(CodeTemplateContextType.PACKAGENAME, Signature.getQualifier(typeSourceBuilder.getFullyQualifiedName()));
      context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
      context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, Signature.getQualifier(typeSourceBuilder.getElementName()));
      context.setVariable(CodeTemplateContextType.TYPENAME, Signature.getSimpleName(typeSourceBuilder.getElementName()));

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

  private static final ICommentSourceBuilder METHOD_COMMENT_SOURCE_BUILDER = createMethodSourceBuilder(METHOD_TYPE_NORMAL);
  private static final ICommentSourceBuilder METHOD_GETTER_COMMENT_SOURCE_BUILDER = createMethodSourceBuilder(METHOD_TYPE_GETTER);
  private static final ICommentSourceBuilder METHOD_SETTER_COMMENT_SOURCE_BUILDER = createMethodSourceBuilder(METHOD_TYPE_SETTER);

  private static final ICommentSourceBuilder FIELD_COMMENT_SOURCE_BUILDER = new ICommentSourceBuilder() {
    @Override
    public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, PropertyMap builderCtx, IImportValidator validator) {
      if (!(sourceBuilder instanceof IFieldSourceBuilder)) {
        throw new IllegalArgumentException("to create an field comment the source builder must be an instance of 'IFieldSourceBuilder'.");
      }
      if (!isAutomaticallyAddComments(builderCtx)) {
        return;
      }
      IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
      IFieldSourceBuilder fieldSourceBuilder = (IFieldSourceBuilder) sourceBuilder;
      Template template = getCodeTemplate(CodeTemplateContextType.FIELDCOMMENT_ID, ownerProject);
      if (template != null) {
        CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
        context.setVariable(CodeTemplateContextType.FIELD_TYPE, Signature.getSignatureSimpleName(fieldSourceBuilder.getSignature()));
        context.setVariable(CodeTemplateContextType.FIELD, fieldSourceBuilder.getElementName());
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

  @Override
  public ICommentSourceBuilder createPreferencesCompilationUnitCommentBuilder() {
    return COMPILATION_UNIT_COMMENT_BUILDER;
  }

  @Override
  public ICommentSourceBuilder createPreferencesMethodOverrideComment(final String interfaceFqn) {
    return new ICommentSourceBuilder() {
      @Override
      public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, PropertyMap builderCtx, IImportValidator validator) {
        if (!(sourceBuilder instanceof IMethodSourceBuilder)) {
          throw new IllegalArgumentException("to create an compilation unit comment the source builder must be an instance of 'ICompilationUnitSourceBuilder'.");
        }
        if (!isAutomaticallyAddComments(builderCtx)) {
          return;
        }
        IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
        IMethodSourceBuilder methodSourceBuilder = (IMethodSourceBuilder) sourceBuilder;
        Template template = getCodeTemplate(CodeTemplateContextType.OVERRIDECOMMENT_ID, ownerProject);
        if (template != null) {
          CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
          context.setVariable(CodeTemplateContextType.PACKAGENAME, UNDEFINED_VAR_VALUE);
          context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
          context.setVariable(CodeTemplateContextType.FILENAME, UNDEFINED_VAR_VALUE);
          context.setVariable(CodeTemplateContextType.ENCLOSING_METHOD, methodSourceBuilder.getElementName());
          context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, UNDEFINED_VAR_VALUE);

          // @see
          StringBuilder seeBuilder = new StringBuilder("@see ");
          seeBuilder.append(interfaceFqn).append("#").append(methodSourceBuilder.getElementName()).append("(");
          Iterator<MethodParameterDescription> parameterIterator = methodSourceBuilder.getParameters().iterator();
          if (parameterIterator.hasNext()) {
            seeBuilder.append(SignatureUtils.toFullyQualifiedName(parameterIterator.next().getSignature()));
            while (parameterIterator.hasNext()) {
              seeBuilder.append(", ").append(SignatureUtils.toFullyQualifiedName(parameterIterator.next().getSignature()));
            }
          }
          seeBuilder.append(")");
          context.setVariable(CodeTemplateContextType.SEE_TO_OVERRIDDEN_TAG, seeBuilder.toString());
          String comment = evaluateTemplate(context, template);
          if (comment != null) {
            source.append(comment);
          }
        }
      }
    };
  }

  private static ICommentSourceBuilder createMethodSourceBuilder(final int type) {
    return new ICommentSourceBuilder() {
      @Override
      public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, PropertyMap builderCtx, IImportValidator validator) {
        if (!(sourceBuilder instanceof IMethodSourceBuilder)) {
          throw new IllegalArgumentException("to create a method comment the source builder must be an instance of 'IMethodSourceBuilder'.");
        }
        if (!isAutomaticallyAddComments(builderCtx)) {
          return;
        }
        IJavaProject ownerProject = builderCtx.getProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, IJavaProject.class);
        IMethodSourceBuilder methodSourceBuilder = (IMethodSourceBuilder) sourceBuilder;
        List<MethodParameterDescription> parameters = methodSourceBuilder.getParameters();
        String[] paramSignatures = new String[parameters.size()];
        String[] paramNames = new String[parameters.size()];
        int j = 0;
        for (MethodParameterDescription param : parameters) {
          paramSignatures[j] = param.getSignature();
          paramNames[j] = param.getName();
          j++;
        }

        List<String> exceptionSignatures = methodSourceBuilder.getExceptionSignatures();
        String returnTypeSignature = methodSourceBuilder.getReturnTypeSignature();

        String fieldTypeSimpleName = UNDEFINED_VAR_VALUE;
        String templateName = null;
        switch (type) {
          case METHOD_TYPE_GETTER:
            templateName = CodeTemplateContextType.GETTERCOMMENT_ID;
            fieldTypeSimpleName = Signature.getSignatureSimpleName(methodSourceBuilder.getReturnTypeSignature());
            break;
          case METHOD_TYPE_SETTER:
            templateName = CodeTemplateContextType.SETTERCOMMENT_ID;
            if (methodSourceBuilder.getParameters().size() > 0) {
              fieldTypeSimpleName = Signature.getSignatureSimpleName(methodSourceBuilder.getParameters().get(0).getSignature());
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
        Matcher matcher = CoreUtils.BEAN_METHOD_NAME.matcher(methodSourceBuilder.getElementName());
        if (matcher.find()) {
          getterSetterName = matcher.group(2);
        }
        context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
        context.setVariable(CodeTemplateContextType.FILENAME, UNDEFINED_VAR_VALUE);
        context.setVariable(CodeTemplateContextType.PACKAGENAME, UNDEFINED_VAR_VALUE);
        context.setVariable(CodeTemplateContextType.ENCLOSING_METHOD, methodSourceBuilder.getElementName());
        context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, UNDEFINED_VAR_VALUE);
        context.setVariable(CodeTemplateContextType.FIELD, getterSetterName);
        context.setVariable(CodeTemplateContextType.FIELD_TYPE, fieldTypeSimpleName);
        context.setVariable(CodeTemplateContextType.BARE_FIELD_NAME, getterSetterName);

        if (parameters.size() > 0) {
          context.setVariable(CodeTemplateContextType.PARAM, parameters.get(0).getName());
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
  public ICommentSourceBuilder createPreferencesTypeCommentBuilder() {
    return TYPE_COMMENT_SOURCE_BUILDER;
  }

  @Override
  public ICommentSourceBuilder createPreferencesMethodCommentBuilder() {
    return METHOD_COMMENT_SOURCE_BUILDER;
  }

  @Override
  public ICommentSourceBuilder createPreferencesMethodGetterCommentBuilder() {
    return METHOD_GETTER_COMMENT_SOURCE_BUILDER;
  }

  @Override
  public ICommentSourceBuilder createPreferencesMethodSetterCommentBuilder() {
    return METHOD_SETTER_COMMENT_SOURCE_BUILDER;
  }

  @Override
  public ICommentSourceBuilder createPreferencesFieldCommentBuilder() {
    return FIELD_COMMENT_SOURCE_BUILDER;
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

  @SuppressWarnings("unchecked")
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
      S2ESdkUiActivator.logError(e);
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

  private static void insertTag(IDocument textBuffer, int offset, int length, String[] paramNames, String[] exceptionNames, String returnType, String[] typeParameterNames, boolean isDeprecated, String lineDelimiter) throws BadLocationException {
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
      buf.append("@param <").append(typeParameterNames[i]).append('>'); //$NON-NLS-1$
    }
    for (int i = 0; i < paramNames.length; i++) {
      if (buf.length() > 0) {
        buf.append(lineDelimiter).append(lineStart);
      }
      buf.append("@param ").append(paramNames[i]); //$NON-NLS-1$
    }
    if (returnType != null && !"void".equals(returnType)) { //$NON-NLS-1$
      if (buf.length() > 0) {
        buf.append(lineDelimiter).append(lineStart);
      }
      buf.append("@return"); //$NON-NLS-1$
    }
    if (exceptionNames != null) {
      for (int i = 0; i < exceptionNames.length; i++) {
        if (buf.length() > 0) {
          buf.append(lineDelimiter).append(lineStart);
        }
        buf.append("@throws ").append(exceptionNames[i]); //$NON-NLS-1$
      }
    }
    if (isDeprecated) {
      if (buf.length() > 0) {
        buf.append(lineDelimiter).append(lineStart);
      }
      buf.append("@deprecated"); //$NON-NLS-1$
    }
    if (buf.length() == 0 && isAllCommentWhitespace(lineStart)) {
      int prevLine = textBuffer.getLineOfOffset(offset) - 1;
      if (prevLine > 0) {
        IRegion prevRegion = textBuffer.getLineInformation(prevLine);
        int prevLineEnd = prevRegion.getOffset() + prevRegion.getLength();
        // clear full line
        textBuffer.replace(prevLineEnd, offset + length - prevLineEnd, ""); //$NON-NLS-1$
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
