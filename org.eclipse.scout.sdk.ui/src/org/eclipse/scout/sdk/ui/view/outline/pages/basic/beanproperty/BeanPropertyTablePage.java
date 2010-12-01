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
package org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty;

import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.jdt.JdtEvent;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.wizard.beanproperty.BeanPropertyNewWizard;
import org.eclipse.scout.sdk.workspace.member.IPropertyBean;
import org.eclipse.scout.sdk.workspace.type.PropertyBeanComparators;
import org.eclipse.scout.sdk.workspace.type.PropertyBeanFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * <h3>BeanPropertyTablePage</h3> ...
 */
public class BeanPropertyTablePage extends AbstractPage {

  private final IType m_declaringType;
  private P_MethodChangedListener m_methodChangedListener;

  public BeanPropertyTablePage(IPage parentPage, IType beanDeclaringType) {
    m_declaringType = beanDeclaringType;
    setName(Texts.get("BeanPropertyTablePage"));
    setParent(parentPage);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Variables));
  }

  @Override
  public void unloadPage() {
    if (m_methodChangedListener != null) {
      ScoutSdk.removeMethodChangedListener(getDeclaringType(), m_methodChangedListener);
    }
    super.unloadPage();
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.BEAN_PROPERTY_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  /**
   * @return the declaringType
   */
  public IType getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public void loadChildrenImpl() {
    if (m_methodChangedListener == null) {
      m_methodChangedListener = new P_MethodChangedListener();
      ScoutSdk.addMethodChangedListener(getDeclaringType(), m_methodChangedListener);
    }
    IPropertyBean[] beans = TypeUtility.getPropertyBeans(getDeclaringType(), PropertyBeanFilters.getFormDataPropertyFilter(), PropertyBeanComparators.getNameComparator());
    for (IPropertyBean bean : beans) {
      new BeanPropertyNodePage(this, bean);
    }
  }

  @Override
  public Action createNewAction() {
    return new WizardAction(Texts.get("Action_newTypeX", "Property Bean"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.VariableAdd),
        new BeanPropertyNewWizard(m_declaringType));
  }

  private class P_MethodChangedListener implements IJavaResourceChangedListener {
    @Override
    public void handleEvent(JdtEvent event) {
      if (TypeUtility.hasAnnotation((IAnnotatable) event.getElement(), RuntimeClasses.FormData)) {
        markStructureDirty();
      }
    }
  } // end class P_MethodChangedListener
}
