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

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;

public class BooleanFieldNodePage extends AbstractFormFieldNodePage {

  public BooleanFieldNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.BooleanField));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.BOOLEAN_FIELD_NODE_PAGE;
  }

  @Override
  public Action createDeleteAction() {
    Action deleteAction = super.createDeleteAction();
    deleteAction.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.BooleanFieldRemove));
    return deleteAction;
  }
}
