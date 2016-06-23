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
package org.eclipse.scout.sdk.s2e;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

public class CachingJavaEnvironmentProvider implements IJavaEnvironmentProvider {
  private final Map<IJavaProject, IJavaEnvironment> m_envs;

  public CachingJavaEnvironmentProvider() {
    m_envs = new HashMap<>();
  }

  @Override
  public IType jdtTypeToScoutType(org.eclipse.jdt.core.IType jdtType) {
    if (jdtType == null) {
      return null;
    }
    return S2eUtils.jdtTypeToScoutType(jdtType, getOrCreateEnv(jdtType.getJavaProject()));
  }

  @Override
  public IJavaEnvironment get(IJavaProject jdtProject) {
    return getOrCreateEnv(jdtProject);
  }

  protected IJavaEnvironment getOrCreateEnv(IJavaProject jdtProject) {
    if (jdtProject == null) {
      return null;
    }
    IJavaEnvironment env = m_envs.get(jdtProject);
    if (env == null) {
      env = createNew(jdtProject);
      m_envs.put(jdtProject, env);
    }
    return env;
  }

  protected IJavaEnvironment createNew(IJavaProject jdtProject) {
    return ScoutSdkCore.createJavaEnvironment(jdtProject);
  }
}
