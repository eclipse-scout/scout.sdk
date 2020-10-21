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
package org.eclipse.scout.sdk.s2e.operation.wellform;

import static org.eclipse.scout.sdk.s2e.environment.WorkingCopyManager.currentWorkingCopyManager;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.structured.Wellformer;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.operation.SourceFormatOperation;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

public class WellformScoutTypeOperation implements BiConsumer<EclipseEnvironment, EclipseProgress> {

  private final Set<IType> m_types;
  private final boolean m_recursive;

  public WellformScoutTypeOperation(Set<IType> types, boolean recursive) {
    m_types = new HashSet<>(types);
    m_recursive = recursive;
  }

  @Override
  public void accept(EclipseEnvironment env, EclipseProgress progress) {
    var monitor = progress.monitor();
    monitor.beginTask("Wellform classes...", m_types.size());
    for (var t : m_types) {
      if (monitor.isCanceled()) {
        return;
      }
      monitor.subTask("Wellform '" + t.getElementName() + "'.");
      try {
        wellformType(t, env);
      }
      catch (JavaModelException e) {
        SdkLog.warning("Unable to wellform type '{}'.", t.getFullyQualifiedName(), e);
      }
      monitor.worked(1);
    }
    monitor.done();
  }

  protected void wellformType(IType type, EclipseEnvironment env) throws JavaModelException {
    var typeRange = type.getSourceRange();
    if (!SourceRange.isAvailable(typeRange)) {
      return;
    }

    var icu = type.getCompilationUnit();
    var lineSeparator = icu.findRecommendedLineSeparator();
    var w = new Wellformer(lineSeparator, isRecursive());

    var sourceBuilder = new StringBuilder(typeRange.getLength() * 2);
    var scoutType = env.toScoutType(type);
    if (scoutType == null) {
      SdkLog.warning("Type '{}' could not be found. Wellforming skipped.", type);
      return;
    }
    var success = w.buildSource(scoutType, sourceBuilder);
    if (success) {
      try {
        currentWorkingCopyManager().register(icu, null);

        // apply changes
        TextEdit edit = new ReplaceEdit(typeRange.getOffset(), typeRange.getLength(), sourceBuilder.toString());
        var icuBuffer = icu.getBuffer();
        var sourceDoc = new Document(icuBuffer.getContents());
        edit.apply(sourceDoc);

        // format
        new SourceFormatOperation(type.getJavaProject(), sourceDoc).accept(null);

        // write new content to file
        icuBuffer.setContents(sourceDoc.get());
      }
      catch (JavaModelException | BadLocationException e) {
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

  @Override
  public String toString() {
    return "Wellform Scout Types";
  }
}
