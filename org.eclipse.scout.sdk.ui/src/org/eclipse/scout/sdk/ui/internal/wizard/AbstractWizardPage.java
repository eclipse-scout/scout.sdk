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
package org.eclipse.scout.sdk.ui.internal.wizard;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.ui.wizard.IStatusProvider;
import org.eclipse.scout.sdk.ui.wizard.WizardPageFieldToolkit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>AbstractWizardPage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 06.03.2010
 */
public abstract class AbstractWizardPage extends WizardPage {
  private IStatus m_status = Status.OK_STATUS;
  private IStatus m_defaultOkStatus = Status.OK_STATUS;
  private Composite m_content;
  private final WizardPageFieldToolkit m_fieldToolkit;
  private int m_stateChangingCounter = 0;
  private boolean m_excludePage;
  private ArrayList<IStatusProvider> m_statusProvider;

  public AbstractWizardPage(String pageName, String title, ImageDescriptor titleImage) {
    super(pageName, title, titleImage);
    m_fieldToolkit = new WizardPageFieldToolkit();
    m_statusProvider = new ArrayList<IStatusProvider>();
  }

  public AbstractWizardPage(String pageName) {
    this(pageName, null, (ImageDescriptor) null);

  }

  public WizardPageFieldToolkit getFieldToolkit() {
    return m_fieldToolkit;
  }

  @Override
  public void setDescription(String newMessage) {
    m_defaultOkStatus = new Status(IStatus.OK, ScoutSdk.PLUGIN_ID, newMessage);
    setStatus(m_status);
  }

  /**
   * Override to handle each page activate.
   */
  public void postActivate() {
  }

  @Override
  public final void createControl(Composite parent) {
    m_content = new Composite(parent, SWT.NONE);
    m_content.setLayout(new FillLayout());
    createContent(m_content);
    setControl(m_content);
    revalidate();
  }

  @Override
  protected boolean isControlCreated() {
    return super.isControlCreated() && !getControl().isDisposed();
  }

  /**
   * Overwrite this method to add your controls to the wizard page.
   * 
   * @param parent
   */
  protected abstract void createContent(Composite parent);

  @Override
  public Composite getControl() {
    return m_content;
  }

  @Override
  public void setVisible(boolean visible) {
    m_content.setVisible(visible);
  }

  public boolean addStatusProvider(IStatusProvider provider) {
    return m_statusProvider.add(provider);
  }

  public boolean removeStatusProvider(IStatusProvider provider) {
    return m_statusProvider.remove(provider);
  }

  protected void setStatus(IStatus status) {
    if (status == null) {
      setPageComplete(true);
    }
    else {
      if (status.isOK() && Status.OK_STATUS.equals(status)) {
        m_status = m_defaultOkStatus;
      }
      else {
        m_status = status;
      }
      if (!m_status.matches(IStatus.ERROR)) {
        setPageComplete(true);
      }
      else {
        setPageComplete(false);
      }
    }
    if (isCurrentPage()) {
      displayStatus(getStatus(), m_defaultOkStatus);

    }
  }

  protected void displayStatus(IStatus status, IStatus defaultStatus) {
    if (status == defaultStatus) {
      setMessage(getDescription(), IStatus.OK);
    }
    else {
      IStatus highestSeverityStatus = getHighestSeverityStatus(status, defaultStatus);
      int messagetype;
      switch (highestSeverityStatus.getSeverity()) {
        case IStatus.INFO:
          messagetype = IMessageProvider.INFORMATION;
          break;
        case IStatus.WARNING:
          messagetype = IMessageProvider.WARNING;
          break;
        case IStatus.ERROR:
          messagetype = IMessageProvider.ERROR;
          break;
        default:
          messagetype = IMessageProvider.NONE;
          break;
      }
      String message = highestSeverityStatus.getMessage();
      setMessage(message, messagetype);
    }
  }

  private IStatus getHighestSeverityStatus(IStatus status, IStatus highestSeverity) {
    if (status.isMultiStatus()) {
      for (IStatus child : status.getChildren()) {
        highestSeverity = getHighestSeverityStatus(child, highestSeverity);
      }
      return highestSeverity;
    }
    else {
      if (highestSeverity.getSeverity() < status.getSeverity()) {
        highestSeverity = status;
      }
      return highestSeverity;
    }
  }

  /**
   * to force a revalidate if needed.
   */
  public void pingStateChanging() {
    if (m_stateChangingCounter <= 0) {
      m_stateChangingCounter = 0;
      revalidate();
    }
  }

  /**
   * NOTE: always call this method in a try finally block.
   * 
   * @param changing
   */
  protected void setStateChanging(boolean changing) {
    if (changing) {
      m_stateChangingCounter++;
    }
    else {
      m_stateChangingCounter--;
    }
    if (m_stateChangingCounter <= 0) {
      m_stateChangingCounter = 0;
      revalidate();
    }
  }

  /**
   * call to revalidate the wizard page. this method calls the overwritable method
   * {@link AbstractScoutWizardPage#validatePage(MultiStatus)}.
   * 
   * @see {@link AbstractScoutWizardPage#validatePage(MultiStatus)}
   */
  protected final void revalidate() {
    MultiStatus multiStatus = new MultiStatus(ScoutSdk.PLUGIN_ID, -1, "multi status", null);
    validatePage(multiStatus);
    for (IStatusProvider p : m_statusProvider) {
      p.validate(this, multiStatus);
    }
    setStatus(multiStatus);
  }

  /**
   * Classes extending BCWizardPage can overwrite this method to do some page validation and
   * add additional status to the given multi status.
   * 
   * @param multiStatus
   */
  protected void validatePage(MultiStatus multiStatus) {
  }

  public boolean performFinish() {
    return true;
  }

  protected IStatus getStatus() {
    return m_status;
  }

  public IStatus getDefaultOkStatus() {
    return m_defaultOkStatus;
  }

  public void setExcludePage(boolean excludePage) {
    m_excludePage = excludePage;
  }

  public boolean isExcludePage() {
    return m_excludePage;
  }

}
