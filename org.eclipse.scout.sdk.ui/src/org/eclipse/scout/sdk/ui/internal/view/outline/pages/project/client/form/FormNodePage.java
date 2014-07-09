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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.util.wellform.WellformScoutTypeOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.delete.FormDeleteAction;
import org.eclipse.scout.sdk.ui.action.dto.FormDataUpdateAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.MainBoxNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyTablePage;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyProvider;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>FormNodePage</h3> ...
 */
public class FormNodePage extends AbstractScoutTypePage implements ITypeHierarchyProvider {

  private InnerTypePageDirtyListener m_mainBoxListener;
  private IJavaResourceChangedListener m_hierarchyClearListener;
  private ITypeHierarchy m_localHierarchy;

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
      ScoutSdkCore.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getType(), m_mainBoxListener);
    }
    if (m_hierarchyClearListener != null) {
      ScoutSdkCore.getJavaResourceChangedEmitter().removeJavaResourceChangedListener(m_hierarchyClearListener);
    }
    super.unloadPage();
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache) {
      clearTypeHierarchy();
    }
    super.refresh(clearCache);
  }

  @Override
  protected void loadChildrenImpl() {
    IType iGroupBox = TypeUtility.getType(IRuntimeClasses.IGroupBox);

    if (m_mainBoxListener == null) {
      m_mainBoxListener = new InnerTypePageDirtyListener(this, iGroupBox);
      ScoutSdkCore.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getType(), m_mainBoxListener);
    }
    if (m_hierarchyClearListener == null) {
      m_hierarchyClearListener = new IJavaResourceChangedListener() {
        @Override
        public void handleEvent(JdtEvent event) {
          if (event.getElementType() == IJavaElement.TYPE) {
            if (getType().getCompilationUnit().equals(event.getElement().getAncestor(IJavaElement.COMPILATION_UNIT))) {
              clearTypeHierarchy();
            }
          }
        }
      };
      ScoutSdkCore.getJavaResourceChangedEmitter().addJavaResourceChangedListener(m_hierarchyClearListener);
    }
    new BeanPropertyTablePage(this, getType());

    for (IType mainBoxType : ScoutTypeUtility.getInnerTypesOrdered(getType(), iGroupBox, getTypeHierarchy())) {
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
      action.init(getScoutBundle(), getType());
      action.setOperation(new WellformScoutTypeOperation(getType(), true));
    }
    else if (menu instanceof FormDataUpdateAction) {
      ((FormDataUpdateAction) menu).setFormDataOwner(getType());
    }
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  protected synchronized void clearTypeHierarchy() {
    m_localHierarchy = null;
  }

  @Override
  public synchronized ITypeHierarchy getTypeHierarchy() {
    if (m_localHierarchy == null) {
      m_localHierarchy = TypeUtility.getLocalTypeHierarchy(getType());
    }
    return m_localHierarchy;
  }
}
