/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.AnnotationUpdateOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.SourceRangeRemoveOperation;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class AnnotationPresenter extends AbstractPropertyPresenter<IAnnotation> {

  private IType m_type;
  private IType m_annotationType;
  protected Button m_checkbox;
  protected Label m_label;
  private SelectionListener m_selectionListener;

  public AnnotationPresenter(Composite parent, FormToolkit toolkit, IType type, IType annotationType) {
    super(parent, toolkit, false);
    m_selectionListener = new P_SelectionListener();
    m_type = type;
    m_annotationType = annotationType;
    setLinkAlwaysEnabled(true);
    callInitializer();
  }

  @Override
  protected Control createContent(Composite parent) {
    m_checkbox = new Button(parent, SWT.CHECK);
    m_label = new Label(parent, SWT.NONE);

    // layout
    GridLayout layout = new GridLayout(2, false);
    layout.marginLeft = 0;
    layout.marginRight = 0;
    layout.marginTop = 0;
    layout.marginBottom = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    parent.setLayout(layout);
    m_checkbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

    GridData gd = new GridData();
    gd.horizontalAlignment = SWT.RIGHT;
    m_label.setLayoutData(gd);
    m_label.setFont(PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().getItalic(JFaceResources.DIALOG_FONT));
    m_label.setText(Texts.get("inherited"));
    return m_checkbox;
  }

  @Override
  protected void setInputInternal(IAnnotation input) {
    m_checkbox.removeSelectionListener(m_selectionListener);
    try {
      m_checkbox.setSelection(input != null);
    }
    finally {
      m_checkbox.addSelectionListener(m_selectionListener);
    }

    updateStyle(input);
  }

  private void updateStyle(IAnnotation annotation) {
    m_label.setVisible(false);
    if (annotation != null) {
      setUseLinkAsLabel(true);
      if (JaxWsSdkUtility.isAnnotationOnDeclaringType(m_type, annotation)) {
        // annotation on declaring type.
        setBoldLabelText(true);
        setEnabled(true);
      }
      else {
        setTooltip(Texts.get("XisInheritedFromY", annotation.getElementName(), annotation.getParent().getElementName()));
        m_label.setVisible(true);
        setBoldLabelText(false);
        setEnabled(false); // cannot be changed as annotation is configured in super type
      }
    }
    else {
      setUseLinkAsLabel(false);
      setBoldLabelText(false);
      setEnabled(true);
    }
  }

  private final class P_SelectionListener extends SelectionAdapter {

    @Override
    public void widgetSelected(SelectionEvent e) {
      if (m_checkbox.getSelection()) {
        P_AnnotationCreateOperation op = new P_AnnotationCreateOperation();
        new OperationJob(op).schedule();
      }
      else {
        IAnnotation annotation = JaxWsSdkUtility.getAnnotation(m_type, JaxWsRuntimeClasses.ScoutTransaction.getFullyQualifiedName(), false);
        if (annotation != null) {
          SourceRangeRemoveOperation op = new SourceRangeRemoveOperation();
          op.setAnnotation(annotation);
          op.setDeclaringType(m_type);
          new OperationJob(op).schedule();
          setValueFromUI(null);
        }
      }
    }
  }

  /**
   * No supported. Use {@link AnnotationPresenter#updatePresenter()} instead.
   */
  @Override
  public void setInput(IAnnotation value) {
    throw new UnsupportedOperationException();
  }

  public void updatePresenter() {
    IAnnotation annotation = JaxWsSdkUtility.getAnnotation(m_type, m_annotationType.getFullyQualifiedName(), true);
    super.setInput(annotation);
  }

  @Override
  protected void setValueFromUI(IAnnotation newValue) {
    updateStyle(newValue);
    super.setValueFromUI(newValue);
  }

  @Override
  protected void execLinkAction() throws CoreException {
    if (getValue() == null) {
      return;
    }
    try {
      JavaUI.openInEditor(getValue());
    }
    catch (Exception e) {
      JaxWsSdk.logWarning(e);
    }
  }

  public IType getType() {
    return m_type;
  }

  public void setType(IType type) {
    m_type = type;
  }

  private class P_AnnotationCreateOperation implements IOperation {

    @Override
    public void validate() throws IllegalArgumentException {
    }

    @Override
    public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
      AnnotationUpdateOperation op = new AnnotationUpdateOperation();
      op.setAnnotationType(m_annotationType);
      op.setDeclaringType(m_type);
      op.run(monitor, workingCopyManager);

      final IAnnotation annotation = JaxWsSdkUtility.getAnnotation(m_type, m_annotationType.getFullyQualifiedName(), false);
      ScoutSdkUi.getDisplay().asyncExec(new Runnable() {

        @Override
        public void run() {
          setValueFromUI(annotation);
        }
      });
    }

    @Override
    public String getOperationName() {
      return P_AnnotationCreateOperation.class.getName();
    }
  }
}
