/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.operation;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.imports.IImportCollector;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link ImportsCreateOperation}</h3>
 *
 * @since 4.0.0 2014-03-27
 */
public class ImportsCreateOperation implements BiConsumer<EclipseEnvironment, EclipseProgress> {

  private final Set<String> m_importsToCreate;
  private final ICompilationUnit m_icu;

  public ImportsCreateOperation(ICompilationUnit icu) {
    m_importsToCreate = new TreeSet<>();
    m_icu = icu;
  }

  public ImportsCreateOperation(ICompilationUnit icu, IImportCollector validator) {
    this(icu);
    if (validator != null) {
      validator.getImports()
          .map(StringBuilder::toString)
          .forEach(this::addImportToCreate);
    }
  }

  @Override
  public void accept(EclipseEnvironment env, EclipseProgress progress) {
    Ensure.isTrue(JdtUtils.exists(getCompilationUnit()));
    try {
      for (var s : m_importsToCreate) {
        getCompilationUnit().createImport(s, null, progress.monitor());
      }
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }

  public ICompilationUnit getCompilationUnit() {
    return m_icu;
  }

  public boolean addImportToCreate(String imp) {
    return m_importsToCreate.add(imp);
  }

  public boolean removeImportToCreate(String imp) {
    return m_importsToCreate.remove(imp);
  }

  public void setImportsToCreate(Collection<String> imports) {
    m_importsToCreate.clear();
    m_importsToCreate.addAll(imports);
  }

  @Override
  public String toString() {
    return "Create imports";
  }
}
