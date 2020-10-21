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
package org.eclipse.scout.sdk.s2e.operation;

import java.util.function.Consumer;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.text.edits.MalformedTreeException;

/**
 * <h3>SourceFormatOperation</h3> equivalent to CTRL-SHIFT-F in Eclipse
 */
public class SourceFormatOperation implements Consumer<EclipseProgress> {
  private final IJavaProject m_project;
  private final Document m_document;
  private final ISourceRange m_range;

  public SourceFormatOperation(ICompilationUnit icu) throws JavaModelException {
    this(icu.getJavaProject(), new Document(icu.getSource()), icu.getSourceRange());
  }

  public SourceFormatOperation(IJavaProject project, Document document) {
    this(project, document, new SourceRange(0, document.getLength()));
  }

  public SourceFormatOperation(IJavaProject project, Document document, ISourceRange range) {
    m_project = project;
    m_document = document;
    m_range = range;
  }

  @Override
  public void accept(EclipseProgress progress) {
    Ensure.notNull(getProject());
    Ensure.notNull(getDocument());

    try {
      var document = getDocument();
      var range = getRange();
      if (range == null) {
        // create full range
        range = new SourceRange(0, document.getLength());
      }
      var formatter = ToolFactory.createCodeFormatter(getProject().getOptions(true));
      var defaultLineDelimiter = document.getDefaultLineDelimiter();
      if (defaultLineDelimiter == null) {
        defaultLineDelimiter = Util.getLineSeparator(null, getProject());
      }
      var te = formatter.format(CodeFormatter.F_INCLUDE_COMMENTS, document.get(), range.getOffset(), range.getLength(), 0, defaultLineDelimiter);
      if (te != null) {
        te.apply(document);
      }
    }
    catch (MalformedTreeException | BadLocationException e) {
      throw new SdkException(e);
    }
  }

  public ISourceRange getRange() {
    return m_range;
  }

  public Document getDocument() {
    return m_document;
  }

  public IJavaProject getProject() {
    return m_project;
  }

  @Override
  public String toString() {
    return "Format source";
  }
}
