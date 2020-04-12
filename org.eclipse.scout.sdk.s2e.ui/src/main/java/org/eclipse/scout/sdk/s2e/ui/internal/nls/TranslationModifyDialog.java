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
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizardPage;
import org.eclipse.swt.widgets.Shell;

/**
 * <h4>NlsEntryModifyDialog</h4>
 */
public class TranslationModifyDialog extends AbstractTranslationDialog {

  public TranslationModifyDialog(Shell parentShell, TranslationStoreStack project, ITranslationEntry row) {
    super(parentShell, "Modify Entry", row, project, false);
    setSelectedStore(row.store());
  }

  @Override
  protected void postCreate() {
    getKeyField().setEnabled(false);

    TextField defaultField = getDefaultTranslationField();
    if (defaultField != null) {
      defaultField.addModifyListener(e -> revalidate());
    }

    revalidate();
  }

  @Override
  protected void revalidate() {
    TextField defaultLangField = getTranslationField(Language.LANGUAGE_DEFAULT);
    if (defaultLangField == null) {
      return;
    }

    IStatus status = TranslationInputValidator.validateDefaultTranslation(defaultLangField.getText());
    if (status.isOK()) {
      setMessage("Create a new translation entry.");
    }
    else {
      setMessage(AbstractWizardPage.getHighestSeverityStatus(status));
    }
    getButton(Window.OK).setEnabled(status.getSeverity() != IStatus.ERROR);
  }
}
