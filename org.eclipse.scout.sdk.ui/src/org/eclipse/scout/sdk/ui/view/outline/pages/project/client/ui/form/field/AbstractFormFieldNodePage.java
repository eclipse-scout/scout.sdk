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
package org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.CreateTemplateAction;
import org.eclipse.scout.sdk.ui.action.OperationAction;
import org.eclipse.scout.sdk.ui.action.delete.FormFieldDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.FormFieldRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;

public abstract class AbstractFormFieldNodePage extends AbstractScoutTypePage {

  public AbstractFormFieldNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormField));

  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredLabel";
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new Separator());
    manager.add(new CreateTemplateAction(getOutlineView().getSite().getShell(), this, getType()));
    if (getType().getDeclaringType() == null) {
      manager.add(new OperationAction("Update Form Data...", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolLoading), new org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation(getType())));
    }
  }

  @Override
  public Action createRenameAction() {
    return new FormFieldRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType(), ScoutIdeProperties.SUFFIX_FORM_FIELD);
  }

  @Override
  public Action createDeleteAction() {
    FormFieldDeleteAction action = new FormFieldDeleteAction(getType(), getName(), getOutlineView().getSite().getShell());

    return action;
  }

//  @Override
//  public void fillContextMenu(IMenuManager manager) {
//    super.fillContextMenu(manager);
//    manager.add(new Action("ast on field") {
//      @Override
//      public void run() {
//        ((ScoutType) getType()).visitMethodsNew();
//      }
//    });
//  }

  // @Override
  // public void fillContextMenu(IMenuManager manager){
  // super.fillContextMenu(manager);
  // manager.add(new Separator());
  // manager.add(createMoveAction(IMoveTypes.UP));
  // manager.add(createMoveAction(IMoveTypes.DOWN));
  // manager.add(new Separator());
  // manager.add(createMoveAction(IMoveTypes.TOP));
  // manager.add(createMoveAction(IMoveTypes.BOTTOM));
  // // XXX rename action
  // }

}
