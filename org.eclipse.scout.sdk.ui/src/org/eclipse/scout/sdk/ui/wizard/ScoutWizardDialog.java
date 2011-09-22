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
package org.eclipse.scout.sdk.ui.wizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ScoutWizardDialog extends WizardDialog implements IWizardPageContainer {

  public ScoutWizardDialog(IWizard newWizard) {
    this(ScoutSdkUi.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), newWizard);
  }

  @Override
  protected Control createContents(Composite parent) {
    // settings
    return super.createContents(parent);
  }

  public ScoutWizardDialog(Shell parentShell, IWizard newWizard) {
    super(parentShell, newWizard);
    super.addPageChangedListener(new IPageChangedListener() {
      @Override
      public void pageChanged(PageChangedEvent event) {
        // initially set the status
        if (event.getSelectedPage() instanceof AbstractScoutWizardPage) {
          AbstractScoutWizardPage selectedPage = (AbstractScoutWizardPage) event.getSelectedPage();
          setStatus(selectedPage.getStatus(), selectedPage.getDefaultOkStatus());
          selectedPage.postActivate();
        }
      }
    });
  }

  @Override
  public void showPage(IWizardPage page) {
    super.showPage(page);
    if (page instanceof AbstractScoutWizardPage) {
      ((AbstractScoutWizardPage) page).setFocus();
    }
    else {
      page.getControl().setFocus();
    }
  }

  @Override
  public void setStatus(IStatus status, IStatus defaultOkStatus) {
    IStatus highestSeverityStatus = getHighestSeverityStatus(status, defaultOkStatus);
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
}
