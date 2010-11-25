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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.form.FormDataUpdateOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformScoutTypeOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.OperationAction;
import org.eclipse.scout.sdk.ui.action.delete.FormDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.MainBoxNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyTablePage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * <h3>FormNodePage</h3> ...
 */
public class FormNodePage extends AbstractScoutTypePage {

  protected final IType iGroupBox = ScoutSdk.getType(RuntimeClasses.IGroupBox);
  private InnerTypePageDirtyListener m_mainBoxListener;

  public FormNodePage(AbstractPage parent, IType type) {

    setParent(parent);
    setType(type);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_FORM));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.FORM_NODE_PAGE;
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredTitle";
  }

  @Override
  public void unloadPage() {
    if (m_mainBoxListener != null) {
      ScoutSdk.removeInnerTypeChangedListener(getType(), m_mainBoxListener);
    }
    super.unloadPage();
  }

  @Override
  public void loadChildrenImpl() {
    if (m_mainBoxListener == null) {
      m_mainBoxListener = new InnerTypePageDirtyListener(this, iGroupBox);
      ScoutSdk.addInnerTypeChangedListener(getType(), m_mainBoxListener);
    }
    new BeanPropertyTablePage(this, getType());
    // find all main boxes

    for (IType mainBoxType : TypeUtility.getInnerTypesOrdered(getType(), iGroupBox)) {
      MainBoxNodePage mainBoxNodePage = new MainBoxNodePage();
      mainBoxNodePage.setParent(this);
      mainBoxNodePage.setType(mainBoxType);
    }

    new FormHandlerTablePage(this, getType());
  }

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new Separator());
    manager.add(new OperationAction("Wellform Form", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_LOADING), new WellformScoutTypeOperation(getType(), true)));
    manager.add(new OperationAction("Update Form Data", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_LOADING), new FormDataUpdateOperation(getType())));
  }

  @Override
  public Action createRenameAction() {
    return new TypeRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType(), ScoutIdeProperties.SUFFIX_FORM);
  }

  @Override
  public Action createDeleteAction() {
    return new FormDeleteAction(getType(), getOutlineView().getSite().getShell());
  }

  @Override
  public boolean isFolder() {
    return false;
  }

}
