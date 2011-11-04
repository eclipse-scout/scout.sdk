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
package org.eclipse.scout.nls.sdk.internal.ui.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.ui.TextField;
import org.eclipse.scout.nls.sdk.internal.ui.formatter.IValidationListener;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.ui.InputValidator;
import org.eclipse.swt.widgets.Shell;

public class NlsEntryNewDialog extends AbstractNlsEntryDialog {

  public NlsEntryNewDialog(Shell parentShell, INlsProject project) {
    this(parentShell, new NlsEntry("", project), project);
  }

  public NlsEntryNewDialog(Shell parentShell, NlsEntry row, INlsProject project) {
    super(parentShell, "New Entry", row, project);
  }

  @Override
  protected void postCreate() {
    getKeyField().setInputValidator(InputValidator.getNlsKeyValidator(getNlsProject()));
    getKeyField().addValidationListener(new IValidationListener() {
      @Override
      public void validationChanged(IStatus valid) {
        revalidate();
      }
    });

    TextField<String> defaultField = getDefaultTranslationField();
    defaultField.setInputValidator(InputValidator.getDefaultTranslationValidator());
    defaultField.addValidationListener(new IValidationListener() {
      @Override
      public void validationChanged(IStatus valid) {
        revalidate();
      }
    });

    revalidate();
  }

  @Override
  protected void revalidate() {
    MultiStatus status = new MultiStatus(NlsCore.PLUGIN_ID, -1, "multi status", null);
    status.add(getKeyField().getStatus());
    status.add(getDefaultTranslationField().getStatus());
    IStatus highestSeverity = NlsCore.getHighestSeverityStatus(status);
    if (highestSeverity.isOK()) {
      setMessage("Create a new Translation entry.");
    }
    else {
      setMessage(highestSeverity);
    }
    getButton(Dialog.OK).setEnabled(highestSeverity.getSeverity() != IStatus.ERROR);
  }
}
