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
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.delete.FormFieldDeleteAction;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.table.TableNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;

public class TableFieldNodePage extends AbstractFormFieldNodePage {

  public TableFieldNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TableField));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.TABLE_FIELD_NODE_PAGE;
  }

  @Override
  public void loadChildrenImpl() {
    new KeyStrokeTablePage(this, getType());
    IType[] tables = SdkTypeUtility.getTables(getType());
    if (tables.length > 0) {
      TableNodePage tableNodePage = new TableNodePage();
      tableNodePage.setParent(this);
      tableNodePage.setType(tables[0]);
    }
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof FormFieldDeleteAction) {
      menu.setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TableFieldRemove));
    }
  }
}
