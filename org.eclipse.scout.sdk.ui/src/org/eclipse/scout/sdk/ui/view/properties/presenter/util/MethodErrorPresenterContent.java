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
import org.eclipse.core.runtime.Status;
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
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.SWT;
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
  private Label m_statusLabel;
  private Label m_statusIcon;
  private ImageHyperlink m_deleteButton;
  private ConfigurationMethod m_configurationMethod;
  private String m_presenterText;
  private CustomTooltip m_customTooltip;

  public MethodErrorPresenterContent(Composite parent, FormToolkit toolkit) {
    super(parent, SWT.NONE);
    m_toolkit = toolkit;
    setPresenterText(Texts.get("CustomImplementation"));
    createContent(this);
  }

  private void createContent(Composite parent) {
    createLabelArea(parent);
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
    Composite linkComposite = getToolkit().createComposite(parent);
    m_labelLink = getToolkit().createHyperlink(linkComposite, "", SWT.NONE);
    m_customTooltip = new CustomTooltip(m_labelLink, true);

    m_labelLink.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        showMethodInEditor(m_configurationMethod.peekMethod());
      }
    });
    m_labelLink.setEnabled(false);

    // layout
    GridData linkCompData = new GridData();
    linkCompData.widthHint = 180;
    linkComposite.setLayoutData(linkCompData);
    linkComposite.setLayout(new GridLayout(1, true));

    m_labelLink.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
  }

  public ConfigurationMethod getMethod() {
    return m_configurationMethod;
  }

  public void setMethod(ConfigurationMethod method) {
    m_configurationMethod = method;
    m_labelLink.setText(SdkProperties.getMethodPresenterName(method.peekMethod()));
    m_labelLink.setEnabled(true);
    m_labelLink.setFont(getFont(JFaceResources.DIALOG_FONT, getMethod().isImplemented()));
    try {
      m_customTooltip.setText(wellFormMethod());
    }
    catch (JavaModelException e1) {
      ScoutSdkUi.logWarning("could not create tooltip for '" + method.getMethodName() + "'", e1);
    }
    m_deleteButton.setEnabled(method.isImplemented());
    setStatus(new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, getPresenterText()));
    m_deleteButton.setToolTipText(Texts.get("RemoveXinY", getMethod().getMethodName(), getMethod().getType().getElementName()));
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
    String newBody = ScoutSourceUtility.removeLeadingCommentAndAnnotationLines(methodBody);
    newBody = ScoutSourceUtility.removeLineLeadingTab(ScoutUtility.getIndent(getMethod().getType()).length() + 1, newBody);
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

  public void setPresenterText(String presenterText) {
    m_presenterText = presenterText;
  }

  public String getPresenterText() {
    return m_presenterText;
  }
}
