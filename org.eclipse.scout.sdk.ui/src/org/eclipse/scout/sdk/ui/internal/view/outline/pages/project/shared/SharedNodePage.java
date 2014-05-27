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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.util.wellform.WellformSharedBundleOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.ClassIdNewAction;
import org.eclipse.scout.sdk.ui.action.create.ScoutBundleNewAction;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.library.LibrariesTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.AbstractBundleNodeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ScoutBundleNode;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>SharedNodePage</h3> ...
 */
public class SharedNodePage extends AbstractBundleNodeTablePage {

  public SharedNodePage(IPage parent, ScoutBundleNode node) {
    super(parent, node);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SHARED_NODE_PAGE;
  }

  @Override
  protected void loadChildrenImpl() {
    super.loadChildrenImpl();

    if (getScoutBundle().getIconProvider() != null) {
      IType abstractIcons = TypeUtility.getType(IRuntimeClasses.AbstractIcons);
      ICachedTypeHierarchy iconHierarchy = TypeUtility.getPrimaryTypeHierarchy(abstractIcons);
      Set<IType> iconTypes = iconHierarchy.getAllSubtypes(abstractIcons, ScoutTypeFilters.getClassesInScoutBundles(getScoutBundle()), null);
      if (iconTypes.size() > 0) {
        new IconNodePage(this, CollectionUtility.firstElement(iconTypes));
      }
    }

    new PermissionTablePage(this);
    new CodeTypeTablePage(this);
    new LookupCallTablePage(this);
    new LibrariesTablePage(this, getScoutBundle());
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      action.init(getScoutBundle());
      action.setLabel(Texts.get("WellformSharedBundle"));
      action.setOperation(new WellformSharedBundleOperation(getScoutBundle()));
    }
    else if (menu instanceof ScoutBundleNewAction) {
      ((ScoutBundleNewAction) menu).setScoutProject(getScoutBundle());
    }
    else if (menu instanceof ClassIdNewAction) {
      ((ClassIdNewAction) menu).setScoutBundle(getScoutBundle());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{WellformAction.class, ScoutBundleNewAction.class, ClassIdNewAction.class};
  }
}
