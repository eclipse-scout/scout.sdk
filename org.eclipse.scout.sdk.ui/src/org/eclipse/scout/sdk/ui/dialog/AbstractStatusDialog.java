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
package org.eclipse.scout.sdk.ui.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.scout.sdk.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractStatusDialog extends TitleAreaDialog {

  private IStatus m_status = Status.OK_STATUS;
  private IStatus m_defaultOkStatus = new Status(IStatus.OK, ScoutSdkUi.PLUGIN_ID, "");
  private final FieldToolkit m_fieldToolkit;
  private int m_stateChangingCounter = 0;
  private String m_title;

  public AbstractStatusDialog(Shell parentShell) {
    super(parentShell);
    m_fieldToolkit = new FieldToolkit();
    setHelpAvailable(false);
  }

  @Override
  public final void setMessage(String newMessage) {
    m_defaultOkStatus = new Status(m_defaultOkStatus.getSeverity(), m_defaultOkStatus.getPlugin(), newMessage);
    setStatus(m_status, m_defaultOkStatus);
  }

  @Override
  public void create() {
    super.create();
    pingStateChanging();
    super.setTitle(getTitle());
  }

  protected boolean isControlCreated() {
    return getContents() != null && !getContents().isDisposed();
  }

  /**
   * to force a revalidate if needed.
   */
  protected void pingStateChanging() {
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
    MultiStatus multiStatus = new MultiStatus(ScoutSdkUi.PLUGIN_ID, -1, "multi status", null);
    validate(multiStatus);
    setStatus(multiStatus, m_defaultOkStatus);
  }

  /**
   * overwrite this method to do some validation and
   * add additional status to the given multi status.
   *
   * @param multiStatus
   */
  protected void validate(MultiStatus multiStatus) {
  }

  public void setStatus(IStatus status, IStatus defaultOkStatus) {
    IStatus highestSeverityStatus = getHighestSeverityStatus(status, defaultOkStatus);
    boolean okEnabled = true;
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
        okEnabled = false;
        break;
      default:
        messagetype = IMessageProvider.NONE;
        break;
    }
    String message = highestSeverityStatus.getMessage();
    if (isControlCreated()) {
      getButton(IDialogConstants.OK_ID).setEnabled(okEnabled);
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

  public FieldToolkit getFieldToolkit() {
    return m_fieldToolkit;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return m_title;
  }

  /**
   * @param title
   *          the title to set
   */
  @Override
  public void setTitle(String title) {
    m_title = title;
  }
}
