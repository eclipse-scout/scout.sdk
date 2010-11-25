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
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.tree.TreeNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;

public class TreeBoxNodePage extends AbstractFormFieldNodePage {

  public TreeBoxNodePage() {
    
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.TREE_BOX_NODE_PAGE;
  }

  @Override
  public void loadChildrenImpl() {
    new KeyStrokeTablePage(this, getType());
    IType[] trees = SdkTypeUtility.getTrees(getType());
    if (trees.length > 0) {
      new TreeNodePage(this, trees[0]);
    }
  }

  @Override
  public Action createEditAction() {
    // XXX
    return null;
    // return new EditAction(new EntityEditOrder(new TreeBoxEntity(getType())));
  }
}
