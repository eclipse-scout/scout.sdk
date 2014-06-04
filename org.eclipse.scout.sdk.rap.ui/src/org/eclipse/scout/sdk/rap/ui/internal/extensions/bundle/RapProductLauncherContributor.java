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
package org.eclipse.scout.sdk.rap.ui.internal.extensions.bundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.ui.extensions.bundle.IProductLauncherContributor;
import org.eclipse.scout.sdk.ui.internal.extensions.bundle.ServerProductLauncherContributor;
import org.eclipse.scout.sdk.ui.internal.extensions.bundle.ServerProductLauncherContributor.JettyProductUrlOpenLink;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.ProductLaunchPresenter;

public class RapProductLauncherContributor implements IProductLauncherContributor {

  private JettyProductUrlOpenLink m_deviceDispatchLink;
  private JettyProductUrlOpenLink m_desktopLink;
  private JettyProductUrlOpenLink m_smartphoneLink;
  private JettyProductUrlOpenLink m_tabletLink;

  public RapProductLauncherContributor() {
  }

  @Override
  public void contributeLinks(IFile productFile, LinksPresenterModel model) throws CoreException {
    boolean isJettyConfigured = ServerProductLauncherContributor.getJettyBaseUrl(productFile) != null;
    if (isJettyConfigured) {
      m_deviceDispatchLink = new JettyProductUrlOpenLink("Automatic Device Dispatch", productFile, "", 30);
      m_desktopLink = new JettyProductUrlOpenLink("Desktop Devices", productFile, "web", 40);
      m_smartphoneLink = new JettyProductUrlOpenLink("Smartphone Devices", productFile, "mobile", 50);
      m_tabletLink = new JettyProductUrlOpenLink("Tablet Devices", productFile, "tablet", 60);

      model.addGlobalLink(m_deviceDispatchLink);
      model.addGlobalLink(m_desktopLink);
      model.addGlobalLink(m_smartphoneLink);
      model.addGlobalLink(m_tabletLink);
    }
  }

  @Override
  public void refreshLaunchState(String mode) {
    if (m_deviceDispatchLink != null && m_desktopLink != null && m_smartphoneLink != null && m_tabletLink != null) {
      boolean running = !ProductLaunchPresenter.TERMINATED_MODE.equals(mode);
      m_deviceDispatchLink.setEnabled(running);
      m_desktopLink.setEnabled(running);
      m_smartphoneLink.setEnabled(running);
      m_tabletLink.setEnabled(running);
    }
  }
}
