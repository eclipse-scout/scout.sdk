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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.BeanPropertyNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.IPropertyBean;
import org.eclipse.scout.sdk.util.type.PropertyBeanComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;
import org.eclipse.scout.sdk.workspace.type.ScoutPropertyBeanFilters;

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
      TypeCacheAccessor.getJavaResourceChangedEmitter().removeMethodChangedListener(getDeclaringType(), m_methodChangedListener);
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
      TypeCacheAccessor.getJavaResourceChangedEmitter().addMethodChangedListener(getDeclaringType(), m_methodChangedListener);
    }
    IPropertyBean[] beans = TypeUtility.getPropertyBeans(getDeclaringType(), ScoutPropertyBeanFilters.getFormDataPropertyFilter(), PropertyBeanComparators.getNameComparator());
    for (IPropertyBean bean : beans) {
      new BeanPropertyNodePage(this, bean);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{BeanPropertyNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    ((BeanPropertyNewAction) menu).setType(m_declaringType);
  }

  private class P_MethodChangedListener implements IJavaResourceChangedListener {
    @Override
    public void handleEvent(JdtEvent event) {
      if (JdtUtility.hasAnnotation((IAnnotatable) event.getElement(), RuntimeClasses.FormData)) {
        markStructureDirty();
      }
    }
  } // end class P_MethodChangedListener
}
