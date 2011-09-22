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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.CalendarItemProviderNewAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;

/**
 * <h3>CalendarItemProducerTablePage</h3> ...
 */
public class CalendarItemProviderTablePage extends AbstractPage {

  final IType iCalendarItemProvider = ScoutSdk.getType(RuntimeClasses.ICalendarItemProvider);

  private final IType m_calendarType;

  public CalendarItemProviderTablePage(IPage parent, IType calendarType) {
    super.setParent(parent);
    m_calendarType = calendarType;
    setName(Texts.get("CalendarItemProducerTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.CalendarItemProviders));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CALENDAR_ITEM_PROVIDER_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void loadChildrenImpl() {
    IType[] innerTypes = SdkTypeUtility.getCalendarItemProviders(getCalendarType());
    for (IType provider : innerTypes) {
      CalendarItemProviderNodePage childPage = new CalendarItemProviderNodePage();
      childPage.setParent(this);
      childPage.setType(provider);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{CalendarItemProviderNewAction.class};
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    ((CalendarItemProviderNewAction) menu).setType(getCalendarType());
  }

  public IType getCalendarType() {
    return m_calendarType;
  }
}
