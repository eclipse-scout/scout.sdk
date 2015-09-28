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
package org.eclipse.scout.sdk.s2e.workspace;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.scout.sdk.core.importcollector.IImportCollector;
import org.eclipse.scout.sdk.core.importcollector.ImportCollector;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.importvalidator.ImportValidator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitScopedImportCollector;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>{@link AnnotationNewOperation}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 06.12.2012
 */
public class AnnotationNewOperation implements IOperation {

  private static final Pattern REGEX_WHITE_SPACE_START = Pattern.compile("^(\\s+).*");

  private IAnnotationSourceBuilder m_sourceBuilder;
  private final IMember m_declaringMember;

  public AnnotationNewOperation(IAnnotationSourceBuilder sourceBuilder, IMember declaringType) {
    m_sourceBuilder = sourceBuilder;
    m_declaringMember = declaringType;
  }

  @Override
  public String getOperationName() {
    return "create annotation " + Signature.getSimpleName(m_sourceBuilder.getName()) + "...";
  }

  @Override
  public void validate() {
    if (!JdtUtils.exists(m_declaringMember)) {
      throw new IllegalArgumentException("Declaring member does not exist!");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ICompilationUnit icu = m_declaringMember.getCompilationUnit();

    workingCopyManager.register(icu, monitor);

    // get package
    String pck = "";
    IPackageDeclaration[] packageDeclarations = icu.getPackageDeclarations();
    if (packageDeclarations.length > 0) {
      pck = packageDeclarations[0].getElementName();
    }

    IJavaEnvironment env = ScoutSdkCore.createJavaEnvironment(m_declaringMember.getJavaProject());
    IImportCollector collector = new CompilationUnitScopedImportCollector(new ImportCollector(env), pck);
    Document doc = new Document(icu.getSource());

    TextEdit edit = createEdit(new ImportValidator(collector), doc, icu.findRecommendedLineSeparator());
    try {
      edit.apply(doc);
      icu.getBuffer().setContents(doc.get());

      // create imports
      new ImportsCreateOperation(icu, collector).run(monitor, workingCopyManager);
    }
    catch (Exception e) {
      SdkLog.warning("could not add annotation to '" + m_declaringMember.getElementName() + "'.", e);
    }
  }

  protected ISourceRange getAnnotationReplaceRange(Document sourceDocument, String newLine, String newAnnotationSource) throws JavaModelException, BadLocationException {
    String sn = Signature.getSimpleName(m_sourceBuilder.getName());
    String fqn = Signature.getQualifier(m_sourceBuilder.getName()) + "." + sn;
    int newLineLength = newLine.length();

    IRegion lineOfMemberName = sourceDocument.getLineInformationOfOffset(m_declaringMember.getNameRange().getOffset());
    int lineBeforeMemberNameEndPos = lineOfMemberName.getOffset() - newLineLength;
    int lastLineStart = sourceDocument.getLineInformationOfOffset(m_declaringMember.getSourceRange().getOffset()).getOffset();
    int newAnnotationLen = newAnnotationSource.length();
    IRegion lineInfo = sourceDocument.getLineInformationOfOffset(lineBeforeMemberNameEndPos);
    IRegion result = lineOfMemberName;
    boolean isReplaceExisting = false;
    boolean isInBlockComment = false;
    while (lineInfo.getOffset() >= lastLineStart) {
      String lineSource = sourceDocument.get(lineInfo.getOffset(), lineInfo.getLength());
      if (lineSource != null) {
        lineSource = CoreUtils.removeComments(lineSource.trim());
        if (lineSource.length() > 0) {
          if (!isInBlockComment && lineSource.endsWith("*/")) {
            isInBlockComment = true;
          }
          else if (isInBlockComment && lineSource.startsWith("/*")) {
            isInBlockComment = false;
          }

          if (!isInBlockComment) {
            if (lineSource.charAt(0) == '@') {
              // if the existing annotation is longer than the one to insert (to ensure the annotations get from short to long)
              if (lineSource.length() > newAnnotationLen) {
                result = lineInfo;
              }

              if (lineSource.startsWith("@" + sn) || lineSource.startsWith("@" + fqn)) {
                // the annotation that should be created already exists -> replace it
                result = lineInfo;
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

  public TextEdit createEdit(IImportValidator validator, Document sourceDocument, String nl) throws CoreException {
    try {
      // create new source
      StringBuilder builder = new StringBuilder();

      PropertyMap context = new PropertyMap();
      context.setProperty(ISdkProperties.CONTEXT_PROPERTY_JAVA_PROJECT, m_declaringMember.getJavaProject());

      getSourceBuilder().createSource(builder, nl, context, validator);

      // find insert/replace range
      ISourceRange replaceRange = getAnnotationReplaceRange(sourceDocument, nl, builder.toString());

      // insert indentation at the beginning
      builder.insert(0, getIndent(sourceDocument, replaceRange));

      // insert newline at the end if required
      if (replaceRange.getLength() == 0) {
        builder.append(nl);
      }

      return new ReplaceEdit(replaceRange.getOffset(), replaceRange.getLength(), builder.toString());
    }
    catch (BadLocationException e) {
      throw new CoreException(new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "could not find insert location for annotation.", e));
    }
  }

  public IAnnotationSourceBuilder getSourceBuilder() {
    return m_sourceBuilder;
  }
}
