/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.nls.action;

import java.util.Locale;
import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.LanguageNewDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * <h4>LanguageNewAction</h4>
 */
public class LanguageNewAction extends Action {

  private final Shell m_shell;
  private final TranslationStoreStack m_project;

  public LanguageNewAction(TranslationStoreStack project, Shell s) {
    super("New Language...");
    m_project = project;
    m_shell = s;
    setEnabled(project.isEditable());
    setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.LanguageAdd));
  }

  @Override
  public void run() {
    LanguageNewDialog dialog = new LanguageNewDialog(m_shell, m_project);
    if (dialog.open() == Window.OK) {
      Language newLanguage = new Language(new Locale(dialog.getLanguageIso(), Optional.ofNullable(dialog.getCountryIso()).orElse("")));
      m_project.addNewLanguage(newLanguage, dialog.getStore());
    }
  }
}
