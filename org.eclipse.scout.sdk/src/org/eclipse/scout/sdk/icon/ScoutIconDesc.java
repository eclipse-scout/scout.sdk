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
package org.eclipse.scout.sdk.icon;

import org.eclipse.jdt.core.IField;
import org.eclipse.jface.resource.ImageDescriptor;

public class ScoutIconDesc {

  private final String m_id;
  private final String m_iconName;
  private ImageDescriptor m_imgDesc;
  private IField m_constantField;
  private boolean m_inherited;

  public ScoutIconDesc(String id, String iconName, IField constantField, boolean inherited) {
    m_inherited = inherited;
    m_id = id;
    m_iconName = iconName;
    m_constantField = constantField;
  }

  public boolean isInherited() {
    return m_inherited;
  }

  public String getId() {
    return m_id;
  }

  public ImageDescriptor getImageDescriptor() {
    return m_imgDesc;
  }

  /**
   * @param imgDesc
   *          the imgDesc to set
   */
  public void setImgDesc(ImageDescriptor imgDesc) {
    m_imgDesc = imgDesc;
  }

  public String getIconName() {
    return m_iconName;
  }

  public IField getConstantField() {
    return m_constantField;
  }

}
