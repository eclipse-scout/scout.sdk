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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.calendar;

import java.util.Set;

import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.MenuTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.SdkProperties;

/**
 * <h3>CalendarItemProviderNodePage</h3>
 */
public class CalendarItemProviderNodePage extends AbstractScoutTypePage {

  public CalendarItemProviderNodePage() {
    super(SdkProperties.SUFFIX_CALENDAR_ITEM_PROVIDER);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.CalendarItemProvider));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CALENDAR_ITEM_PROVIDER_NODE_PAGE;
  }

  @Override
  protected void loadChildrenImpl() {
    new MenuTablePage(this, getType());
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(TypeRenameAction.class, ShowJavaReferencesAction.class, DeleteAction.class);
  }
}
