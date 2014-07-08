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
package org.eclipse.scout.sdk.ws.jaxws;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.commons.StringUtility;

public class JaxWsSdkStatus implements IStatus {
  private final IStatus m_status;

  public JaxWsSdkStatus(IStatus status) {
    this.m_status = status;
  }

  @Override
  public IStatus[] getChildren() {
    return m_status.getChildren();
  }

  @Override
  public int getCode() {
    return m_status.getCode();
  }

  @Override
  public Throwable getException() {
    return m_status.getException();
  }

  @Override
  public String getMessage() {
    return StringUtility.join(" > JaxWS Scout SDK", m_status.getMessage());
  }

  @Override
  public String getPlugin() {
    return m_status.getPlugin();
  }

  @Override
  public int getSeverity() {
    return m_status.getSeverity();
  }

  @Override
  public boolean isMultiStatus() {
    return m_status.isMultiStatus();
  }

  @Override
  public boolean isOK() {
    return m_status.isOK();
  }

  @Override
  public boolean matches(int severityMask) {
    return m_status.matches(severityMask);
  }
}
