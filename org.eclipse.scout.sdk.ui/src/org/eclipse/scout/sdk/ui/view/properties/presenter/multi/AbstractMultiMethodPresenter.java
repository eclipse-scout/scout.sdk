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
package org.eclipse.scout.sdk.ui.view.properties.presenter.multi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ui.fields.tooltip.CustomTooltip;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.ui.view.properties.presenter.util.MethodBean;
import org.eclipse.scout.sdk.ui.view.properties.presenter.util.MethodErrorPresenterContent;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethodSet;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * <h3>{@link AbstractMultiMethodPresenter}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 31.08.2010
 * @param <T>
 */
public abstract class AbstractMultiMethodPresenter<T> extends AbstractPresenter {

  private Hyperlink m_labelLink;
  private Composite m_body;
  private MethodErrorPresenterContent m_errorContent;
  private final Map<String, MethodBean<T>> m_methodSources = new HashMap<String, MethodBean<T>>();

  public AbstractMultiMethodPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
    create(getContainer());
  }

  protected static <T> boolean allEqual(Collection<MethodBean<T>> ar) {
    if (ar.size() > 1) {
      Iterator<MethodBean<T>> it = ar.iterator();
      T first = it.next().getCurrentSourceValue();
      while (it.hasNext()) {
        T cur = it.next().getCurrentSourceValue();
        if (CompareUtility.notEquals(first, cur)) {
          return false;
        }
      }
    }
    return true;
  }

  public final void setMethodSet(ConfigurationMethodSet methodSet) {
    try {
      for (ConfigurationMethod method : methodSet.getMethods()) {
        String key = method.getType().getFullyQualifiedName() + SignatureUtility.getMethodIdentifier(method.peekMethod());

        MethodBean<T> methodBean = m_methodSources.get(key);
        if (methodBean == null) {
          methodBean = new MethodBean<T>(method, key);
          m_methodSources.put(key, methodBean);
        }
        else {
          methodBean.setMethod(method);
        }
      }
      try {
        init(methodSet);
      }
      catch (CoreException e) {
        ScoutSdkUi.logWarning("parse error in multi presenter. ", e);
      }
    }
    catch (CoreException e) {
      ScoutSdkUi.logWarning("could not compare method body with cached body of presenter '" + methodSet.getMethodName() + "'", e);
    }
    getContainer().layout(true);
  }

  protected void init(ConfigurationMethodSet methodSet) throws CoreException {
    m_labelLink.setText(SdkProperties.getMethodPresenterName(getFirstMethod()));
    for (MethodBean<T> bean : m_methodSources.values()) {
      T sourceValue = parseSourceInput(PropertyMethodSourceUtility.getMethodReturnValue(bean.getMethod().peekMethod()), bean.getMethod());
      bean.setCurrentSourceValue(sourceValue);
      T defaultValue = parseSourceInput(bean.getMethod().computeDefaultValue(), bean.getMethod());
      bean.setDefaultValue(defaultValue);
    }
  }

  protected abstract String formatSourceValue(T value) throws CoreException;

  protected abstract String formatDisplayValue(T value) throws CoreException;

  protected abstract T parseSourceInput(String value, ConfigurationMethod method) throws CoreException;

  protected abstract T parseDisplayInput(String value) throws CoreException;

  protected void create(Composite parent) {
    m_errorContent = new MethodErrorPresenterContent(parent, getToolkit());
    m_body = getToolkit().createComposite(parent);
    m_labelLink = getToolkit().createHyperlink(m_body, "", SWT.NONE);
    new CustomTooltip(m_labelLink, false);
    Control content = createContent(m_body);

    // layout
    GridLayout glayout = new GridLayout(1, true);
    glayout.horizontalSpacing = 0;
    glayout.marginHeight = 0;
    glayout.marginWidth = 0;
    glayout.verticalSpacing = 0;
    parent.setLayout(glayout);
    m_body.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
    m_errorContent.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
    ((GridData) m_errorContent.getLayoutData()).exclude = true;

    m_body.setLayout(new FormLayout());

    FormData data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(0, 0);
    data.bottom = new FormAttachment(100, 0);
    data.right = new FormAttachment(0, 150);
    m_labelLink.setLayoutData(data);
    if (content != null) {
      data = new FormData();
      data.top = new FormAttachment(0, 0);
      data.left = new FormAttachment(m_labelLink, 5);
      data.right = new FormAttachment(100, 0);
      data.bottom = new FormAttachment(100, 0);
      content.setLayoutData(data);
    }
  }

  protected abstract Control createContent(Composite container);

  protected IMethod getFirstMethod() {
    if (m_methodSources == null || m_methodSources.size() < 1) {
      return null;
    }
    return m_methodSources.values().iterator().next().getMethod().peekMethod();
  }

  protected Collection<MethodBean<T>> getMethodBeans() {
    return m_methodSources.values();
  }
}
