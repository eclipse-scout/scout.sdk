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
package org.eclipse.scout.sdk.compatibility;

/**
 * <h3>{@link License}</h3>
 *
 * @author Matthias Villiger
 * @since 3.8.0 16.02.2012
 */
public class License {
  private String m_title;
  private String m_body;
  private String m_iuId;

  public License(String body, String iuId) {
    m_body = body;
    m_iuId = iuId;
    m_title = getFirstLine(body);
  }

  public String getTitle() {
    return m_title;
  }

  public void setTitle(String title) {
    m_title = title;
  }

  public String getBody() {
    return m_body;
  }

  public void setBody(String body) {
    m_body = body;
  }

  public String getInstallableUnitId() {
    return m_iuId;
  }

  public void setInstallableUnitId(String iuId) {
    m_iuId = iuId;
  }

  private String getFirstLine(String body) {
    int i = body.indexOf('\n');
    int j = body.indexOf('\r');
    if (i > 0) {
      if (j > 0) return body.substring(0, i < j ? i : j);
      return body.substring(0, i);
    }
    else if (j > 0) {
      return body.substring(0, j);
    }
    return body;
  }
}
