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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.axis;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

public class AxisWebServiceProviderTablePage extends AbstractPage {

  final IType iService = ScoutSdk.getType(RuntimeClasses.IService);
  ICachedTypeHierarchy m_serviceHierarchy;

  public AxisWebServiceProviderTablePage(AbstractPage parent) {
    setParent(parent);
    setName(Texts.get("AxisWebServiceProviderTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Webservices));
  }

  @Override
  public void unloadPage() {
    if (m_serviceHierarchy != null) {
      m_serviceHierarchy.removeHierarchyListener(getPageDirtyListener());
    }
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.AXIS_WEB_SERVICE_PROVIDER_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  /**
   * server bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void loadChildrenImpl() {
    if (m_serviceHierarchy == null) {
      m_serviceHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iService);
      m_serviceHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    IType[] services = m_serviceHierarchy.getAllSubtypes(iService, new P_AxisFilter(getScoutResource()), TypeComparators.getTypeNameComparator());
    for (IType service : services) {
      new AxisWebServiceProviderNodePage(this, service);
    }
  }

  private class P_AxisFilter implements ITypeFilter {
    private final IScoutBundle m_bundle;

    private P_AxisFilter(IScoutBundle scoutBundle) {
      m_bundle = scoutBundle;

    }

    @Override
    public boolean accept(IType type) {
      return TypeUtility.exists(type) && m_bundle.contains(type) && type.getCompilationUnit().getResource().getParent().exists(new Path(type.getElementName() + "-deploy.wsdd"));
    }

  }
}
