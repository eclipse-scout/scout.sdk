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
package org.eclipse.scout.sdk.ui.internal.extensions.bundle;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.extensions.bundle.IScoutBundleProvider;
import org.eclipse.scout.sdk.ui.extensions.project.IScoutBundleExtension;

/**
 *
 */
public class ScoutBundleExtension implements IScoutBundleExtension {

  private int m_orderNumber;
  private String m_bundleID;
  private String m_bundleName;
  private IScoutBundleProvider m_bundleExtention;
  private String m_iconPath;
  private BundleTypes m_bundleType;

  void setBundleID(String bundleID) {
    m_bundleID = bundleID;
  }

  @Override
  public String getBundleID() {
    return m_bundleID;
  }

  void setBundleName(String bundleName) {
    m_bundleName = bundleName;
  }

  @Override
  public String getBundleName() {
    return m_bundleName;
  }

  void setOrderNumber(int orderNumber) {
    m_orderNumber = orderNumber;
  }

  @Override
  public int getOrderNumber() {
    return m_orderNumber;
  }

  /**
   * @param createExecutableExtension
   */
  void setBundleExtension(IScoutBundleProvider bundleExtenion) {
    m_bundleExtention = bundleExtenion;
  }

  @Override
  public IScoutBundleProvider getBundleExtention() {
    return m_bundleExtention;
  }

  void setIconPath(String iconPath) {
    m_iconPath = iconPath;
  }

  @Override
  public String getIconPath() {
    return m_iconPath;
  }

  public void setBundleType(BundleTypes bundleType) {
    m_bundleType = bundleType;
  }

  @Override
  public BundleTypes getBundleType() {
    return m_bundleType;
  }

  boolean isValidConfiguration() {
    if (StringUtility.isNullOrEmpty(getBundleName())) {
      return false;
    }
    if (getBundleExtention() == null) {
      return false;
    }
    if (StringUtility.isNullOrEmpty(getBundleID())) {
      return false;
    }
    if (getBundleType() == null) {
      return false;
    }
    return true;
  }

}
