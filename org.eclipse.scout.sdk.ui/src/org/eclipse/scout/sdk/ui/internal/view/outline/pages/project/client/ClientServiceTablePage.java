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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.ClientServiceNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

public class ClientServiceTablePage extends AbstractPage {

  private ICachedTypeHierarchy m_servieHierarchy;

  public ClientServiceTablePage(IPage parentPage) {
    setParent(parentPage);
    setName(Texts.get("ClientServicesNodePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Services));
  }

  @Override
  public void unloadPage() {
    if (m_servieHierarchy != null) {
      m_servieHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_servieHierarchy = null;
    }
    super.unloadPage();
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_servieHierarchy != null) {
      m_servieHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CLIENT_SERVICE_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void loadChildrenImpl() {
    IType iService = TypeUtility.getType(IRuntimeClasses.IService);

    if (m_servieHierarchy == null) {
      m_servieHierarchy = TypeUtility.getPrimaryTypeHierarchy(iService);
      m_servieHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    IType[] serviceTypes = m_servieHierarchy.getAllSubtypes(iService, ScoutTypeFilters.getTypesInScoutBundles(getScoutBundle()), TypeComparators.getTypeNameComparator());
    for (IType type : serviceTypes) {
      IType serviceInterface = null;
      IType[] interfaces = m_servieHierarchy.getSuperInterfaces(type, TypeFilters.getElementNameFilter("I" + type.getElementName()));
      if (interfaces.length > 0) {
        serviceInterface = interfaces[0];
      }

      new ClientServiceNodePage(this, type, serviceInterface);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ClientServiceNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    ((ClientServiceNewAction) menu).setScoutBundle(getScoutBundle());
  }
}
