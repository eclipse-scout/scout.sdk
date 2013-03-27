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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.SdkIcons;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>UiSwtNodePage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2010
 */
public abstract class AbstractBundleNodeTablePage extends AbstractPage {

  private final ScoutBundleNode m_bundle;

  public AbstractBundleNodeTablePage(IPage parentPage, ScoutBundleNode bundle) {
    m_bundle = bundle;
    setParent(parentPage);
    setName(m_bundle.getSymbolicName());

    ImageDescriptor icon = m_bundle.getUiExtension().getIcon();
    if (bundle.getScoutBundle().isBinary()) {
      icon = ScoutSdkUi.getImageDescriptor(icon, SdkIcons.BinaryDecorator, IDecoration.BOTTOM_LEFT);
    }
    setImageDescriptor(icon);
  }

  @Override
  public final int getOrder() {
    return m_bundle.getUiExtension().getOrderNumber();
  }

  @Override
  public final boolean isInitiallyLoaded() {
    return true;
  }

  @Override
  public void loadChildrenImpl() {
    for (ScoutBundleNode b : m_bundle.getChildBundles()) {
      b.createBundlePage(this);
    }
  }

  @Override
  public final boolean isFolder() {
    return true;
  }

  @Override
  public final IScoutBundle getScoutBundle() {
    return m_bundle.getScoutBundle();
  }
}
