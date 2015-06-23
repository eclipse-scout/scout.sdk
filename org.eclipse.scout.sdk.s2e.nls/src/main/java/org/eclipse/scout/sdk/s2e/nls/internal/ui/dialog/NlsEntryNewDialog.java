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
package org.eclipse.scout.sdk.s2e.nls.internal.ui.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.fields.TextField;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.formatter.IInputValidator;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.formatter.IValidationListener;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.formatter.InputValidator;
import org.eclipse.scout.sdk.s2e.nls.model.NlsEntry;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.swt.widgets.Shell;

public class NlsEntryNewDialog extends AbstractNlsEntryDialog {

  private final IValidationListener m_validationListener;

  public NlsEntryNewDialog(Shell parentShell, INlsProject project, boolean showProjectList) {
    this(parentShell, new NlsEntry("", project), project, showProjectList);
  }

  public NlsEntryNewDialog(Shell parentShell, NlsEntry row, INlsProject project, boolean showProjectList) {
    super(parentShell, "New Entry", row, project, showProjectList);
    m_validationListener = new IValidationListener() {
      @Override
      public void validationChanged(IStatus valid) {
        revalidate();
      }
    };
  }

  @Override
  protected void postCreate() {
    getKeyField().setInputValidator(new IInputValidator() {
      @Override
      public IStatus isValid(String value) {
        IInputValidator tmp = InputValidator.getNlsKeyValidator(getNlsProject());
        return tmp.isValid(value);
      }
    });

    getKeyField().removeValidationListener(m_validationListener);
    getKeyField().addValidationListener(m_validationListener);

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
