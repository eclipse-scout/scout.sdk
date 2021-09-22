/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls;

import static java.util.stream.Collectors.toList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizardPage;
import org.eclipse.swt.widgets.Shell;

/**
 * <h4>NlsEntryModifyDialog</h4>
 */
public class TranslationModifyDialog extends AbstractTranslationDialog {

  public TranslationModifyDialog(Shell parentShell, TranslationManager project, IStackedTranslation translation) {
    super(parentShell, "Modify Entry", translation, project, translation.languagesOfAllStores().collect(toList()), false);
  }

  @Override
  protected void postCreate() {
    getKeyField().setEnabled(false);

    var defaultField = getDefaultTranslationField();
    if (defaultField != null) {
      defaultField.addModifyListener(e -> revalidate());
    }

    revalidate();
  }

  @Override
  protected void revalidate() {
    var defaultLangField = getTranslationField(Language.LANGUAGE_DEFAULT);
    if (defaultLangField == null) {
      return;
    }

    var status = TranslationInputValidator.validateDefaultTranslation(defaultLangField.getText());
    if (status.isOK()) {
      setMessage("Create a new translation entry.");
    }
    else {
      setMessage(AbstractWizardPage.getHighestSeverityStatus(status));
    }
    getButton(Window.OK).setEnabled(status.getSeverity() != IStatus.ERROR);
  }
}
