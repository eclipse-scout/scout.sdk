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

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.operation.util.wellform.WellformClientBundleOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.InstallClientSessionAction;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.ClassIdNewAction;
import org.eclipse.scout.sdk.ui.action.create.ScoutBundleNewAction;
import org.eclipse.scout.sdk.ui.action.dto.MultipleUpdateFormDataAction;
import org.eclipse.scout.sdk.ui.action.dto.TypeResolverPageDataAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.library.LibrariesTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.AbstractBundleNodeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ScoutBundleNode;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.FormTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.SearchFormTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.AllPagesTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.wizard.WizardTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.formdata.ClientBundleUpdateFormDataOperation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

public class ClientNodePage extends AbstractBundleNodeTablePage {

  private ICachedTypeHierarchy m_clientSessionHierarchy;
  private ICachedTypeHierarchy m_desktopHierarchy;
  private ICachedTypeHierarchy m_desktopExtensionHierarchy;

  public ClientNodePage(IPage parent, ScoutBundleNode node) {
    super(parent, node);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CLIENT_NODE_PAGE;
  }

  @Override
  public void unloadPage() {
    if (m_desktopHierarchy != null) {
      m_desktopHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_desktopHierarchy = null;
    }
    if (m_desktopExtensionHierarchy != null) {
      m_desktopExtensionHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_desktopExtensionHierarchy = null;
    }
    if (m_clientSessionHierarchy != null) {
      m_clientSessionHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_clientSessionHierarchy = null;
    }
  }

  @Override
  protected void loadChildrenImpl() {
    super.loadChildrenImpl();
    IType iDesktop = TypeUtility.getType(IRuntimeClasses.IDesktop);
    IType iDesktopExtension = TypeUtility.getType(IRuntimeClasses.IDesktopExtension);

    if (m_clientSessionHierarchy == null) {
      IType iClientSession = TypeUtility.getType(IRuntimeClasses.IClientSession);
      m_clientSessionHierarchy = TypeUtility.getPrimaryTypeHierarchy(iClientSession);
      m_clientSessionHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    if (m_desktopHierarchy == null) {
      m_desktopHierarchy = TypeUtility.getPrimaryTypeHierarchy(iDesktop);
      m_desktopHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    if (m_desktopExtensionHierarchy == null) {
      m_desktopExtensionHierarchy = TypeUtility.getPrimaryTypeHierarchy(iDesktopExtension);
      m_desktopExtensionHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    // client sessions
    Set<IType> clientSessions = ScoutTypeUtility.getClientSessionTypes(getScoutBundle());
    for (IType clientSession : clientSessions) {
      new ClientSessionNodePage(this, clientSession);
    }
    // desktop
    Set<IType> desktops = m_desktopHierarchy.getAllSubtypes(iDesktop, ScoutTypeFilters.getTypesInScoutBundles(getScoutBundle()));
    if (desktops.size() > 1) {
      ScoutSdkUi.logWarning("more than one desktop found.");
    }
    for (IType desktop : desktops) {
      new DesktopNodePage(this, desktop);
    }
    // desktop extension
    Set<IType> desktopExtensions = m_desktopExtensionHierarchy.getAllSubtypes(iDesktopExtension, ScoutTypeFilters.getTypesInScoutBundles(getScoutBundle()));
    for (IType desktopExtension : desktopExtensions) {
      new DesktopExtensionNodePage(this, desktopExtension);
    }
    //others
    new FormTablePage(this);
    new SearchFormTablePage(this);
    new WizardTablePage(this);
    try {
      new ClientLookupCallTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("could not load LocalLookupCallTablePage.", e);
    }
    new ClientServiceTablePage(this);
    new OutlineTablePage(this);
    new AllPagesTablePage(this);
    new TemplateTablePage(this);
    try {
      new LibrariesTablePage(this, getScoutBundle());
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error occured while loading '" + LibrariesTablePage.class.getSimpleName() + "' node in bundle '" + getScoutBundle().getSymbolicName() + "'.", e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{WellformAction.class, MultipleUpdateFormDataAction.class, InstallClientSessionAction.class,
        ScoutBundleNewAction.class, TypeResolverPageDataAction.class, ClassIdNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      action.setOperation(new WellformClientBundleOperation(CollectionUtility.hashSet(getScoutBundle())));
      action.init(getScoutBundle());
    }
    else if (menu instanceof MultipleUpdateFormDataAction) {
      ((MultipleUpdateFormDataAction) menu).setOperation(new ClientBundleUpdateFormDataOperation(getScoutBundle()));
    }
    else if (menu instanceof InstallClientSessionAction) {
      ((InstallClientSessionAction) menu).init(m_clientSessionHierarchy, getScoutBundle());
    }
    else if (menu instanceof ScoutBundleNewAction) {
      ((ScoutBundleNewAction) menu).setScoutProject(getScoutBundle());
    }
    else if (menu instanceof ClassIdNewAction) {
      ((ClassIdNewAction) menu).setScoutBundle(getScoutBundle());
    }
    else if (menu instanceof TypeResolverPageDataAction) {
      ((TypeResolverPageDataAction) menu).init(new ITypeResolver() {
        @Override
        public Set<IType> getTypes() {
          IType iPageWithTable = TypeUtility.getType(IRuntimeClasses.IPageWithTable);
          ICachedTypeHierarchy pageWithTableHierarchy = TypeUtility.getPrimaryTypeHierarchy(iPageWithTable);
          return pageWithTableHierarchy.getAllSubtypes(iPageWithTable, ScoutTypeFilters.getTypesInScoutBundles(getScoutBundle()));
        }
      }, getScoutBundle());
    }
  }
}
