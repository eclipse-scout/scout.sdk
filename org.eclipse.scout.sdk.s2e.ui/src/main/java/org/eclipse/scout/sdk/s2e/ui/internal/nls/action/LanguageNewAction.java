/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.action;

import java.util.Locale;
import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.LanguageNewDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * <h4>LanguageNewAction</h4>
 */
public class LanguageNewAction extends Action {

  private final Shell m_shell;
  private final TranslationManager m_project;

  public LanguageNewAction(TranslationManager project, Shell s) {
    super("New Language...");
    m_project = project;
    m_shell = s;
    setEnabled(project.isEditable());
    setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.LanguageAdd));
  }

  @Override
  public void run() {
    var dialog = new LanguageNewDialog(m_shell, m_project);
    if (dialog.open() == Window.OK) {
      var newLanguage = new Language(new Locale(dialog.getLanguageIso(), Optional.ofNullable(dialog.getCountryIso()).orElse("")));
      m_project.addNewLanguage(newLanguage, dialog.getStore());
    }
  }
}
