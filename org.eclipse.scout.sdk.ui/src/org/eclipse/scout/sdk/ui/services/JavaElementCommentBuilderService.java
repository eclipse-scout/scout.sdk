/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.services;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContext;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaUIStatus;
import org.eclipse.jdt.internal.ui.viewsupport.ProjectTemplateStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.comment.IJavaElementCommentBuilderService;
import org.eclipse.scout.sdk.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.MethodParameter;

/**
 * <h3>{@link JavaElementCommentBuilderService}</h3>
 * 
 * @author aho
 * @since 3.10.0 12.07.2013
 */
@SuppressWarnings("restriction")
public class JavaElementCommentBuilderService implements IJavaElementCommentBuilderService {
  private static final String[] EMPTY = new String[0];

  @Override
  public ICommentSourceBuilder createCompilationUnitCommentBuilder() {
    return new ICommentSourceBuilder() {
      @Override
      public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        if (!(sourceBuilder instanceof ICompilationUnitSourceBuilder)) {
          throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "to create an compilation unit comment the source builder must be an instance of 'ICompilationUnitSourceBuilder'."));
        }
        ICompilationUnitSourceBuilder icuSourceBuilder = (ICompilationUnitSourceBuilder) sourceBuilder;
        Template template = getCodeTemplate(CodeTemplateContextType.FILECOMMENT_ID, ownerProject);
        if (template != null) {
          CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
          context.setVariable(CodeTemplateContextType.FILENAME, icuSourceBuilder.getElementName());
          context.setVariable(CodeTemplateContextType.PACKAGENAME, icuSourceBuilder.getPackageFragmentName());
          context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
          context.setVariable(CodeTemplateContextType.TYPENAME, JavaCore.removeJavaLikeExtension(icuSourceBuilder.getElementName()));
          source.append(evaluateTemplate(context, template));
        }
      }
    };
  }

  @Override
  public ICommentSourceBuilder createPreferencesMethodOverrideComment(final String interfaceFqn) {
    return new ICommentSourceBuilder() {

      @Override
      public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        if (!(sourceBuilder instanceof IMethodSourceBuilder)) {
          throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "to create an compilation unit comment the source builder must be an instance of 'ICompilationUnitSourceBuilder'."));
        }
        IMethodSourceBuilder methodSourceBuilder = (IMethodSourceBuilder) sourceBuilder;
        Template template = getCodeTemplate(CodeTemplateContextType.OVERRIDECOMMENT_ID, ownerProject);
        if (template != null) {
          CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
//          context.setVariable(CodeTemplateContextType.M, icuSourceBuilder.getElementName());
          context.setVariable(CodeTemplateContextType.PACKAGENAME, "undefined");
          context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());
          context.setVariable(CodeTemplateContextType.FILENAME, "undefined");
          context.setVariable(CodeTemplateContextType.ENCLOSING_METHOD, methodSourceBuilder.getElementName());
          context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, "enclosingType");

          // @see
          StringBuilder seeBuilder = new StringBuilder("@see ");
          seeBuilder.append(interfaceFqn).append("#").append(methodSourceBuilder.getElementName()).append("(");
          Iterator<MethodParameter> parameterIterator = methodSourceBuilder.getParameters().iterator();
          if (parameterIterator.hasNext()) {
            seeBuilder.append(SignatureUtility.getFullyQuallifiedName(parameterIterator.next().getSignature()));
            while (parameterIterator.hasNext()) {
              seeBuilder.append(", ").append(SignatureUtility.getFullyQuallifiedName(parameterIterator.next().getSignature()));
            }
          }
          seeBuilder.append(")");
          context.setVariable(CodeTemplateContextType.SEE_TO_OVERRIDDEN_TAG, seeBuilder.toString());
//          context.setVariable(CodeTemplateContextType.TYPENAME, JavaCore.removeJavaLikeExtension(icuSourceBuilder.getElementName()));
          source.append(evaluateTemplate(context, template));
        }
      }
    };
  }

  @Override
  public ICommentSourceBuilder createPreferencesTypeCommentBuilder() {
    return new ICommentSourceBuilder() {
      @Override
      public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        if (!(sourceBuilder instanceof ITypeSourceBuilder)) {
          throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "to create a type comment the source builder must be an instance of 'ITypeSourceBuilder'."));
        }
        ITypeSourceBuilder typeSourceBuilder = (ITypeSourceBuilder) sourceBuilder;
        Template template = getCodeTemplate(CodeTemplateContextType.TYPECOMMENT_ID, ownerProject);
        if (template == null) {
          return;
        }
        CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
//        context.setCompilationUnitVariables(cu);
        context.setVariable(CodeTemplateContextType.FILENAME, "");
        context.setVariable(CodeTemplateContextType.PACKAGENAME, "");//typeSourceBuilder.ge.getPackageFragmentName());
        context.setVariable(CodeTemplateContextType.PROJECTNAME, ownerProject.getElementName());

        context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, Signature.getQualifier(typeSourceBuilder.getElementName()));
        context.setVariable(CodeTemplateContextType.TYPENAME, Signature.getSimpleName(typeSourceBuilder.getElementName()));

        TemplateBuffer buffer;
        try {
          buffer = context.evaluate(template);
        }
        catch (BadLocationException e) {
          throw new CoreException(Status.CANCEL_STATUS);
        }
        catch (TemplateException e) {
          throw new CoreException(Status.CANCEL_STATUS);
        }
        String str = buffer.getString();
        if (Strings.containsOnlyWhitespaces(str)) {
          return;
        }

        TemplateVariable position = findVariable(buffer, CodeTemplateContextType.TAGS); // look if Javadoc tags have to be added
        if (position == null) {
          source.append(str);
          return;
        }

        IDocument document = new Document(str);
        int[] tagOffsets = position.getOffsets();
        for (int i = tagOffsets.length - 1; i >= 0; i--) { // from last to first
          try {
            insertTag(document, tagOffsets[i], position.getLength(), EMPTY, EMPTY, null, EMPTY, false, lineDelimiter);
          }
          catch (BadLocationException e) {
            throw new CoreException(JavaUIStatus.createError(IStatus.ERROR, e));
          }
        }
        source.append(document.get());
      }
    };
  }

  @Override
  public ICommentSourceBuilder createPreferencesMethodCommentBuilder() {
    return new ICommentSourceBuilder() {
      @Override
      public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        if (!(sourceBuilder instanceof IMethodSourceBuilder)) {
          throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "to create a method comment the source builder must be an instance of 'IMethodSourceBuilder'."));
        }
        IMethodSourceBuilder methodSourceBuilder = (IMethodSourceBuilder) sourceBuilder;
        List<MethodParameter> parameters = methodSourceBuilder.getParameters();
        String[] paramSignatures = new String[parameters.size()];
        String[] paramNames = new String[parameters.size()];
        int j = 0;
        for (MethodParameter param : parameters) {
          paramSignatures[j] = param.getSignature();
          paramNames[j] = param.getName();
          j++;
        }
        List<String> exceptionSignatures = methodSourceBuilder.getExceptionSignatures();

        String templateName = CodeTemplateContextType.METHODCOMMENT_ID;
        String returnTypeSignature = methodSourceBuilder.getReturnTypeSignature();
        if (returnTypeSignature == null) {
          templateName = CodeTemplateContextType.CONSTRUCTORCOMMENT_ID;
        }
        Template template = getCodeTemplate(templateName, ownerProject);
        if (template == null) {
          return;
        }
        CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
//        context.setCompilationUnitVariables(cu);
//        context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE, typeName);
        context.setVariable(CodeTemplateContextType.ENCLOSING_METHOD, methodSourceBuilder.getElementName());

        if (returnTypeSignature != null) {
          context.setVariable(CodeTemplateContextType.RETURN_TYPE, Signature.toString(returnTypeSignature));
        }
        TemplateBuffer buffer;
        try {
          buffer = context.evaluate(template);
        }
        catch (BadLocationException e) {
          throw new CoreException(Status.CANCEL_STATUS);
        }
        catch (TemplateException e) {
          throw new CoreException(Status.CANCEL_STATUS);
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
          source.append(str);
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
            insertTag(document, tagOffsets[i], position.getLength(), paramNames, exceptionNames, returnType, paramNames, false, lineDelimiter);
          }
          catch (BadLocationException e) {
            throw new CoreException(JavaUIStatus.createError(IStatus.ERROR, e));
          }
        }
        source.append(document.get());
      }
    };
  }

  @Override
  public ICommentSourceBuilder createPreferencesFieldCommentBuilder() {
    return new ICommentSourceBuilder() {
      @Override
      public void createSource(ISourceBuilder sourceBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        if (!(sourceBuilder instanceof IFieldSourceBuilder)) {
          throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "to create an field comment the source builder must be an instance of 'IFieldSourceBuilder'."));
        }
        IFieldSourceBuilder fieldSourceBuilder = (IFieldSourceBuilder) sourceBuilder;
        Template template = getCodeTemplate(CodeTemplateContextType.FIELDCOMMENT_ID, ownerProject);
        if (template != null) {
          CodeTemplateContext context = new CodeTemplateContext(template.getContextTypeId(), ownerProject, lineDelimiter);
//          context.setCompilationUnitVariables(cu);
          context.setVariable(CodeTemplateContextType.FIELD_TYPE, Signature.getSignatureSimpleName(fieldSourceBuilder.getSignature()));
          context.setVariable(CodeTemplateContextType.FIELD, fieldSourceBuilder.getElementName());
          source.append(evaluateTemplate(context, template));
        }
      }
    };
  }

  private String evaluateTemplate(CodeTemplateContext context, Template template) throws CoreException {
    TemplateBuffer buffer;
    try {
      buffer = context.evaluate(template);
    }
    catch (BadLocationException e) {
      throw new CoreException(Status.CANCEL_STATUS);
    }
    catch (TemplateException e) {
      throw new CoreException(Status.CANCEL_STATUS);
    }
    if (buffer == null) return null;
    String str = buffer.getString();
    if (Strings.containsOnlyWhitespaces(str)) {
      return null;
    }
    return str;
  }

  private Template getCodeTemplate(String id, IJavaProject project) {
    if (project == null) return JavaPlugin.getDefault().getCodeTemplateStore().findTemplateById(id);
    ProjectTemplateStore projectStore = new ProjectTemplateStore(project.getProject());
    try {
      projectStore.load();
    }
    catch (IOException e) {
      ScoutSdk.logError(e);
    }
    return projectStore.findTemplateById(id);
  }

  private TemplateVariable findVariable(TemplateBuffer buffer, String variable) {
    TemplateVariable[] positions = buffer.getVariables();
    for (int i = 0; i < positions.length; i++) {
      TemplateVariable curr = positions[i];
      if (variable.equals(curr.getType())) {
        return curr;
      }
    }
    return null;
  }

  private static void insertTag(IDocument textBuffer, int offset, int length, String[] paramNames, String[] exceptionNames, String returnType, String[] typeParameterNames, boolean isDeprecated,
      String lineDelimiter) throws BadLocationException {
    IRegion region = textBuffer.getLineInformationOfOffset(offset);
    if (region == null) {
      return;
    }
    String lineStart = textBuffer.get(region.getOffset(), offset - region.getOffset());

    StringBuffer buf = new StringBuffer();
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
    if (returnType != null && !returnType.equals("void")) { //$NON-NLS-1$
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

}
