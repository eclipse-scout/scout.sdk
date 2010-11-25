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
package org.eclipse.scout.sdk.operation.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.javaeditor.IndentUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.internal.jdt.LineRange;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * <h3>SourceIndentationFormatter</h3> ...
 */
public class SourceIndentationFormatter implements IOperation {

  private final IJavaProject m_project;
  private final Document m_document;
  private final ILineRange m_range;

  public SourceIndentationFormatter(IJavaProject project, Document document, ILineRange range) {
    m_project = project;
    m_document = document;
    if (range == null) {
      // entire document
      range = new LineRange(0, document.getNumberOfLines());
    }
    m_range = range;

  }

  public String getOperationName() {
    return "Formatting indents...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getProject() == null) {
      throw new IllegalArgumentException("java project can not be null.");
    }
    if (getDocument() == null) {
      throw new IllegalArgumentException("document can not be null.");
    }
  }

  @SuppressWarnings("restriction")
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      IndentUtil.indentLines(m_document, m_range, getProject(), null);
    }
    catch (BadLocationException e) {
      ScoutSdk.logWarning("could not wellform indents.", e);
    }
  }

  public Document getDocument() {
    return m_document;
  }

  public IJavaProject getProject() {
    return m_project;
  }

}
