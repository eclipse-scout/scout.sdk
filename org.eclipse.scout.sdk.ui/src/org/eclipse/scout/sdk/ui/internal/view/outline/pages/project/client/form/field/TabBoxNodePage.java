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

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.create.CreateTemplateAction;
import org.eclipse.scout.sdk.ui.action.create.GroupBoxNewAction;
import org.eclipse.scout.sdk.ui.action.delete.FormFieldDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.FormFieldRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.FormFieldExtensionPoint;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.ITypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;

public class TabBoxNodePage extends AbstractFormFieldNodePage {

  private InnerTypePageDirtyListener m_innerTypeListener;

  public TabBoxNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Tabbox));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.TAB_BOX_NODE_PAGE;
  }

  @Override
  public void unloadPage() {
    if (m_innerTypeListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getType(), m_innerTypeListener);
      m_innerTypeListener = null;
    }
    super.unloadPage();
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_innerTypeListener == null) {
      IType iFormField = TypeUtility.getType(IRuntimeClasses.IFormField);
      m_innerTypeListener = new InnerTypePageDirtyListener(this, iFormField);
      ScoutSdkCore.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getType(), m_innerTypeListener);
    }
    new KeyStrokeTablePage(this, getType());
    ITypeHierarchy hierarchy = getLocalHierarchy();
    IType iGroupBox = TypeUtility.getType(IRuntimeClasses.IGroupBox);
    Set<IType> allGroupboxes = TypeUtility.getInnerTypes(getType(), TypeFilters.getSubtypeFilter(iGroupBox, hierarchy), ScoutTypeComparators.getOrderAnnotationComparator());
    for (IType groupBox : allGroupboxes) {
      ITypePage nodePage = (ITypePage) FormFieldExtensionPoint.createNodePage(groupBox, hierarchy);
      if (nodePage != null) {
        nodePage.setParent(this);
        nodePage.setType(groupBox);
        nodePage.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TabboxTab));
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ShowJavaReferencesAction.class, CreateTemplateAction.class, FormFieldRenameAction.class,
        FormFieldDeleteAction.class, GroupBoxNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof FormFieldRenameAction) {
      FormFieldRenameAction a = (FormFieldRenameAction) menu;
      a.setReadOnlySuffix(SdkProperties.SUFFIX_BOX);
    }
    else if (menu instanceof FormFieldDeleteAction) {
      menu.setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TabboxRemove));
    }
    else if (menu instanceof GroupBoxNewAction) {
      ((GroupBoxNewAction) menu).setType(getType());
    }
  }
}
