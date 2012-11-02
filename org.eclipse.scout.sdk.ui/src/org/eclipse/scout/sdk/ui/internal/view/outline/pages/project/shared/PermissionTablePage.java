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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.PermissionNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class PermissionTablePage extends AbstractPage {

  final IType basicPermission = TypeUtility.getType(RuntimeClasses.BasicPermission);
  private ICachedTypeHierarchy m_basicPermissionHierarchy;

  public PermissionTablePage(AbstractPage parent) {
    setParent(parent);
    setName(Texts.get("PermissionTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Permissions));
  }

  @Override
  public void unloadPage() {
    if (m_basicPermissionHierarchy != null) {
      m_basicPermissionHierarchy.removeHierarchyListener(getPageDirtyListener());
      m_basicPermissionHierarchy = null;
    }
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_basicPermissionHierarchy != null) {
      m_basicPermissionHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.PERMISSION_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  /**
   * shared bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void loadChildrenImpl() {
    if (m_basicPermissionHierarchy == null) {
      m_basicPermissionHierarchy = TypeUtility.getPrimaryTypeHierarchy(basicPermission);
      m_basicPermissionHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    ITypeFilter filter = TypeFilters.getClassesInProject(getScoutResource().getJavaProject());
    IType[] permissions = m_basicPermissionHierarchy.getAllSubtypes(basicPermission, filter, TypeComparators.getTypeNameComparator());
    for (IType type : permissions) {
      new PermissionNodePage(this, type);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{PermissionNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    ((PermissionNewAction) menu).setScoutBundle(getScoutResource());
  }
}
