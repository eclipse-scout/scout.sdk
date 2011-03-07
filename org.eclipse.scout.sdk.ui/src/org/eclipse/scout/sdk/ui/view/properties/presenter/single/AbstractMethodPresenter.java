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

import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.tooltip.JavadocTooltip;
import org.eclipse.scout.sdk.ui.jdt.JdtUiUtility;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.ui.view.properties.presenter.util.MethodErrorPresenterContent;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.ScoutSourceUtilities;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * <h3>AbstractMethodPresenter</h3> ...
 */
public abstract class AbstractMethodPresenter extends AbstractPresenter {

  private ConfigurationMethod m_configurationMethod;
  private Hyperlink m_labelLink;
  private Composite m_body;
  private MethodErrorPresenterContent m_errorContent;
  private JavadocTooltip m_tooltip;
  private ImageHyperlink m_deleteButton;

  public AbstractMethodPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
    create(getContainer());
  }

  protected void init(ConfigurationMethod method) throws CoreException {
    m_labelLink.setText(ScoutIdeProperties.getMethodPresenterName(method.peekMethod()));
    m_labelLink.setEnabled(true);
    m_labelLink.setFont(getFont(JFaceResources.DIALOG_FONT, method.isImplemented()));
    m_labelLink.getParent().layout();
    m_tooltip.setMember(method.peekMethod());

  }

  protected void create(Composite parent) {
    m_errorContent = new MethodErrorPresenterContent(parent, getToolkit());
    Control body = createBody(parent);

    // layout
    GridLayout glayout = new GridLayout(1, true);
    glayout.horizontalSpacing = 0;
    glayout.marginHeight = 0;
    glayout.marginWidth = 0;
    glayout.verticalSpacing = 0;
    parent.setLayout(glayout);
    body.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
    m_errorContent.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
    ((GridData) m_errorContent.getLayoutData()).exclude = true;

  }

  private Control createBody(Composite parent) {
    m_body = getToolkit().createComposite(parent);
    Composite linkComposite = getToolkit().createComposite(m_body);
    m_labelLink = getToolkit().createHyperlink(linkComposite, "", SWT.NONE);

    IHyperlinkListener actionListener = new HyperlinkAdapter() {
      @Override
      public void linkActivated(HyperlinkEvent e) {
        handleLabelLinkSelected();
      }
    };
    m_labelLink.addHyperlinkListener(actionListener);
    m_tooltip = new JavadocTooltip(m_labelLink);
    Control content = createContent(m_body);
    Composite buttonArea = getToolkit().createComposite(m_body);
//    buttonArea.setSize(0, 0);
    Control placeHolderControl = new Canvas(buttonArea, SWT.NONE);
    fillButtonArea(buttonArea);
//    placeHolderControl.setSize(new Point(1, 1));

    //layout
    GridLayout bodyLayout = new GridLayout(3, false);
    bodyLayout.horizontalSpacing = 0;
    bodyLayout.marginHeight = 0;
    bodyLayout.marginWidth = 0;
    bodyLayout.verticalSpacing = 0;
    m_body.setLayout(bodyLayout);

    GridData linkCompData = new GridData();
    linkCompData.widthHint = 130;
    linkComposite.setLayoutData(linkCompData);

    GridData contentData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    content.setLayoutData(contentData);

    GridData buttonAreaData = new GridData(GridData.FILL_VERTICAL);
//    buttonAreaData.heightHint = 1;
//    buttonAreaData.widthHint = 1;
    buttonArea.setLayoutData(buttonAreaData);

    RowLayout linkCompLayout = new RowLayout(SWT.HORIZONTAL);
    linkCompLayout.marginBottom = 0;
    linkCompLayout.marginLeft = 0;
    linkCompLayout.marginRight = 0;
    linkCompLayout.marginTop = 0;
    linkCompLayout.spacing = 0;
    linkComposite.setLayout(linkCompLayout);

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
        m_body.setVisible(true);
        m_errorContent.setVisible(false);
        ((GridData) m_errorContent.getLayoutData()).exclude = true;
        ((GridData) m_body.getLayoutData()).exclude = false;

      }
      catch (CoreException e) {
        // ScoutSdkUi.logWarning("could not parse method.", e);
        m_errorContent.setMethod(configurationMethod);
        m_body.setVisible(false);
        m_errorContent.setVisible(true);

        ((GridData) m_errorContent.getLayoutData()).exclude = false;
        ((GridData) m_body.getLayoutData()).exclude = true;
      }
      ((GridData) m_deleteButton.getLayoutData()).exclude = !configurationMethod.isImplemented();
      m_deleteButton.setVisible(configurationMethod.isImplemented());
      m_deleteButton.setToolTipText("Remove '" + getMethod().getMethodName() + "' in '" + getMethod().getType().getElementName() + "'...");

    }
    finally {
      getContainer().setRedraw(true);
      getContainer().layout(true, true);
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
      m_errorContent.setEnabled(enabled);
      m_deleteButton.setEnabled(enabled);
    }
  }

  @Override
  public boolean isEnabled() {
    if (!isDisposed()) {
      return m_errorContent.isEnabled();
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
    JdtUiUtility.showJavaElementInEditor(e, createNew);
  }

  protected final String readInitalValue() throws CoreException {
    try {
      Matcher m = Pattern.compile(Regex.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE, Pattern.DOTALL).matcher(getMethod().peekMethod().getSource());
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

  private String wellFormMethod() throws JavaModelException, MalformedTreeException, BadLocationException {
    String methodBody = getMethod().peekMethod().getSource();
    if (methodBody == null) {
      ScoutSdkUi.logWarning("methodBody of " + getMethod().getMethodName() + " in " + getMethod().getType().getFullyQualifiedName() + " is null");
    }
    String newBody = methodBody;
    newBody = ScoutSourceUtilities.removeLineLeadingTab(ScoutUtility.getIndent(getMethod().peekMethod().getDeclaringType()).length() + 1, newBody);
    newBody = newBody.replaceAll("\t", ScoutIdeProperties.TAB);
    return newBody;
  }

  protected String getJavaDoc() {
    try {
      Reader contentReader = JavadocContentAccess.getContentReader(getMethod().peekMethod(), true);
      if (contentReader != null) {
        return IOUtility.getContent(contentReader);
      }
    }
    catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();

    }
    return null;
  }

}
