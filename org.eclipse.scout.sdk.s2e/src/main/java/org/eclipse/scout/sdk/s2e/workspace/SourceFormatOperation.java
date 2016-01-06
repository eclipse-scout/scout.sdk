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
package org.eclipse.scout.sdk.s2e.workspace;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * <h3>SourceFormatOperation</h3> equivalent to CONT-SHIFT-F in Eclipse ...
 */
public class SourceFormatOperation implements IOperation {
  private final IJavaProject m_project;
  private Document m_document;
  private ISourceRange m_range;
  private int m_indent;

  public SourceFormatOperation(ICompilationUnit icu) throws JavaModelException {
    this(icu.getJavaProject(), new Document(icu.getSource()), icu.getSourceRange());
  }

  public SourceFormatOperation(IJavaProject project, Document document) {
    this(project, document, new SourceRange(0, document.getLength()));
  }

  public SourceFormatOperation(IJavaProject project, Document document, ISourceRange range) {
    m_indent = 0;
    m_project = project;
    m_document = document;
    m_range = range;
  }

  @Override
  public String getOperationName() {
    return "Format source";
  }

  @Override
  public void validate() {
    if (getProject() == null) {
      throw new IllegalArgumentException("java project can not be null.");
    }
    if (getDocument() == null) {
      throw new IllegalArgumentException("document can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (monitor.isCanceled()) {
      return;
    }
    try {
      Document document = getDocument();
      ISourceRange range = getRange();
      if (range == null) {
        // create full range
        range = new SourceRange(0, document.getLength());
      }
      CodeFormatter formatter = ToolFactory.createCodeFormatter(getProject().getOptions(true));
      int kind = CodeFormatter.F_INCLUDE_COMMENTS | CodeFormatter.K_UNKNOWN;
      String defaultLineDelimiter = document.getDefaultLineDelimiter();
      if (defaultLineDelimiter == null) {
        defaultLineDelimiter = S2eUtils.lineSeparator(getProject());
      }
      TextEdit te = formatter.format(kind, document.get(), range.getOffset(), range.getLength(), m_indent, defaultLineDelimiter);
      if (te != null) {
        te.apply(document);
      }
    }
    catch (MalformedTreeException e) {
      SdkLog.error(e);
    }
    catch (IllegalArgumentException e) {
      SdkLog.error(e);
    }
    catch (BadLocationException e) {
      SdkLog.error(e);
    }
  }

  public ISourceRange getRange() {
    return m_range;
  }

  public void setRange(ISourceRange range) {
    m_range = range;
  }

  public Document getDocument() {
    return m_document;
  }

  public void setDocument(Document document) {
    m_document = document;
  }

  public int getIndent() {
    return m_indent;
  }

  public void setIndent(int indent) {
    m_indent = indent;
  }

  public IJavaProject getProject() {
    return m_project;
  }
}
