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
package org.eclipse.scout.sdk.ui.internal.fields.code;

import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.sdk.operation.service.ParameterArgument;
import org.eclipse.scout.sdk.ui.fields.javacode.JavaCodeField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ReturnParameterField extends Composite {

  private Label m_labelType;
  private JavaCodeField m_parameterType;
  private ParameterArgument m_parameter = new ParameterArgument();

  private EventListenerList m_eventListeners = new EventListenerList();
  private final int m_labelProcentage;
  private final IJavaSearchScope m_scope;

  public ReturnParameterField(Composite parent, IJavaSearchScope scope) {
    this(parent, 40, scope);
  }

  public ReturnParameterField(Composite parent, int labelProcentage, IJavaSearchScope scope) {
    this(parent, labelProcentage, null, scope);
  }

  public ReturnParameterField(Composite parent, int labelProcentage, ParameterArgument a, IJavaSearchScope scope) {
    super(parent, SWT.NONE);
    m_labelProcentage = labelProcentage;
    m_scope = scope;
    createContent(this, a);
  }

  protected void createContent(Composite parent, ParameterArgument a) {
    m_labelType = new Label(parent, SWT.NONE);
    m_labelType.setAlignment(SWT.RIGHT);
    m_parameterType = new JavaCodeField(parent, m_scope);
    if (a != null) {
      m_parameterType.setText(a.getType());
      m_parameter.setType(a.getType());
    }
    m_parameterType.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        m_parameter.setType(m_parameterType.getText());
        m_parameter.setFullyQualifiedImports(m_parameterType.getAllImports());
        notifyParameterChanged();
      }
    });
    // layout
    parent.setLayout(new FormLayout());
    FormData data = new FormData();
    data.top = new FormAttachment(0, 4);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(m_labelProcentage, 0);
    data.bottom = new FormAttachment(100, 0);
    m_labelType.setLayoutData(data);
    data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(m_labelType, 5);
    data.right = new FormAttachment(100, 0);
    data.bottom = new FormAttachment(100, 0);
    m_parameterType.setLayoutData(data);
  }

  public void setLabel(String label) {
    m_labelType.setText(label);
  }

  @Override
  public void addFocusListener(FocusListener listener) {
    m_parameterType.addFocusListener(listener);
  }

  public String getParameterType() {
    return m_parameterType.getText();
  }

  private void notifyParameterChanged() {
    for (IParameterFieldListener listener : m_eventListeners.getListeners(IParameterFieldListener.class)) {
      listener.parameterChanged(m_parameter);
    }
  }

  public void addParameterFieldListener(IParameterFieldListener listener) {
    m_eventListeners.add(IParameterFieldListener.class, listener);
  }

  public void removeParameterFieldListener(IParameterFieldListener listener) {
    m_eventListeners.remove(IParameterFieldListener.class, listener);
  }

  public ParameterArgument getParameter() {
    return m_parameter;
  }
}
