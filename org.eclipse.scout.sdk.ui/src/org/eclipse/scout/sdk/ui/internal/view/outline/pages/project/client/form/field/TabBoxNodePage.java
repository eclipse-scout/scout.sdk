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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.action.rename.FormFieldRenameAction;
import org.eclipse.scout.sdk.ui.internal.extensions.FormFieldExtensionPoint;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.ITypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;
import org.eclipse.scout.sdk.ui.wizard.form.fields.groupbox.GroupBoxNewWizard;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

public class TabBoxNodePage extends AbstractFormFieldNodePage {
  IType igroupBox = ScoutSdk.getType(RuntimeClasses.IGroupBox);

//  IPrimaryTypeTypeHierarchy formfieldHierarchy = ScoutSdk.getTypeHierarchyPrimaryTypes(igroupBox);

  public TabBoxNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Tabbox));

  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.TAB_BOX_NODE_PAGE;
  }

  @Override
  protected void loadChildrenImpl() {
    new KeyStrokeTablePage(this, getType());
    ITypeHierarchy hierarchy = ScoutSdk.getLocalTypeHierarchy(getType());
    IType[] allGroupboxes = TypeUtility.getInnerTypes(getType(), TypeFilters.getSubtypeFilter(igroupBox, hierarchy), TypeComparators.getOrderAnnotationComparator());
    for (IType groupBox : allGroupboxes) {
      ITypePage nodePage = (ITypePage) FormFieldExtensionPoint.createNodePage(groupBox, hierarchy);
      if (nodePage != null) {
        nodePage.setParent(this);
        nodePage.setType(groupBox);
        nodePage.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TabboxTab));
      }
    }
  }

  @Override
  public Action createRenameAction() {
    return new FormFieldRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType(), ScoutIdeProperties.SUFFIX_BOX);
  }

  @Override
  public Action createNewAction() {
    GroupBoxNewWizard wizard = new GroupBoxNewWizard();
    wizard.initWizard(getType());
    return new WizardAction(Texts.get("Process_newTypeX", "GroupBox"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TabboxTabAdd),
        wizard);
  }

  @Override
  public Action createDeleteAction() {
    Action deleteAction = super.createDeleteAction();
    if (deleteAction != null) {
      deleteAction.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TabboxRemove));
    }
    return deleteAction;
  }
}
