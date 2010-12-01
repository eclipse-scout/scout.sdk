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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.calendar.CalendarNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;

public class CalendarFieldNodePage extends AbstractFormFieldNodePage {

  public CalendarFieldNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.CalendarField));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CALENDAR_FIELD_NODE_PAGE;
  }

  @Override
  public void loadChildrenImpl() {
    new KeyStrokeTablePage(this, getType());
    IType[] calendars = SdkTypeUtility.getCalendar(getType());
    if (calendars.length > 0) {
      new CalendarNodePage(this, calendars[0]);
    }

  }

  @Override
  public Action createDeleteAction() {
    Action deleteAction = super.createDeleteAction();
    if (deleteAction != null) {
      deleteAction.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.CalendarFieldRemove));
    }
    return deleteAction;
  }

  @Override
  public Action createEditAction() {
    // XXX
    return null;
    // return new EditAction(new EntityEditOrder(new CalendarFieldEntity(getType())));
  }
}
