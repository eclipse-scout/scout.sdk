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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared;

import java.util.Set;

import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.delete.MemberListDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.PropertyBeansRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.type.IPropertyBean;

public class SharedContextPropertyNodePage extends AbstractPage {
  private final IPropertyBean m_serverDesc;
  private final IPropertyBean m_clientDesc;

  public SharedContextPropertyNodePage(IPage parent, IPropertyBean clientDesc, IPropertyBean serverDesc) {
    setParent(parent);
    m_clientDesc = clientDesc;
    m_serverDesc = serverDesc;
    IPropertyBean desc = m_serverDesc;
    if (desc == null) {
      desc = m_clientDesc;
    }
    setName(NamingUtility.ensureStartWithLowerCase(desc.getBeanName()) + " (" + Signature.getSignatureSimpleName(desc.getBeanSignature()) + ")");
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Variable));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SHARED_CONTEXT_PROPERTY_NODE_PAGE;
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(PropertyBeansRenameAction.class, MemberListDeleteAction.class);
  }

  public IPropertyBean getServerDesc() {
    return m_serverDesc;
  }

  public IPropertyBean getClientDesc() {
    return m_clientDesc;
  }
}
