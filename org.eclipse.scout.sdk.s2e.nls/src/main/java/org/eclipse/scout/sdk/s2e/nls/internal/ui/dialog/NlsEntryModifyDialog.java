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
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.fields.TextField;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.formatter.IValidationListener;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.formatter.InputValidator;
import org.eclipse.scout.sdk.s2e.nls.model.NlsEntry;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.swt.widgets.Shell;

/** <h4>NlsEntryModifyDialog</h4> */
public class NlsEntryModifyDialog extends AbstractNlsEntryDialog {

  public NlsEntryModifyDialog(Shell parentShell, NlsEntry row, INlsProject project) {
    super(parentShell, "Modify Entry", row, project, false);
  }

  @Override
  protected void postCreate() {
    getKeyField().setInputValidator(InputValidator.getNlsKeyValidator(getNlsProject(), new String[]{getNlsEntry().getKey()}));
    getKeyField().setEnabled(false);
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
      setMessage("Modify this Translation entry.");
    }
    else {
      setMessage(highestSeverity);
    }
    getButton(Window.OK).setEnabled(highestSeverity.getSeverity() != IStatus.ERROR);
  }
}
