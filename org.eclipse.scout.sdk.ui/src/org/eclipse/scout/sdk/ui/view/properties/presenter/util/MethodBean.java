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
package org.eclipse.scout.sdk.ui.view.properties.presenter.util;

import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;

/**
 * <h3>MethodBean</h3>
 *
 * @param <T>
 */
public class MethodBean<T> {

  private final String m_identifier;
  private T m_defaultValue;
  private T m_currentSourceValue;
  private ConfigurationMethod m_method;

  public MethodBean(ConfigurationMethod method, String identifier) {
    m_method = method;
    m_identifier = identifier;
  }

  public T getCurrentSourceValue() {
    return m_currentSourceValue;
  }

  public T getDefaultValue() {
    return m_defaultValue;
  }

  public String getSource() {
    return m_method.getSource();
  }

  public String getIdentifier() {
    return m_identifier;
  }

  public void setCurrentSourceValue(T currentSourceValue) {
    m_currentSourceValue = currentSourceValue;
  }

  public void setDefaultValue(T defaultValue) {
    m_defaultValue = defaultValue;
  }

  public ConfigurationMethod getMethod() {
    return m_method;
  }

  public void setMethod(ConfigurationMethod method) {
    m_method = method;
  }
}
