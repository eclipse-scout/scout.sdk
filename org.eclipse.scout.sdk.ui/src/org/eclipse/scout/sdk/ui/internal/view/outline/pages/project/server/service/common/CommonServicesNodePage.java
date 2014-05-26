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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.accesscontrol.AccessControlServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.bookmark.BookmarkStorageServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.calendar.CalendarServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.smtp.SmtpServiceTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.common.sql.SqlServiceTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

public class CommonServicesNodePage extends AbstractPage {

  public CommonServicesNodePage(IPage parent) {
    setParent(parent);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Services));
    setName(Texts.get("ServerServicesCommonNodePage"));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.COMMON_SERVICES_NODE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  protected void loadChildrenImpl() {
    try {
      new SqlServiceTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not create 'SqlServiceTablePage'.", e);
    }
    try {
      new BookmarkStorageServiceTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not create 'BookmarkStorageServiceTablePage'.", e);
    }
    try {
      new CalendarServiceTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not create 'CalendarServiceTablePage'.", e);
    }
    try {
      new SmtpServiceTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not create 'SmtpServiceTablePage'.", e);
    }
    try {
      new AccessControlServiceTablePage(this);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("could not create 'AccessControlServiceTablePage'.", e);
    }
  }
}
