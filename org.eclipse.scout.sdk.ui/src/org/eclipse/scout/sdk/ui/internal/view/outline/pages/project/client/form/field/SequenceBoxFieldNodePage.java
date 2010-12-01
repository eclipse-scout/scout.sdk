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
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.rename.FormFieldRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

/** <h3>SequenceBoxFieldNodePage</h3> */
public class SequenceBoxFieldNodePage extends AbstractBoxNodePage {

  public SequenceBoxFieldNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Sequencebox));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.SEQUENCE_BOX_FIELD_NODE_PAGE;
  }

  @Override
  protected void loadChildrenImpl() {
    super.loadChildrenImpl();
    IPage[] childArray = getChildArray();
    if (childArray.length == 3) {
      // expect from to fields
      childArray[1].setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormFieldFrom));
      childArray[2].setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormFieldTo));
    }
  }

  @Override
  public Action createRenameAction() {
    return new FormFieldRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType(), ScoutIdeProperties.SUFFIX_BOX);
  }

  @Override
  public Action createDeleteAction() {
    Action deleteAction = super.createDeleteAction();
    if (deleteAction != null) {
      deleteAction.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SequenceboxRemove));
    }
    return deleteAction;
  }
}
