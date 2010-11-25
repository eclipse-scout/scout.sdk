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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.wizard;

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ui.action.delete.WizardStepDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.WizardStepRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

/**
 * <h3>WizardStepNodePage</h3> ...
 */
public class WizardStepNodePage extends AbstractScoutTypePage {

  public WizardStepNodePage() {
    
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.WIZARD_STEP_NODE_PAGE;
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredTitle";
  }

  @Override
  public Action createRenameAction() {
    return new WizardStepRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType());
  }

  @Override
  public Action createDeleteAction() {
    return new WizardStepDeleteAction(getType(), getOutlineView().getSite().getShell());
  }

  // @Override
  // public Action createMoveAction(int moveOperation){
  // return new ProcessAction(Texts.get("Action_moveTypeX",IMoveTypes.TEXTS[moveOperation]),
  // JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_CFILE),
  // new BCTypeMoveProcess(getType(),SDE.getType(RuntimeClasses.IWizardStep),moveOperation));
  // // BCTypeMoveOperation o=new BCTypeMoveOperation(getType());
  // // o.setMoveOperation(moveOperation);
  // // o.setTypeFilter(new DescendantTypeFilter(IWizardStep.class));
  // // return new RunAction(o);
  // }

  // @Override
  // public void fillContextMenu(IMenuManager manager){
  // super.fillContextMenu(manager);
  // manager.add(new Separator());
  // manager.add(createMoveAction(IMoveTypes.UP));
  // manager.add(createMoveAction(IMoveTypes.DOWN));
  // manager.add(new Separator());
  // manager.add(createMoveAction(IMoveTypes.TOP));
  // manager.add(createMoveAction(IMoveTypes.BOTTOM));
  // }
}
