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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.CalendarItemProviderNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypeOrderChangedPageDirtyListener;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>CalendarItemProducerTablePage</h3> ...
 */
public class CalendarItemProviderTablePage extends AbstractPage {
  private InnerTypePageDirtyListener m_innerTypeListener;
  private InnerTypeOrderChangedPageDirtyListener m_orderChangedListener;
  private final IType m_calendarType;

  public CalendarItemProviderTablePage(IPage parent, IType calendarType) {
    super.setParent(parent);
    m_calendarType = calendarType;
    setName(Texts.get("CalendarItemProducerTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.CalendarItemProviders));
  }

  @Override
  public void unloadPage() {
    if (m_innerTypeListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getCalendarType(), m_innerTypeListener);
      m_innerTypeListener = null;
    }
    if (m_orderChangedListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeJavaResourceChangedListener(m_orderChangedListener);
      m_orderChangedListener = null;
    }
    super.unloadPage();
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CALENDAR_ITEM_PROVIDER_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  protected void loadChildrenImpl() {
    IType iCalendarItemProvider = TypeUtility.getType(IRuntimeClasses.ICalendarItemProvider);

    if (m_innerTypeListener == null) {
      m_innerTypeListener = new InnerTypePageDirtyListener(this, iCalendarItemProvider);
      ScoutSdkCore.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getCalendarType(), m_innerTypeListener);
    }
    if (m_orderChangedListener == null) {
      m_orderChangedListener = new InnerTypeOrderChangedPageDirtyListener(this, iCalendarItemProvider, getCalendarType());
      ScoutSdkCore.getJavaResourceChangedEmitter().addJavaResourceChangedListener(m_orderChangedListener);
    }

    Set<IType> innerTypes = ScoutTypeUtility.getCalendarItemProviders(getCalendarType());
    for (IType provider : innerTypes) {
      CalendarItemProviderNodePage childPage = new CalendarItemProviderNodePage();
      childPage.setParent(this);
      childPage.setType(provider);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{CalendarItemProviderNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    ((CalendarItemProviderNewAction) menu).setType(getCalendarType());
  }

  public IType getCalendarType() {
    return m_calendarType;
  }
}
