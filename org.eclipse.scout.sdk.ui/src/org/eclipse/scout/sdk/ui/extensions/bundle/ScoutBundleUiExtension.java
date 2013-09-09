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
package org.eclipse.scout.sdk.ui.extensions.bundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

/**
 *
 */
public class ScoutBundleUiExtension {

  private int m_orderNumber;
  private String m_bundleID;
  private String m_bundleName;
  private INewScoutBundleHandler m_bundleExtention;
  private IProductLauncherContributor m_productLauncherContributor;
  private Class<? extends IPage> m_bundlePageClass;
  private ImageDescriptor m_icon;
  private ImageDescriptor m_launcherIcon;
  private String m_bundleType;

  public void setBundleID(String bundleID) {
    m_bundleID = bundleID;
  }

  public String getBundleId() {
    return m_bundleID;
  }

  public void setBundleName(String bundleName) {
    m_bundleName = bundleName;
  }

  public String getBundleName() {
    return m_bundleName;
  }

  public void setOrderNumber(int orderNumber) {
    m_orderNumber = orderNumber;
  }

  public int getOrderNumber() {
    return m_orderNumber;
  }

  public void setNewScoutBundleHandler(INewScoutBundleHandler bundleExtenion) {
    m_bundleExtention = bundleExtenion;
  }

  public INewScoutBundleHandler getNewScoutBundleHandler() {
    return m_bundleExtention;
  }

  public void setIcon(ImageDescriptor icon) {
    m_icon = icon;
  }

  public ImageDescriptor getIcon() {
    return m_icon;
  }

  public void setBundleType(String bundleType) {
    m_bundleType = bundleType;
  }

  public String getBundleType() {
    return m_bundleType;
  }

  public boolean isValidConfiguration() {
    if (!StringUtility.hasText(getBundleName())) {
      return false;
    }
    if (getNewScoutBundleHandler() == null) {
      return false;
    }
    if (!StringUtility.hasText(getBundleId())) {
      return false;
    }
    if (!StringUtility.hasText(getBundleType())) {
      return false;
    }
    return true;
  }

  public Class<? extends IPage> getBundlePageClass() {
    return m_bundlePageClass;
  }

  public void setBundlePageClass(Class<? extends IPage> bundlePageClass) {
    m_bundlePageClass = bundlePageClass;
  }

  public ImageDescriptor getLauncherIconPath() {
    return m_launcherIcon;
  }

  public void setLauncherIconPath(ImageDescriptor launcherIcon) {
    m_launcherIcon = launcherIcon;
  }

  public IProductLauncherContributor getProductLauncherContributor() {
    return m_productLauncherContributor;
  }

  public void setProductLauncherContributor(IProductLauncherContributor productLauncherContributor) {
    m_productLauncherContributor = productLauncherContributor;
  }
}
