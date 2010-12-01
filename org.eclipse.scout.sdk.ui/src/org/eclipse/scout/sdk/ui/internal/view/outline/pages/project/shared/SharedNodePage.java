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

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.util.wellform.WellformSharedBundleOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

/**
 * <h3>SharedNodePage</h3> ...
 */
public class SharedNodePage extends AbstractPage {

  final IType abstractIcons = ScoutSdk.getType(RuntimeClasses.AbstractIcons);
  private final IScoutBundle m_sharedProject;

  public SharedNodePage(IPage parent, IScoutBundle sharedProject) {
    setParent(parent);
    m_sharedProject = sharedProject;
    setName(getScoutResource().getSimpleName());
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SharedBundle));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SHARED_NODE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public IScoutBundle getScoutResource() {
    return m_sharedProject;
  }

  @Override
  public boolean isInitiallyLoaded() {
    return true;
  }

  @Override
  public void loadChildrenImpl() {

    if (getScoutResource().getScoutProject().getIconProvider() != null) {
      ICachedTypeHierarchy iconHierarchy = ScoutSdk.getPrimaryTypeHierarchy(abstractIcons);
      IType[] iconTypes = iconHierarchy.getAllSubtypes(abstractIcons, TypeFilters.getClassesInProject(getScoutResource().getJavaProject()), null);
      if (iconTypes.length > 0) {
        new IconNodePage(this, iconTypes[0]);
      }
    }
    if (getScoutResource().getScoutProject().getNlsProject() != null) {
      new NlsTextsNodePage(this);
    }
    new PermissionTablePage(this);
    new CodeTypeTablePage(this);
    try {
      new LookupCallTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("could not create LookupCallTablePage in project '" + getScoutResource().getRootPackageName() + "'", e);
    }

  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new Separator());
    manager.add(new WellformAction(getOutlineView().getSite().getShell(), "Wellform shared bundle...", new WellformSharedBundleOperation(getScoutResource())));
  }

}
