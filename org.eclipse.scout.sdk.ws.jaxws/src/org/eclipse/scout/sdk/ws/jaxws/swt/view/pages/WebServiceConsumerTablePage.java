/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.pages;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.ConsumerNewWizardAction;

public class WebServiceConsumerTablePage extends AbstractPage {

  private IScoutBundle m_bundle; // necessary to be hold as in method unloadPage, a reference to the bundle is required

  private IPrimaryTypeTypeHierarchy m_hierarchy;
  private ITypeHierarchyChangedListener m_hierarchyChangedListener;
  private IResourceListener m_resourceListener;

  public WebServiceConsumerTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("Services"));
    setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsIcons.WebserviceConsumerFolder));

    m_bundle = getScoutResource();

    m_hierarchyChangedListener = new P_TypeHierarchyChangedListener();
    m_hierarchy = TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(JaxWsRuntimeClasses.AbstractWebServiceClient));
    m_hierarchy.addHierarchyListener(m_hierarchyChangedListener);

    // listener on build-jaxws.xml is necessary to reflect created consumers. That is because type listener is not notified about created types (bug).
    m_resourceListener = new P_BuildJaxWsResourceListener();
    getBuildJaxWsResource().addResourceListener(IResourceListener.EVENT_BUILDJAXWS_ENTRY_ADDED, m_resourceListener);
    getBuildJaxWsResource().addResourceListener(IResourceListener.ELEMENT_FILE, m_resourceListener);
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.WEBSERVICE_CONSUMER_TABLE_PAGE;
  }

  @Override
  public void unloadPage() {
    if (m_hierarchy != null && m_hierarchyChangedListener != null) {
      m_hierarchy.removeHierarchyListener(m_hierarchyChangedListener);
    }
    getBuildJaxWsResource().removeResourceListener(m_resourceListener);
    super.unloadPage();
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache) {
      m_hierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof ConsumerNewWizardAction) {
      ((ConsumerNewWizardAction) menu).init(getScoutResource());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ConsumerNewWizardAction.class};
  }

  @Override
  protected void loadChildrenImpl() {
    IType[] wsConsumerTypes = m_hierarchy.getAllSubtypes(TypeUtility.getType(JaxWsRuntimeClasses.AbstractWebServiceClient), TypeFilters.getClassesInProject(getScoutResource().getJavaProject()), TypeComparators.getTypeNameComparator());
    for (IType consumerType : wsConsumerTypes) {
      new WebServiceConsumerNodePage(this, consumerType);
    }
  }

  public XmlResource getBuildJaxWsResource() {
    return ResourceFactory.getBuildJaxWsResource(m_bundle);
  }

  private class P_TypeHierarchyChangedListener implements ITypeHierarchyChangedListener {

    @Override
    public void handleEvent(int eventType, IType type) {
      switch (eventType) {
        // important: ignore CHANGE events to exclude marker updates
        case POST_TYPE_REMOVING:
        case POST_TYPE_ADDING:
          IScoutBundle bundle = ScoutSdkCore.getScoutWorkspace().getScoutBundle(type.getJavaProject().getProject());
          if (bundle.getScoutProject() == getScoutResource().getScoutProject()) {
            markStructureDirty();
          }
          break;
      }
    }
  }

  private class P_BuildJaxWsResourceListener implements IResourceListener {

    @Override
    public void changed(String element, int event) {
      markStructureDirty();
    }
  }
}
