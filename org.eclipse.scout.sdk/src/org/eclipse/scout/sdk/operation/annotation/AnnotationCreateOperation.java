/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.annotation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 *
 */
public class AnnotationCreateOperation implements IOperation {

  private final String m_signature;
  private final IMember m_annotationOwner;
  private List<String> m_parameters = new ArrayList<String>();
  private final boolean m_replaceExisting;

  public AnnotationCreateOperation(IMember annotationOwner, String signature) {
    this(annotationOwner, signature, true);
  }

  public AnnotationCreateOperation(IMember annotationOwner, String signature, boolean replaceExisting) {
    m_annotationOwner = annotationOwner;
    m_signature = signature;
    m_replaceExisting = replaceExisting;
  }

  @Override
  public String getOperationName() {
    return null;
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    workingCopyManager.register(getAnnotationOwner().getCompilationUnit(), monitor);
    CompilationUnitImportValidator validator = new CompilationUnitImportValidator(getAnnotationOwner().getCompilationUnit());
    Document doc = new Document(getAnnotationOwner().getCompilationUnit().getSource());
    TextEdit edit = createEdit(validator);
    if (edit != null) {
      // source format
      int indent = 0;
      IMember indentMember = getAnnotationOwner().getDeclaringType();
      while (indentMember != null) {
        indent++;
        indentMember = indentMember.getDeclaringType();
      }

      try {
        edit.apply(doc);
//      ISourceRange range = new SourceRange(edit.getOffset(), builder.toString().length());
//      SourceFormatOperation formatOp = new SourceFormatOperation(getAnnotationOwner().getJavaProject(), doc, range);
//      formatOp.setIndent(indent);
//      formatOp.run(monitor, workingCopyManager);
//      doc = formatOp.getDocument();
        getAnnotationOwner().getCompilationUnit().getBuffer().setContents(doc.get());
      }
      catch (Exception e) {
        ScoutSdk.logWarning("could not add annotation to '" + getAnnotationOwner().getElementName() + "'.");
      }
    }
  }

  public TextEdit createEdit(IImportValidator validator) throws JavaModelException {
    StringBuilder builder = new StringBuilder();
    builder.append(createSource(validator));
    TextEdit edit = null;
    String sn = Signature.getSignatureSimpleName(getSignature());
    String fqn = Signature.getSignatureQualifier(getSignature()) + "." + sn;
    for (IAnnotation a : getExistingAnnotations()) {
      String annotationName = a.getElementName();
      if (sn.equals(annotationName) || fqn.equals(annotationName)) {
        ISourceRange existingRange = a.getSourceRange();
        edit = new ReplaceEdit(existingRange.getOffset(), existingRange.getLength(), builder.toString());
        break;
      }
    }
    if (edit == null) {
      ISourceRange sourceRange = getAnnotationOwner().getSourceRange();
      ISourceRange javadocRange = getAnnotationOwner().getJavadocRange();
      int insertPos = sourceRange.getOffset();
      if (javadocRange != null) {
        insertPos = javadocRange.getOffset() + javadocRange.getLength();
      }
      edit = new InsertEdit(insertPos, "\n" + builder.toString() + "\n");
    }
    return edit;
  }

  @Override
  public void validate() throws IllegalArgumentException {
  }

  public String createSource(IImportValidator validator) throws JavaModelException {
    StringBuilder source = new StringBuilder();
    source.append("@" + ScoutSdkUtility.getSimpleTypeRefName(getSignature(), validator));
    String[] params = getParameters();
    if (params != null && params.length > 0) {
      source.append("(");
      for (int i = 0; i < params.length; i++) {
        source.append(params[i]);
        if (i < (params.length - 1)) {
          source.append(",");
        }
      }
      source.append(")");
    }
    return source.toString();
  }

  public String getSignature() {
    return m_signature;
  }

  public IMember getAnnotationOwner() {
    return m_annotationOwner;
  }

  public void addParameter(String parameter) {
    m_parameters.add(parameter);
  }

  public String[] getParameters() {
    return m_parameters.toArray(new String[m_parameters.size()]);
  }

  public boolean isReplaceExisting() {
    return m_replaceExisting;
  }

  private IAnnotation[] getExistingAnnotations() throws JavaModelException {
    switch (getAnnotationOwner().getElementType()) {
      case IMember.FIELD:
      case IMember.METHOD:
      case IMember.TYPE:
      case IMember.LOCAL_VARIABLE:
        return ((IAnnotatable) getAnnotationOwner()).getAnnotations();
      default:
        return new IAnnotation[0];
    }
  }
}
