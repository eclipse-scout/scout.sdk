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
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.util.wellform.WellformScoutTypeOperation;
import org.eclipse.scout.sdk.ui.action.FormDataUpdateAction;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.delete.FormDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.MainBoxNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyTablePage;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>FormNodePage</h3> ...
 */
public class FormNodePage extends AbstractScoutTypePage {

  protected final IType iGroupBox = TypeUtility.getType(RuntimeClasses.IGroupBox);
  private InnerTypePageDirtyListener m_mainBoxListener;

  public FormNodePage(AbstractPage parent, IType type) {
    super(SdkProperties.SUFFIX_FORM);
    setParent(parent);
    setType(type);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Form));
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
      TypeCacheAccessor.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getType(), m_mainBoxListener);
    }
    super.unloadPage();
  }

  @Override
  public void loadChildrenImpl() {
    if (m_mainBoxListener == null) {
      m_mainBoxListener = new InnerTypePageDirtyListener(this, iGroupBox);
      TypeCacheAccessor.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getType(), m_mainBoxListener);
    }
    new BeanPropertyTablePage(this, getType());
    // find all main boxes

    for (IType mainBoxType : ScoutTypeUtility.getInnerTypesOrdered(getType(), iGroupBox)) {
      MainBoxNodePage mainBoxNodePage = new MainBoxNodePage();
      mainBoxNodePage.setParent(this);
      mainBoxNodePage.setType(mainBoxType);
    }

    new FormHandlerTablePage(this, getType());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{TypeRenameAction.class, ShowJavaReferencesAction.class, FormDeleteAction.class, WellformAction.class, FormDataUpdateAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof FormDeleteAction) {
      ((FormDeleteAction) menu).setFormType(getType());
    }
    else if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      action.setScoutBundle(getScoutResource());
      action.setOperation(new WellformScoutTypeOperation(getType(), true));
    }
    else if (menu instanceof FormDataUpdateAction) {
      ((FormDataUpdateAction) menu).setType(getType());
    }
  }

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public boolean isFolder() {
    return false;
  }

}
