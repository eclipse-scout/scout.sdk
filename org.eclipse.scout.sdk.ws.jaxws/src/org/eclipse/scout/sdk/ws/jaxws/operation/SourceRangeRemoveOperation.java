/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.operation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.text.edits.ReplaceEdit;

public class SourceRangeRemoveOperation implements IOperation {

  private IType m_declaringType;
  private IAnnotation m_annotation;

  @Override
  public void validate() {
    if (m_declaringType == null) {
      throw new IllegalArgumentException("declaring must not be null");
    }
    if (m_annotation == null) {
      throw new IllegalArgumentException("annotation must not be null");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ICompilationUnit icu = m_declaringType.getCompilationUnit();
    String source = m_declaringType.getCompilationUnit().getBuffer().getContents();
    ISourceRange sourceRange = m_annotation.getSourceRange();

    Document icuDoc = new Document(source);
    ReplaceEdit edit = new ReplaceEdit(sourceRange.getOffset(), sourceRange.getLength(), "");
    try {
      edit.apply(icuDoc);
    }
    catch (BadLocationException e) {
      throw new CoreException(new ScoutStatus("Failed to remve annotation", e));
    }

    workingCopyManager.register(icu, monitor);

    // format icu
    SourceFormatOperation sourceFormatOp = new SourceFormatOperation(m_declaringType.getJavaProject(), icuDoc, null);
    sourceFormatOp.run(monitor, workingCopyManager);

    // write document back
    icu.getBuffer().setContents(ScoutUtility.cleanLineSeparator(icuDoc.get(), icuDoc));
  }

  @Override
  public String getOperationName() {
    return SourceRangeRemoveOperation.class.getName();
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public void setDeclaringType(IType declaringType) {
    m_declaringType = declaringType;
  }

  public IAnnotation getAnnotation() {
    return m_annotation;
  }

  public void setAnnotation(IAnnotation annotation) {
    m_annotation = annotation;
  }
}
