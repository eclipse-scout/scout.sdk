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
package org.eclipse.scout.sdk.s2e.operation.wellform;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.core.s.structured.Wellformer;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.operation.SourceFormatOperation;
import org.eclipse.text.edits.ReplaceEdit;

/**
 *
 */
public class WellformScoutTypeOperation implements IOperation {

  private final Set<IType> m_types;
  private final boolean m_recursive;
  private final IJavaEnvironmentProvider m_envProvider;

  public WellformScoutTypeOperation(Set<IType> types, boolean recursive) {
    m_types = new HashSet<>(types);
    m_recursive = recursive;
    m_envProvider = new CachingJavaEnvironmentProvider();
  }

  @Override
  public String getOperationName() {
    StringBuilder builder = new StringBuilder();
    builder.append("Wellform ");
    if (m_types.size() > 0) {
      int i = 0;
      builder.append('\'');
      for (IType t : m_types) {
        builder.append(t.getElementName());
        if (i < 2) {
          builder.append(", ");
        }
        else if (i == 2) {
          break;
        }
        i++;
      }
      builder.append('\'');
    }
    builder.append("...");
    return builder.toString();
  }

  @Override
  public void validate() {
    // nothing to validate
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    monitor.beginTask("Wellform classes...", m_types.size());
    for (IType t : m_types) {
      if (monitor.isCanceled()) {
        return;
      }
      monitor.subTask("Wellform '" + t.getElementName() + "'.");
      wellformType(t, workingCopyManager);
      monitor.worked(1);
    }
    monitor.done();
  }

  protected void wellformType(IType type, IWorkingCopyManager workingCopyManager) throws CoreException {
    ISourceRange typeRange = type.getSourceRange();
    if (!SourceRange.isAvailable(typeRange)) {
      return;
    }

    ICompilationUnit icu = type.getCompilationUnit();
    String lineSeparator = icu.findRecommendedLineSeparator();
    Wellformer w = new Wellformer(lineSeparator, isRecursive());

    StringBuilder sourceBuilder = new StringBuilder(typeRange.getLength() * 2);
    boolean success = w.buildSource(m_envProvider.jdtTypeToScoutType(type), sourceBuilder);
    if (success) {
      try {
        workingCopyManager.register(icu, null);

        // apply changes
        ReplaceEdit edit = new ReplaceEdit(typeRange.getOffset(), typeRange.getLength(), sourceBuilder.toString());
        IBuffer icuBuffer = icu.getBuffer();
        Document sourceDoc = new Document(icuBuffer.getContents());
        edit.apply(sourceDoc);

        // format
        SourceFormatOperation op = new SourceFormatOperation(type.getJavaProject(), sourceDoc);
        op.run(null, workingCopyManager);

        // write new content to file
        icuBuffer.setContents(sourceDoc.get());
      }
      catch (Exception e) {
        SdkLog.warning("Could not wellform type '{}'.", type.getFullyQualifiedName(), e);
      }
    }
    else {
      SdkLog.warning("Unable to get source of type '{}'. Skipping wellform for this type.", type.getFullyQualifiedName());
    }
  }

  public boolean isRecursive() {
    return m_recursive;
  }
}
