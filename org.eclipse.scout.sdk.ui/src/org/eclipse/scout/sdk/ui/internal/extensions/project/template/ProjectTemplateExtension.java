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
package org.eclipse.scout.sdk.ui.internal.extensions.project.template;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.extensions.project.template.IProjectTemplate;

/**
 *
 */
public class ProjectTemplateExtension {

  private String m_extensionId;
  private String m_iconPath;
  private IProjectTemplate m_template;
  private long m_orderNr;

  public String getExtensionId() {
    return m_extensionId;
  }

  public void setExtensionId(String extensionId) {
    m_extensionId = extensionId;
  }

  public String getIconPath() {
    return m_iconPath;
  }

  public void setIconPath(String iconPath) {
    m_iconPath = iconPath;
  }

  public IProjectTemplate getTemplate() {
    return m_template;
  }

  public void setTemplate(IProjectTemplate template) {
    m_template = template;
  }

  public void setOrderNr(long orderNr) {
    m_orderNr = orderNr;
  }

  public long getOrderNr() {
    return m_orderNr;
  }

  /**
   * @return
   */
  public boolean isValidConfiguration() {
    if (getTemplate() == null) {
      return false;
    }
    if (StringUtility.isNullOrEmpty(getExtensionId())) {
      return false;
    }
    return true;
  }
}
