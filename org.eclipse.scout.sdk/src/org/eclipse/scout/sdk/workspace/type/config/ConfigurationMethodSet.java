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
package org.eclipse.scout.sdk.workspace.type.config;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ConfigurationMethodSet {

  private List<ConfigurationMethod> m_methods;
  private final String m_methodName;
  private final String m_configAnnotationType;

  public ConfigurationMethodSet(String methodName, String configAnnotationType) {
    m_methodName = methodName;
    m_configAnnotationType = configAnnotationType;
    m_methods = new ArrayList<ConfigurationMethod>();
  }

  public void add(ConfigurationMethod m) {
    m_methods.add(m);
  }

  public void remove(ConfigurationMethod m) {
    m_methods.remove(m);
  }

  public ConfigurationMethod[] getMethods() {
    return m_methods.toArray(new ConfigurationMethod[m_methods.size()]);
  }

  public String getMethodName() {
    return m_methodName;
  }

  public String getConfigAnnotationType() {
    return m_configAnnotationType;
  }
}
