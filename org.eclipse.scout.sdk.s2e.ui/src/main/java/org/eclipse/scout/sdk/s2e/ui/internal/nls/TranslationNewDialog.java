/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizardPage;
import org.eclipse.swt.widgets.Shell;

public class TranslationNewDialog extends AbstractTranslationDialog {

  public TranslationNewDialog(Shell parentShell, TranslationStoreStack project, ITranslation row) {
    super(parentShell, "New Entry", row, project, true);
  }

  @Override
  protected void postCreate() {
    getKeyField().addModifyListener(e -> revalidate());
    TextField defaultField = getDefaultTranslationField();
    if (defaultField != null) {
      defaultField.addModifyListener(e -> revalidate());
    }
    revalidate();
  }

  @Override
  protected void revalidate() {
    MultiStatus status = new MultiStatus(S2ESdkUiActivator.PLUGIN_ID, -1, "multi status", null);
    status.add(TranslationInputValidator.validateNlsKey(getNlsProject(), getKeyField().getText()));

    TextField defaultLanguageField = getTranslationField(Language.LANGUAGE_DEFAULT);
    if (defaultLanguageField != null) {
      status.add(TranslationInputValidator.validateDefaultTranslation(defaultLanguageField.getText()));
    }

    if (status.isOK()) {
      setMessage("Create a new translation entry.");
    }
    else {
      setMessage(AbstractWizardPage.getHighestSeverityStatus(status));
    }
    getButton(Window.OK).setEnabled(status.getSeverity() != IStatus.ERROR);
  }
}
