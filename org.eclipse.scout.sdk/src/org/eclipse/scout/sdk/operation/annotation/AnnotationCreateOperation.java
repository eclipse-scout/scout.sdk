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

import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.form.formdata.AnnotationSourceBuilder;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.jdt.SourceRange;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 *
 */
public class AnnotationCreateOperation implements IOperation {

  public static final AnnotationCreateOperation OVERRIDE_OPERATION = new AnnotationCreateOperation(null, SignatureCache.createTypeSignature(Override.class.getName()));
  private static final Pattern PATTERN = Pattern.compile("\\s*");

  private final AnnotationSourceBuilder m_builder;
  private final IMember m_annotationOwner;

  public AnnotationCreateOperation(IMember annotationOwner, String signature) {
    m_annotationOwner = annotationOwner;
    m_builder = new AnnotationSourceBuilder(signature);
  }

  @Override
  public String getOperationName() {
    return "create annotation...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    workingCopyManager.register(getAnnotationOwner().getCompilationUnit(), monitor);
    CompilationUnitImportValidator validator = new CompilationUnitImportValidator(getAnnotationOwner().getCompilationUnit());
    Document doc = new Document(getAnnotationOwner().getCompilationUnit().getSource());

    TextEdit edit = createEdit(validator, doc.getDefaultLineDelimiter());
    if (edit != null) {
      try {
        edit.apply(doc);
        getAnnotationOwner().getCompilationUnit().getBuffer().setContents(doc.get());
        // create imports
        for (String fqi : validator.getImportsToCreate()) {
          getAnnotationOwner().getCompilationUnit().createImport(fqi, null, monitor);
        }
      }
      catch (Exception e) {
        ScoutSdk.logWarning("could not add annotation to '" + getAnnotationOwner().getElementName() + "'.");
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

    ISourceRange sourceRange = getAnnotationOwner().getSourceRange();
    int insertPos = sourceRange.getOffset();
    ISourceRange javadocRange = getAnnotationOwner().getJavadocRange();
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
          String ownerSrc = getAnnotationOwner().getSource();
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
      char[] characters = getAnnotationOwner().getCompilationUnit().getBuffer().getCharacters();
      if (replaceRange.getOffset() >= 0 && replaceRange.getOffset() <= characters.length
          && replaceRange.getOffset() + replaceRange.getLength() >= 0 && replaceRange.getOffset() + replaceRange.getLength() <= characters.length) {
        StringBuilder builder = new StringBuilder(createSource(validator, NL));
        Document sourceDocument = new Document(getAnnotationOwner().getCompilationUnit().getSource());
        IRegion lineInfo = sourceDocument.getLineInformationOfOffset(replaceRange.getOffset());
        String line = sourceDocument.get(lineInfo.getOffset(), lineInfo.getLength());
        String prefix = line.substring(0, replaceRange.getOffset() - lineInfo.getOffset());
        String postfix = line.substring(replaceRange.getOffset() - lineInfo.getOffset() + replaceRange.getLength(), line.length());
        CodeFormatter formatter = ToolFactory.createCodeFormatter(getAnnotationOwner().getJavaProject().getOptions(false));
        if (PATTERN.matcher(prefix).matches() && PATTERN.matcher(postfix).matches()) {
          // void
        }
        else if (PATTERN.matcher(prefix).matches()) {
          builder.append(NL + formatter.createIndentationString(1));
        }
        else if (PATTERN.matcher(postfix).matches()) {
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

  @Override
  public void validate() throws IllegalArgumentException {
  }

  public String createSource(IImportValidator validator, String NL) throws JavaModelException {
    return m_builder.createSource(validator);
  }

  public String getSignature() {
    return m_builder.getAnnotationSignature();
  }

  public IMember getAnnotationOwner() {
    return m_annotationOwner;
  }

  public void addParameter(String parameter) {
    m_builder.addParameter(parameter);
  }

  public String[] getParameters() {
    return m_builder.getParameters();
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
