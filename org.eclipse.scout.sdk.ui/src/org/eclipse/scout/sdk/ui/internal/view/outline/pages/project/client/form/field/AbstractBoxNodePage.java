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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.CreateTemplateAction;
import org.eclipse.scout.sdk.ui.action.OperationAction;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.action.delete.BoxDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.FormFieldRenameAction;
import org.eclipse.scout.sdk.ui.internal.extensions.FormFieldExtensionPoint;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.ITypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.ui.wizard.form.fields.FormFieldNewWizard;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

public abstract class AbstractBoxNodePage extends AbstractScoutTypePage {
  protected IType iFormField = ScoutSdk.getType(RuntimeClasses.IFormField);

  private InnerTypePageDirtyListener m_innerTypeListener;

  public AbstractBoxNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Groupbox));
  }

  @Override
  public void unloadPage() {
    if (m_innerTypeListener != null) {
      ScoutSdk.removeInnerTypeChangedListener(getType(), m_innerTypeListener);
      m_innerTypeListener = null;
    }
    super.unloadPage();
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredLabel";
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_innerTypeListener == null) {
      m_innerTypeListener = new InnerTypePageDirtyListener(this, iFormField);
      ScoutSdk.addInnerTypeChangedListener(getType(), m_innerTypeListener);
    }
    new KeyStrokeTablePage(this, getType());
    ITypeHierarchy hierarchy = ScoutSdk.getLocalTypeHierarchy(getType());
    IType[] allSubtypes = TypeUtility.getInnerTypes(getType(), TypeFilters.getSubtypeFilter(iFormField, hierarchy), TypeComparators.getOrderAnnotationComparator());
    for (IType t : allSubtypes) {
      ITypePage nodePage = (ITypePage) FormFieldExtensionPoint.createNodePage(t, hierarchy);
      if (nodePage != null) {
        nodePage.setParent(this);
        nodePage.setType(t);
      }
    }
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    manager.add(new WizardAction(Texts.get("Action_newTypeX", "Form Field"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormFieldAdd),
        new FormFieldNewWizard(getType())));
    manager.add(new Separator());
    super.fillContextMenu(manager);
    manager.add(new Separator());
    manager.add(new CreateTemplateAction(getOutlineView().getSite().getShell(), this, getType()));
    if (getType().getDeclaringType() == null) {
      manager.add(new OperationAction("Update Form Data...", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolLoading), new org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation(getType())));
    }
  }

  @Override
  public Action createRenameAction() {
    return new FormFieldRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType(), ScoutIdeProperties.SUFFIX_BOX);
  }

  @Override
  public Action createDeleteAction() {
    return new BoxDeleteAction(getType(), getOutlineView().getSite().getShell());
  }

}
