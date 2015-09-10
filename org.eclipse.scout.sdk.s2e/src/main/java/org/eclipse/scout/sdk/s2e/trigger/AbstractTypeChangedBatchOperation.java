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

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.core.util.CoreUtils;

/**
 *
 */
public abstract class AbstractTypeChangedBatchOperation implements ITypeChangedOperation {
  private final IJavaEnvironmentProvider m_envProvider;
  private final Collection<org.eclipse.jdt.core.IType> m_jdtTypes;

  protected AbstractTypeChangedBatchOperation(Collection<org.eclipse.jdt.core.IType> jdtTypes, IJavaEnvironmentProvider envProvider) {
    m_jdtTypes = jdtTypes;
    m_envProvider = envProvider;
  }

  protected final IJavaEnvironmentProvider getJavaEnvironmentProvider() {
    return m_envProvider;
  }

  protected final Collection<org.eclipse.jdt.core.IType> getJdtTypes() {
    return m_jdtTypes;
  }

  @Override
  public void validate() {
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
    return 1;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    return true;
  }
}
