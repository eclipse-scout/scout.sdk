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

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleExtensionPoint;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleUiExtension;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerSettingsSupport;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link ScoutBundleNode}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.9.0 12.02.2013
 */
public class ScoutBundleNode {

  private final String m_symbolicName;
  private final String m_type;
  private final Set<ScoutBundleNode> m_childNodes;
  private final ScoutBundleUiExtension m_uiExtension;
  private final IScoutBundle m_scoutBundle;

  public ScoutBundleNode(IScoutBundle bundle, ScoutBundleUiExtension uiExtension) {
    m_scoutBundle = bundle;
    m_symbolicName = bundle.getSymbolicName();
    m_type = bundle.getType();
    m_childNodes = new HashSet<ScoutBundleNode>();
    m_uiExtension = uiExtension;
    if (!ScoutExplorerSettingsSupport.BundlePresentation.FLAT.equals(ScoutExplorerSettingsSupport.get().getBundlePresentation())) {
      for (IScoutBundle child : bundle.getDirectChildBundles()) {
        if (ScoutExplorerSettingsBundleFilter.get().accept(child)) {
          ScoutBundleUiExtension childExt = ScoutBundleExtensionPoint.getExtension(child.getType());
          if (childExt != null) {
            m_childNodes.add(new ScoutBundleNode(child, childExt));
          }
        }
      }
    }
  }

  public String getSymbolicName() {
    return m_symbolicName;
  }

  public String getType() {
    return m_type;
  }

  public void removeChildBundle(ScoutBundleNode node) {
    m_childNodes.remove(node);
  }

  public Set<ScoutBundleNode> getChildBundles() {
    return m_childNodes;
  }

  public boolean containsBundle(ScoutBundleNode node) {
    for (ScoutBundleNode child : getChildBundles()) {
      if (node.equals(child)) {
        return true;
      }
      if (child.containsBundle(node)) {
        return true;
      }
    }
    return false;
  }

  public IPage createBundlePage(IPage parentPage) {
    Class<? extends IPage> bundleNodePageClass = getUiExtension().getBundlePageClass();
    if (bundleNodePageClass != null) {
      try {
        Constructor<? extends IPage> constructor = bundleNodePageClass.getConstructor(IPage.class, ScoutBundleNode.class);
        return constructor.newInstance(parentPage, this);
      }
      catch (Exception e) {
        ScoutSdkUi.logError("Unable to create scout bundle table page.", e);
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return getSymbolicName();
  }

  @Override
  public int hashCode() {
    return m_symbolicName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ScoutBundleNode) {
      return CompareUtility.equals(((ScoutBundleNode) obj).m_symbolicName, m_symbolicName);
    }
    else {
      return false;
    }
  }

  public ScoutBundleUiExtension getUiExtension() {
    return m_uiExtension;
  }

  public IScoutBundle getScoutBundle() {
    return m_scoutBundle;
  }
}
