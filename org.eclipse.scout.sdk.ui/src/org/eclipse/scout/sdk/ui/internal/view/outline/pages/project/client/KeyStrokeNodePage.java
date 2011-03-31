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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client;

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.operation.IDeleteOperation;
import org.eclipse.scout.sdk.operation.util.TypeDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class KeyStrokeNodePage extends AbstractScoutTypePage {

  public KeyStrokeNodePage() {
    super();
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Keystroke));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.KEY_STROKE_NODE_PAGE;
  }

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredText";
  }

  @Override
  public Action createRenameAction() {
    return new TypeRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType(), ScoutIdeProperties.SUFFIX_KEY_STROKE);
  }

  @Override
  public Action createDeleteAction() {
    IDeleteOperation op = new TypeDeleteOperation(getType());
    DeleteAction deleteAction = new DeleteAction(getName(), getOutlineView().getSite().getShell(), op);
    deleteAction.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.KeystrokeRemove));
    return deleteAction;
  }

}
