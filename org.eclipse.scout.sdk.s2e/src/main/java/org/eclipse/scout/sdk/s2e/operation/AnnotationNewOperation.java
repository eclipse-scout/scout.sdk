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
package org.eclipse.scout.sdk.s2e.operation;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.eclipse.scout.sdk.s2e.environment.WorkingCopyManager.currentWorkingCopyManager;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.imports.CompilationUnitScopedImportCollector;
import org.eclipse.scout.sdk.core.imports.IImportCollector;
import org.eclipse.scout.sdk.core.imports.IImportValidator;
import org.eclipse.scout.sdk.core.imports.ImportCollector;
import org.eclipse.scout.sdk.core.imports.ImportValidator;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.PropertySupport;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>{@link AnnotationNewOperation}</h3>
 *
 * @since 3.10.0 2012-12-06
 */
public class AnnotationNewOperation implements BiConsumer<EclipseEnvironment, EclipseProgress> {

  private static final Pattern REGEX_WHITE_SPACE_START = Pattern.compile("^(\\s+).*");

  private final IAnnotationGenerator<?> m_sourceBuilder;
  private final IMember m_declaringMember;

  public AnnotationNewOperation(IAnnotationGenerator<?> sourceBuilder, IMember declaringType) {
    m_sourceBuilder = sourceBuilder;
    m_declaringMember = declaringType;
  }

  @Override
  public void accept(EclipseEnvironment env, EclipseProgress progress) {
    Ensure.isTrue(JdtUtils.exists(m_declaringMember));

    try {
      ICompilationUnit icu = m_declaringMember.getCompilationUnit();
      currentWorkingCopyManager().register(icu, progress.monitor());

      IJavaEnvironment javaEnv = env.toScoutJavaEnvironment(m_declaringMember.getJavaProject());
      IImportCollector collector = new CompilationUnitScopedImportCollector(new ImportCollector(javaEnv), JdtUtils.getPackage(icu));
      IDocument doc = new Document(icu.getSource());

      TextEdit edit = createEdit(new ImportValidator(collector), doc, icu.findRecommendedLineSeparator());
      edit.apply(doc);
      IBuffer buffer = icu.getBuffer();
      buffer.setContents(doc.get());

      // create imports
      new ImportsCreateOperation(icu, collector).accept(env, progress);
    }
    catch (CoreException | BadLocationException ex) {
      SdkLog.warning("could not add annotation to '{}'.", m_declaringMember.getElementName(), ex);
    }
  }

  protected ISourceRange getAnnotationReplaceRange(IDocument sourceDocument, CharSequence newLine, CharSequence newAnnotationSource) throws JavaModelException, BadLocationException {
    String sn = JavaTypes.simpleName(m_sourceBuilder.elementName().orElseThrow(() -> newFail("Annotation generator is missing the name.")));
    String fqn = JavaTypes.qualifier(m_sourceBuilder.elementName().get()) + JavaTypes.C_DOT + sn;
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
        lineSource = CoreUtils.removeComments(lineSource);
        if (!lineSource.isEmpty()) {
          if (!isInBlockComment && lineSource.endsWith("*/")) {
            isInBlockComment = true;
          }
          else if (isInBlockComment && lineSource.startsWith("/*")) {
            isInBlockComment = false;
          }

          if (!isInBlockComment && lineSource.charAt(0) == '@') {
            // if the existing annotation is longer than the one to insert (to ensure the annotations get from short to long)
            if (lineSource.length() > newAnnotationLen) {
              result = lineInfo;
            }

            if (lineSource.startsWith('@' + sn) || lineSource.startsWith('@' + fqn)) {
              // the annotation that should be created already exists -> replace it
              result = lineInfo;
              isReplaceExisting = true;
              break;
            }
          }
        }
      }
      lineInfo = sourceDocument.getLineInformationOfOffset(lineInfo.getOffset() - newLineLength); // one line up
    }

    return new SourceRange(result.getOffset(), isReplaceExisting ? result.getLength() : 0);
  }

  protected static String getIndent(IDocument sourceDocument, ISourceRange replaceRange) throws BadLocationException {
    IRegion line = sourceDocument.getLineInformationOfOffset(replaceRange.getOffset());
    Matcher matcher = REGEX_WHITE_SPACE_START.matcher(sourceDocument.get(line.getOffset(), line.getLength()));
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "";
  }

  public TextEdit createEdit(IImportValidator validator, IDocument sourceDocument, String nl) throws CoreException {
    try {
      // create new source
      PropertySupport properties = S2eUtils.propertyMap(m_declaringMember.getJavaProject());
      StringBuilder src = getSourceBuilder().toJavaSource(new JavaBuilderContext(new BuilderContext(nl, properties), validator));

      // find insert/replace range
      ISourceRange replaceRange = getAnnotationReplaceRange(sourceDocument, nl, src);

      // insert indentation at the beginning
      src.insert(0, getIndent(sourceDocument, replaceRange));

      // insert newline at the end if required
      if (replaceRange.getLength() == 0) {
        src.append(nl);
      }

      return new ReplaceEdit(replaceRange.getOffset(), replaceRange.getLength(), src.toString());
    }
    catch (BadLocationException e) {
      throw new CoreException(new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "could not find insert location for annotation.", e));
    }
  }

  public IAnnotationGenerator<?> getSourceBuilder() {
    return m_sourceBuilder;
  }

  @Override
  public String toString() {
    return "Create new Annotation";
  }
}
