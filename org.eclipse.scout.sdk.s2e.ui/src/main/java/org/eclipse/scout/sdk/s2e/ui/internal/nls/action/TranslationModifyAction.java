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

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.AbstractTranslationDialog;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.TranslationModifyDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link TranslationModifyAction}</h3>
 *
 * @since 7.0.0
 */
public class TranslationModifyAction extends Action {

  private final IStackedTranslation m_entry;
  private final TranslationManager m_manager;
  private final Shell m_parentShell;

  public TranslationModifyAction(Shell parentShell, IStackedTranslation entry, TranslationManager manager) {
    super("Modify Entry...");
    m_entry = entry;
    m_manager = manager;
    m_parentShell = parentShell;
    setEnabled(manager.isEditable());
    setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.Text));
  }

  @Override
  public void run() {
    AbstractTranslationDialog dialog = new TranslationModifyDialog(m_parentShell, m_manager, m_entry);
    var acceptedTranslation = dialog.show();
    acceptedTranslation.ifPresent(m_manager::setTranslation);
  }
}
