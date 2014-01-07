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
package org.eclipse.scout.sdk.operation.jdt.annotation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.jdt.SourceRange;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>{@link AnnotationNewOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.10.0 06.12.2012
 */
public class AnnotationNewOperation implements IOperation {

  private final static Pattern REGEX_WHITE_SPACE_START = Pattern.compile("^(\\s+).*");

  private IAnnotationSourceBuilder m_sourceBuilder;
  private final IMember m_declaringType;

  /**
   * @param elementName
   */
  public AnnotationNewOperation(String signature, IMember declaringType) {
    this(new AnnotationSourceBuilder(signature), declaringType);
  }

  public AnnotationNewOperation(IAnnotationSourceBuilder sourceBuilder, IMember declaringType) {
    m_sourceBuilder = sourceBuilder;
    m_declaringType = declaringType;
  }

  @Override
  public String getOperationName() {
    return "create annotation " + Signature.getSignatureSimpleName(getSignature()) + "...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!TypeUtility.exists(getDeclaringType())) {
      throw new IllegalArgumentException("Declaring member does not exist!");
    }
    getSourceBuilder().validate();
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    workingCopyManager.register(getDeclaringType().getCompilationUnit(), monitor);
    CompilationUnitImportValidator validator = new CompilationUnitImportValidator(getDeclaringType().getCompilationUnit());
    Document doc = new Document(getDeclaringType().getCompilationUnit().getSource());

    TextEdit edit = createEdit(validator, doc, ResourceUtility.getLineSeparator(getDeclaringType().getCompilationUnit()));
    try {
      edit.apply(doc);
      getDeclaringType().getCompilationUnit().getBuffer().setContents(doc.get());

      // create imports
      for (String fqi : validator.getImportsToCreate()) {
        getDeclaringType().getCompilationUnit().createImport(fqi, null, monitor);
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not add annotation to '" + getDeclaringType().getElementName() + "'.");
    }
  }

  protected ISourceRange getAnnotationReplaceRange(Document sourceDocument, String newLine) throws JavaModelException, BadLocationException {
    String sn = Signature.getSignatureSimpleName(getSignature());
    String fqn = Signature.getSignatureQualifier(getSignature()) + "." + sn;
    int newLineLength = newLine.length();

    IRegion lineOfMemberName = sourceDocument.getLineInformationOfOffset(getDeclaringType().getNameRange().getOffset());
    int lineBeforeMemberNameEndPos = lineOfMemberName.getOffset() - newLineLength;
    int lastLineStart = sourceDocument.getLineInformationOfOffset(getDeclaringType().getSourceRange().getOffset()).getOffset();
    IRegion lineInfo = sourceDocument.getLineInformationOfOffset(lineBeforeMemberNameEndPos);
    IRegion result = lineOfMemberName;
    boolean isReplaceExisting = false;
    boolean isInBlockComment = false;
    while (lineInfo.getOffset() >= lastLineStart) {
      String lineSource = sourceDocument.get(lineInfo.getOffset(), lineInfo.getLength());
      if (lineSource != null) {
        lineSource = ScoutUtility.removeComments(lineSource.trim());
        if (lineSource.length() > 0) {
          if (!isInBlockComment && lineSource.endsWith("*/")) {
            isInBlockComment = true;
          }
          else if (isInBlockComment && lineSource.startsWith("/*")) {
            isInBlockComment = false;
          }

          if (!isInBlockComment) {
            if (lineSource.charAt(0) == '@') {
              result = lineInfo;
              if (lineSource.startsWith("@" + sn) || lineSource.startsWith("@" + fqn)) {
                // the annotation that should be created already exists -> replace it
                isReplaceExisting = true;
                break;
              }
            }
          }
        }
      }
      lineInfo = sourceDocument.getLineInformationOfOffset(lineInfo.getOffset() - newLineLength); // one line up
    }

    return new SourceRange(result.getOffset(), isReplaceExisting ? result.getLength() : 0);
  }

  protected String getIndent(Document sourceDocument, ISourceRange replaceRange) throws BadLocationException {
    IRegion line = sourceDocument.getLineInformationOfOffset(replaceRange.getOffset());
    Matcher matcher = REGEX_WHITE_SPACE_START.matcher(sourceDocument.get(line.getOffset(), line.getLength()));
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "";
  }

  public TextEdit createEdit(IImportValidator validator, Document sourceDocument, String NL) throws CoreException {
    try {
      // find insert/replace range
      ISourceRange replaceRange = getAnnotationReplaceRange(sourceDocument, NL);

      // create new source
      StringBuilder builder = new StringBuilder(getIndent(sourceDocument, replaceRange));
      getSourceBuilder().createSource(builder, NL, getDeclaringType().getJavaProject(), validator);
      if (replaceRange.getLength() == 0) {
        builder.append(NL);
      }
      return new ReplaceEdit(replaceRange.getOffset(), replaceRange.getLength(), builder.toString());
    }
    catch (BadLocationException e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not find insert location for annotation.", e));
    }
  }

  public IAnnotationSourceBuilder getSourceBuilder() {
    return m_sourceBuilder;
  }

  public IMember getDeclaringType() {
    return m_declaringType;
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilder#getSignature()
   */
  public String getSignature() {
    return m_sourceBuilder.getSignature();
  }

  /**
   * @param parameter
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilder#addParameter(java.lang.String)
   */
  public boolean addParameter(String parameter) {
    return m_sourceBuilder.addParameter(parameter);
  }

  /**
   * @param parameter
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilder#removeParameter(java.lang.String)
   */
  public boolean removeParameter(String parameter) {
    return m_sourceBuilder.removeParameter(parameter);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilder#getParameters()
   */
  public String[] getParameters() {
    return m_sourceBuilder.getParameters();
  }
}
