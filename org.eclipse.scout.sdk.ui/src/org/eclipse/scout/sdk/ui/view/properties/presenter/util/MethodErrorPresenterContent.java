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
package org.eclipse.scout.sdk.ui.view.properties.presenter.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IRegion;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.fields.tooltip.CustomTooltip;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.ScoutSourceUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

public class MethodErrorPresenterContent extends Composite {

  private final FormToolkit m_toolkit;
  private Hyperlink m_labelLink;
  private Label m_label;
  private Label m_statusLabel;
  private Label m_statusIcon;
  private ImageHyperlink m_deleteButton;
  private ConfigurationMethod m_configurationMethod;
  private CustomTooltip m_customTooltip;
  private Composite m_linkComposite;

  public MethodErrorPresenterContent(Composite parent, FormToolkit toolkit) {
    super(parent, SWT.NONE);
    m_toolkit = toolkit;
    createContent(this);
  }

  private void createContent(Composite parent) {
    m_linkComposite = getToolkit().createComposite(parent);
    GridData linkCompData = new GridData();
    linkCompData.widthHint = 180;
    m_linkComposite.setLayoutData(linkCompData);
    m_linkComposite.setLayout(new GridLayout(1, true));

    createLabelArea(m_linkComposite);
    createBodyArea(parent);

    // layout
    GridLayout bodyLayout = new GridLayout(3, false);
    bodyLayout.horizontalSpacing = 0;
    bodyLayout.marginHeight = 0;
    bodyLayout.marginWidth = 0;
    bodyLayout.verticalSpacing = 0;
    parent.setLayout(bodyLayout);
    parent.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
  }

  private void createBodyArea(Composite rootArea) {
    Composite area = getToolkit().createComposite(rootArea);
    m_statusIcon = getToolkit().createLabel(area, "");
    m_statusLabel = getToolkit().createLabel(area, "");

    m_deleteButton = getToolkit().createImageHyperlink(rootArea, SWT.PUSH);
    m_deleteButton.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolRemove));
    m_deleteButton.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        new OperationJob(new ScoutMethodDeleteOperation(getMethod().peekMethod())).schedule();
      }
    });

    GridLayout glayout = new GridLayout(3, false);
    glayout.marginHeight = 0;
    glayout.marginWidth = 0;
    glayout.verticalSpacing = 0;
    glayout.horizontalSpacing = 3;
    area.setLayout(glayout);
    area.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

    m_statusIcon.setLayoutData(new GridData(GridData.BEGINNING));
    m_statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

    m_deleteButton.setLayoutData(new GridData(SWT.DEFAULT, SdkProperties.TOOL_BUTTON_SIZE));
    m_deleteButton.setEnabled(false);
  }

  private void createLabelArea(Composite parent) {
    if (m_labelLink != null) {
      m_labelLink.dispose();
    }
    if (m_label != null) {
      m_label.dispose();
    }

    GridData gd = new GridData(SWT.RIGHT, SWT.TOP, true, false);
    if (getMethod() != null && getMethod().isImplemented()) {
      m_labelLink = getToolkit().createHyperlink(parent, "", SWT.NONE);
      m_labelLink.addHyperlinkListener(new HyperlinkAdapter() {
        @Override
        public void linkActivated(HyperlinkEvent e) {
          showMethodInEditor(getMethod().peekMethod());
        }
      });
      m_labelLink.setFont(getFont(JFaceResources.DIALOG_FONT, true));
      m_labelLink.setLayoutData(gd);
      m_customTooltip = new CustomTooltip(m_labelLink, true);
    }
    else {
      m_label = getToolkit().createLabel(parent, "");
      m_label.setForeground(new Color(parent.getDisplay(), 0, 0, 128));
      m_label.setLayoutData(gd);
      m_customTooltip = new CustomTooltip(m_label, true);
    }

  }

  public ConfigurationMethod getMethod() {
    return m_configurationMethod;
  }

  public void setMethod(ConfigurationMethod method) {
    m_configurationMethod = method;
    try {
      this.setRedraw(false);
      createLabelArea(m_linkComposite);
      if (m_configurationMethod != null && m_configurationMethod.isImplemented()) {
        m_labelLink.setText(SdkProperties.getMethodPresenterName(method.peekMethod()));
      }
      else {
        m_label.setText(SdkProperties.getMethodPresenterName(getMethod().peekMethod()));
      }
      try {
        m_customTooltip.setText(wellFormMethod());
      }
      catch (Exception e1) {
        ScoutSdkUi.logWarning("could not create tooltip for '" + method.getMethodName() + "'", e1);
      }
      m_deleteButton.setEnabled(method.isImplemented());
      m_deleteButton.setToolTipText(Texts.get("RemoveXinY", getMethod().getMethodName(), getMethod().getType().getElementName()));
    }
    finally {
      this.setRedraw(true);
      this.layout(true, true);
    }
  }

  public void setStatus(IStatus status) {
    Image image = null;
    String message = "";
    if (status != null) {
      switch (status.getSeverity()) {
        case IStatus.ERROR:
          image = ScoutSdkUi.getImage(ScoutSdkUi.StatusError);
          break;
        case IStatus.WARNING:
          image = ScoutSdkUi.getImage(ScoutSdkUi.StatusWarning);
          break;
        case IStatus.INFO:
          image = ScoutSdkUi.getImage(ScoutSdkUi.StatusInfo);
          break;
      }
      message = status.getMessage();
    }
    m_statusIcon.setImage(image);
    m_statusLabel.setText(message);
  }

  protected FormToolkit getToolkit() {
    return m_toolkit;
  }

  protected void showMethodInEditor(IMethod method) {
    try {
      IEditorPart editor = JavaUI.openInEditor(method);
      JavaUI.revealInEditor(editor, (IJavaElement) method);
      if (editor instanceof ITextEditor) {
        ITextEditor textEditor = (ITextEditor) editor;
        IRegion reg = textEditor.getHighlightRange();
        if (reg != null) {
          textEditor.setHighlightRange(reg.getOffset(), reg.getLength(), true);
        }
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning(e);
    }
  }

  private String wellFormMethod() throws JavaModelException {
    String methodBody = getMethod().peekMethod().getSource();
    if (methodBody == null) {
      return null;
    }
    String newBody = ScoutSourceUtility.removeLeadingCommentAndAnnotationLines(methodBody);
    String newLine = ResourceUtility.getLineSeparator(getMethod().getType().getOpenable());
    newBody = ScoutSourceUtility.removeLineLeadingTab(ScoutUtility.getIndent(getMethod().getType()).length() + 1, newBody, newLine);
    newBody = newBody.replaceAll("\t", SdkProperties.TAB);
    return newBody;
  }

  protected Font getFont(String symbolicName, boolean bold) {
    if (bold) {
      return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().getBold(symbolicName);
    }
    else {
      return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(symbolicName);
    }
  }
}
