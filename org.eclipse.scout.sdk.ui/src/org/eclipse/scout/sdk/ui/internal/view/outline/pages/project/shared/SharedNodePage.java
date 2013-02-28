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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.util.wellform.WellformSharedBundleOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.ScoutBundleNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.library.LibrariesTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.AbstractBundleNodeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ScoutBundleNode;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;

/**
 * <h3>SharedNodePage</h3> ...
 */
public class SharedNodePage extends AbstractBundleNodeTablePage {

  private final IType abstractIcons = TypeUtility.getType(RuntimeClasses.AbstractIcons);

  public SharedNodePage(IPage parent, ScoutBundleNode node) {
    super(parent, node);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SHARED_NODE_PAGE;
  }

  @Override
  public void loadChildrenImpl() {
    super.loadChildrenImpl();
    if (getScoutResource().getIconProvider() != null) {
      ICachedTypeHierarchy iconHierarchy = TypeUtility.getPrimaryTypeHierarchy(abstractIcons);
      IType[] iconTypes = iconHierarchy.getAllSubtypes(abstractIcons, TypeFilters.getClassesInProject(getScoutResource().getJavaProject()), null);
      if (iconTypes.length > 0) {
        new IconNodePage(this, iconTypes[0]);
      }
    }

    new PermissionTablePage(this);
    new CodeTypeTablePage(this);
    try {
      new LookupCallTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("could not create LookupCallTablePage in project '" + getScoutResource().getSymbolicName() + "'", e);
    }
    try {
      new LibrariesTablePage(this, getScoutResource());
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("Error occured while loading '" + LibrariesTablePage.class.getSimpleName() + "' node in bundle '" + getScoutResource().getSymbolicName() + "'.", e);
    }
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      action.setLabel(Texts.get("WellformSharedBundle"));
      action.setOperation(new WellformSharedBundleOperation(getScoutResource()));
    }
    else if (menu instanceof ScoutBundleNewAction) {
      ((ScoutBundleNewAction) menu).setScoutProject(getScoutResource());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{WellformAction.class, ScoutBundleNewAction.class};
  }
}
