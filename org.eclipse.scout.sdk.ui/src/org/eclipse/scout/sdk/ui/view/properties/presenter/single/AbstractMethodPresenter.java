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
package org.eclipse.scout.sdk.ui.view.properties.presenter.single;

import java.util.regex.Matcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.fields.tooltip.JavadocTooltip;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.ui.view.properties.presenter.util.MethodErrorPresenterContent;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * <h3>AbstractMethodPresenter</h3> ...
 */
public abstract class AbstractMethodPresenter extends AbstractPresenter {

  private ConfigurationMethod m_configurationMethod;
  private Hyperlink m_labelLink;
  private Label m_label;
  private Composite m_body;
  private Composite m_linkComposite;
  private MethodErrorPresenterContent m_errorContent;
  private JavadocTooltip m_tooltip;
  private ImageHyperlink m_deleteButton;

  public AbstractMethodPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
    create(getContainer());
  }

  protected boolean isLinkMode() {
    return m_configurationMethod != null && m_configurationMethod.isImplemented();
  }

  protected void init(ConfigurationMethod method) throws CoreException {
    if (m_labelLink != null) m_labelLink.dispose();
    if (m_label != null) m_label.dispose();

    GridData gd = new GridData(SWT.RIGHT, SWT.TOP, true, false);
    if (isLinkMode()) {
      m_labelLink = getToolkit().createHyperlink(m_linkComposite, SdkProperties.getMethodPresenterName(method.peekMethod()), SWT.NONE);
      m_labelLink.addHyperlinkListener(new HyperlinkAdapter() {
        @Override
        public void linkActivated(HyperlinkEvent e) {
          handleLabelLinkSelected();
        }
      });
      m_labelLink.setEnabled(true);
      m_labelLink.setFont(getFont(JFaceResources.DIALOG_FONT, true));
      m_labelLink.setLayoutData(gd);
      m_labelLink.getParent().layout();
      m_tooltip = new JavadocTooltip(m_labelLink);
    }
    else {
      m_label = getToolkit().createLabel(m_linkComposite, SdkProperties.getMethodPresenterName(method.peekMethod()));
      m_label.setForeground(new Color(m_linkComposite.getDisplay(), 0, 0, 128));
      m_label.setLayoutData(gd);
      m_tooltip = new JavadocTooltip(m_label);
    }
    m_tooltip.setMember(method.peekMethod());
  }

  protected void create(Composite parent) {
    // layout parent
    GridLayout glayout = new GridLayout(1, true);
    glayout.horizontalSpacing = 0;
    glayout.marginHeight = 0;
    glayout.marginWidth = 0;
    glayout.verticalSpacing = 0;
    parent.setLayout(glayout);

    createBody(parent);
  }

  private Control createBody(Composite parent) {
    m_body = getToolkit().createComposite(parent);
    m_linkComposite = getToolkit().createComposite(m_body);

    Control content = createContent(m_body);
    Composite buttonArea = getToolkit().createComposite(m_body);
    Control placeHolderControl = new Canvas(buttonArea, SWT.NONE);
    fillButtonArea(buttonArea);

    //layout
    GridLayout bodyLayout = new GridLayout(3, false);
    bodyLayout.horizontalSpacing = 0;
    bodyLayout.marginHeight = 0;
    bodyLayout.marginWidth = 0;
    bodyLayout.verticalSpacing = 0;
    m_body.setLayout(bodyLayout);

    GridData bodyLayoutData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    m_body.setLayoutData(bodyLayoutData);

    GridData linkCompData = new GridData();
    linkCompData.widthHint = 180;
    m_linkComposite.setLayoutData(linkCompData);

    GridData contentData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    if (content != null) {
      content.setLayoutData(contentData);
    }

    GridData buttonAreaData = new GridData(GridData.FILL_VERTICAL);
    buttonArea.setLayoutData(buttonAreaData);

    m_linkComposite.setLayout(new GridLayout(1, true));

    GridLayout buttonBarLayout = new GridLayout(100, false);
    buttonBarLayout.horizontalSpacing = 0;
    buttonBarLayout.marginHeight = 0;
    buttonBarLayout.marginWidth = 0;
    buttonBarLayout.verticalSpacing = 0;
    buttonArea.setLayout(buttonBarLayout);
    placeHolderControl.setLayoutData(new GridData(1, 1));
    return m_body;
  }

  protected void fillButtonArea(Composite buttonArea) {
    m_deleteButton = getToolkit().createImageHyperlink(buttonArea, SWT.PUSH);
    m_deleteButton.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolRemove));
    m_deleteButton.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        if (getMethod().isImplemented()) {
          ScoutMethodDeleteOperation op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
          OperationJob job = new OperationJob(op);
          job.schedule();
        }
      }
    });
    GridData deleteButtonData = new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.GRAB_VERTICAL);
    m_deleteButton.setLayoutData(deleteButtonData);
  }

  protected abstract Control createContent(Composite container);

  public final void setMethod(ConfigurationMethod configurationMethod) {
    try {
      getContainer().setRedraw(false);
      m_configurationMethod = configurationMethod;
      try {
        init(configurationMethod);
        setErrorPresenterVisible(false);
      }
      catch (Exception e) {
        if (m_errorContent == null) {
          m_errorContent = new MethodErrorPresenterContent(getContainer(), getToolkit());
          m_errorContent.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        }
        if (e instanceof CoreException) {
          m_errorContent.setStatus(((CoreException) e).getStatus());
        }
        else {
          ScoutSdkUi.logError(e);
          if (e.getMessage() != null) {
            m_errorContent.setStatus(new ScoutStatus(Status.ERROR, e.getMessage(), e));
          }
          else {
            m_errorContent.setStatus(new ScoutStatus(Status.ERROR, e.getClass().getSimpleName(), e));
          }
        }
        m_errorContent.setMethod(configurationMethod);
        setErrorPresenterVisible(true);
      }
      ((GridData) m_deleteButton.getLayoutData()).exclude = !configurationMethod.isImplemented();
      m_deleteButton.setVisible(configurationMethod.isImplemented());
      m_deleteButton.setToolTipText(Texts.get("RemoveXinY", getMethod().getMethodName(), getMethod().getType().getElementName()));
    }
    finally {
      getContainer().setRedraw(true);
      getContainer().layout(true, true);
    }
  }

  private void setErrorPresenterVisible(boolean visible) {
    m_body.setVisible(!visible);
    ((GridData) m_body.getLayoutData()).exclude = visible;

    if (m_errorContent != null) {
      m_errorContent.setVisible(visible);
      ((GridData) m_errorContent.getLayoutData()).exclude = !visible;
    }
  }

  public ConfigurationMethod getMethod() {
    return m_configurationMethod;
  }

  public void setLabelText(String text) {
    if (!isDisposed()) {
      m_labelLink.setText(text);
    }
  }

  public String getLabelText() {
    String labelText = null;
    if (!isDisposed()) {
      labelText = m_labelLink.getText();
    }
    return labelText;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!isDisposed()) {
      if (m_errorContent != null) {
        m_errorContent.setEnabled(enabled);
      }
      m_deleteButton.setEnabled(enabled);
      if (m_labelLink != null) {
        m_labelLink.setEnabled(true); // always allowed to click the label
      }
    }
  }

  @Override
  public boolean isEnabled() {
    if (!isDisposed()) {
      return m_deleteButton.isEnabled();
    }
    return false;
  }

  protected void handleLabelLinkSelected() {
    showJavaElementInEditor(getMethod().peekMethod());
  }

  protected void showJavaElementInEditor(IJavaElement e) {
    showJavaElementInEditor(e, true);
  }

  protected void showJavaElementInEditor(IJavaElement e, boolean createNew) {
    UiUtility.showJavaElementInEditor(e, createNew);
  }

  protected final String readInitalValue() throws CoreException {
    try {
      Matcher m = Regex.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE.matcher(getMethod().peekMethod().getSource());
      if (m.find()) {
        return m.group(1);
      }
      else {
        throw new CoreException(new ScoutStatus(getMethod().peekMethod().getSource()));
      }
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError(e);
      return null;
    }
  }
}
