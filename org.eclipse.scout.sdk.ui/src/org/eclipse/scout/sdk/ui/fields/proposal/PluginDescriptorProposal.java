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
package org.eclipse.scout.sdk.ui.fields.proposal;

import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

public class PluginDescriptorProposal implements IContentProposalEx {

  private final IPluginModelBase m_pluginBase;
  private String m_label;

  public PluginDescriptorProposal(IPluginModelBase pluginBase) {
    m_pluginBase = pluginBase;
    m_label = getPluginBase().getBundleDescription().getName();
  }

  @Override
  public int getCursorPosition(boolean selected, boolean expertMode) {
    return m_label.length();
  }

  @Override
  public Image getImage(boolean selected, boolean expertMode) {
    return ScoutSdkUi.getImage(ScoutSdkUi.SharedBundle);
  }

  @Override
  public String getLabel(boolean selected, boolean expertMode) {
    return m_label;
  }

  public IPluginModelBase getPluginBase() {
    return m_pluginBase;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PluginDescriptorProposal) {
      return ((PluginDescriptorProposal) obj).getPluginBase().getBundleDescription().getName().equals(getPluginBase().getBundleDescription().getName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getPluginBase().getBundleDescription().getName().hashCode();
  }

}
