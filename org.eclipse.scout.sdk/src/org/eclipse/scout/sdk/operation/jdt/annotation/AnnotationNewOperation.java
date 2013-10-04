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

import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
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
 * @author aho
 * @since 3.10.0 06.12.2012
 */
public class AnnotationNewOperation implements IOperation {

  private static final Pattern PATTERN = Pattern.compile("\\s*");

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

    TextEdit edit = createEdit(validator, doc.getDefaultLineDelimiter());
    if (edit != null) {
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
  }

  protected ISourceRange getAnnotationReplaceRange() throws JavaModelException {
    String sn = Signature.getSignatureSimpleName(getSignature());
    String fqn = Signature.getSignatureQualifier(getSignature()) + "." + sn;
    for (IAnnotation a : getExistingAnnotations()) {
      String annotationName = a.getElementName();
      if (sn.equals(annotationName) || fqn.equals(annotationName)) {
        ISourceRange existingRange = a.getSourceRange();
        return existingRange;
      }
    }

    ISourceRange sourceRange = getDeclaringType().getSourceRange();
    int insertPos = sourceRange.getOffset();
    ISourceRange javadocRange = getDeclaringType().getJavadocRange();
    if (javadocRange != null) {
      insertPos = javadocRange.getOffset() + javadocRange.getLength();
    }
    return new SourceRange(insertPos + getJdtAnnotationOffsetFix(), 0);
  }

  private int getJdtAnnotationOffsetFix() throws JavaModelException {
    // Fix for JDT bug with annotation creation and source ranges not updated properly
    try {
      String annotationStart = "@";
      IAnnotation[] existings = getExistingAnnotations();
      for (IAnnotation sample : existings) {
        String src = sample.getSource();
        if (!src.startsWith(annotationStart)) {
          String ownerSrc = getDeclaringType().getSource();
          int fixCandidate = ownerSrc.indexOf(annotationStart);
          if (fixCandidate > 0) {
            ISourceRange annoRange = sample.getSourceRange();
            SourceRange r = new SourceRange(fixCandidate, annoRange.getLength());

            int start = r.getOffset() + annotationStart.length();
            int end = r.getOffset() + r.getLength();
            if (start >= 0 && start < ownerSrc.length() && end > start && end < ownerSrc.length()) {
              String check = ownerSrc.substring(start, end);
              if (check.startsWith(sample.getElementName())) {
                return fixCandidate;
              }
            }
          }
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning(e);
    }
    return 0;
  }

  public TextEdit createEdit(IImportValidator validator, String NL) throws CoreException {
    try {
      ISourceRange replaceRange = getAnnotationReplaceRange();
      // check empty line
      char[] characters = getDeclaringType().getCompilationUnit().getBuffer().getCharacters();
      if (replaceRange.getOffset() >= 0 && replaceRange.getOffset() <= characters.length
          && replaceRange.getOffset() + replaceRange.getLength() >= 0 && replaceRange.getOffset() + replaceRange.getLength() <= characters.length) {
        StringBuilder builder = new StringBuilder();
        getSourceBuilder().createSource(builder, ResourceUtility.getLineSeparator(getDeclaringType().getCompilationUnit()), getDeclaringType().getJavaProject(), validator);
        Document sourceDocument = new Document(getDeclaringType().getCompilationUnit().getSource());
        IRegion lineInfo = sourceDocument.getLineInformationOfOffset(replaceRange.getOffset());
        String line = sourceDocument.get(lineInfo.getOffset(), lineInfo.getLength());
        String prefix = line.substring(0, replaceRange.getOffset() - lineInfo.getOffset());
        String postfix = line.substring(replaceRange.getOffset() - lineInfo.getOffset() + replaceRange.getLength(), line.length());
        CodeFormatter formatter = ToolFactory.createCodeFormatter(getDeclaringType().getJavaProject().getOptions(false));
        if (PATTERN.matcher(prefix).matches() && !PATTERN.matcher(postfix).matches()) {
          builder.append(NL + formatter.createIndentationString(1));
        }
        else if (PATTERN.matcher(postfix).matches() && !PATTERN.matcher(prefix).matches()) {
          builder.insert(0, NL + formatter.createIndentationString(1));
        }
        return new ReplaceEdit(replaceRange.getOffset(), replaceRange.getLength(), builder.toString());
      }
    }
    catch (BadLocationException e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not find insert location for annotation.", e));
    }
    return null;
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

  private IAnnotation[] getExistingAnnotations() throws JavaModelException {
    switch (getDeclaringType().getElementType()) {
      case IMember.FIELD:
      case IMember.METHOD:
      case IMember.TYPE:
      case IMember.LOCAL_VARIABLE:
        return ((IAnnotatable) getDeclaringType()).getAnnotations();
      default:
        return new IAnnotation[0];
    }
  }

  public static AnnotationNewOperation createOrderAnnotation(IType declaringType, double orderNr) {
    AnnotationNewOperation orderAnnoation = new AnnotationNewOperation(SignatureCache.createTypeSignature(RuntimeClasses.Order), declaringType);
    orderAnnoation.addParameter(Double.toString(orderNr));
    return orderAnnoation;
  }
}
