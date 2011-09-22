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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ParameterField extends Composite {

  private Label m_label;
  private Text m_parameterName;
  private Label m_labelType;
  private JavaCodeField m_parameterType;
  private ParameterArgument m_parameter = new ParameterArgument();

  private EventListenerList m_eventListeners = new EventListenerList();
  private final IJavaSearchScope m_scope;

  public ParameterField(Composite parent, ParameterArgument a, IJavaSearchScope scope) {
    super(parent, SWT.NONE);
    m_scope = scope;
    createContent(this, a);
  }

  protected void createContent(Composite parent, ParameterArgument a) {
    m_label = new Label(parent, SWT.NONE);
    m_parameterName = new Text(parent, SWT.BORDER);
    if (a != null) {
      m_parameterName.setText(a.getName());
      m_parameter.setName(a.getName());
    }
    m_parameterName.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_parameter.setName(m_parameterName.getText());
        notifyParameterChanged();
      }
    });
    m_labelType = new Label(parent, SWT.NONE);
    m_parameterType = new JavaCodeField(parent, m_scope);
    if (a != null) {
      m_parameterType.setText(a.getType());
      m_parameter.setType(a.getType());
    }
    m_parameterType.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        m_parameter.setType(m_parameterType.getText());
        m_parameter.setFullyQuallifiedImports(m_parameterType.getAllImports());
        notifyParameterChanged();
      }
    });
    // layout
    parent.setLayout(new FormLayout());
    FormData data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(20, 0);
    data.bottom = new FormAttachment(100, 0);
    m_label.setLayoutData(data);

    data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(m_label, 5);
    data.right = new FormAttachment(55, 0);
    data.bottom = new FormAttachment(100, 0);
    m_parameterName.setLayoutData(data);

    data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(m_parameterName, 5);
    data.right = new FormAttachment(65, 0);
    data.bottom = new FormAttachment(100, 0);
    m_labelType.setLayoutData(data);

    data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(m_labelType, 5);
    data.right = new FormAttachment(100, 0);
    data.bottom = new FormAttachment(100, 0);
    m_parameterType.setLayoutData(data);
  }

  public void setLabelParameterName(String label) {
    m_label.setText(label);
  }

  public void setLabelParameterType(String label) {
    m_labelType.setText(label);
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

  public String getLabel() {
    return m_label.getText();
  }

}
