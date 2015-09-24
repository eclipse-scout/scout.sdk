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
package org.eclipse.scout.sdk.s2e.trigger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.CoreUtils;

/**
 *
 */
public abstract class AbstractDerivedResourceOperation implements IDerivedResourceOperation {
  private final IJavaEnvironmentProvider m_envProvider;
  private final org.eclipse.jdt.core.IType m_jdtType;
  private final IType m_modelType;

  protected AbstractDerivedResourceOperation(org.eclipse.jdt.core.IType jdtType, IJavaEnvironmentProvider envProvider) throws CoreException {
    m_jdtType = jdtType;
    m_envProvider = envProvider;
    m_modelType = m_envProvider.jdtTypeToScoutType(jdtType);
  }

  protected final org.eclipse.jdt.core.IType getJdtType() {
    return m_jdtType;
  }

  protected final IJavaEnvironmentProvider getJavaEnvironmentProvider() {
    return m_envProvider;
  }

  protected final IType getModelType() {
    return m_modelType;
  }

  @Override
  public void validate() {
    if (m_jdtType == null || !m_jdtType.exists()) {
      throw new IllegalArgumentException("jdt type must exist: [" + (m_jdtType != null ? m_jdtType.getFullyQualifiedName() : null) + "]");
    }
    if (m_modelType == null) {
      throw new IllegalArgumentException("model type must exist: [" + m_modelType + "]");
    }
  }

  @Override
  public void run(IProgressMonitor monitor) throws CoreException {
    String backup = CoreUtils.getUsername();
    try {
      CoreUtils.setUsernameForThread("Scout robot");
      runImpl(monitor);
    }
    finally {
      CoreUtils.setUsernameForThread(backup);
    }
  }

  protected abstract void runImpl(IProgressMonitor monitor) throws CoreException;

  @Override
  public int hashCode() {
    return m_modelType.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    return m_modelType.equals(((AbstractDerivedResourceOperation) obj).m_modelType);
  }
}
