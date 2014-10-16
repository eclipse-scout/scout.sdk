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
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

public class CalendarNodePage extends AbstractScoutTypePage {

  public CalendarNodePage(IPage parentPage, IType caledarType) {
    setParent(parentPage);
    setType(caledarType);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Calendar));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.CALENDAR_NODE_PAGE;
  }

  @Override
  protected void loadChildrenImpl() {
    new CalendarItemProviderTablePage(this, getType());
  }

  @Override
  public boolean isFolder() {
    return false;
  }
}
