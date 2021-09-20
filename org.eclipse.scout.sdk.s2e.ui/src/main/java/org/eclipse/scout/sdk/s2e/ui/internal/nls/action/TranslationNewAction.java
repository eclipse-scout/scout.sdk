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
package org.eclipse.scout.sdk.s2e.ui.internal.nls.action;

import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;

import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.Translation;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.AbstractTranslationDialog;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.TranslationNewDialog;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.NlsTableController;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link TranslationNewAction}</h3>
 *
 * @since 7.0.0
 */
public class TranslationNewAction extends Action {

  // in
  private final ITranslation m_initialEntry;
  private final TranslationManager m_manager;
  private final Shell m_shell;
  private final NlsTableController m_controller; // may be null

  // out
  private ITranslation m_createdTranslation;

  public TranslationNewAction(Shell shell, TranslationManager manager, NlsTableController controller) {
    this(shell, manager, null, controller);
  }

  public TranslationNewAction(Shell shell, TranslationManager manager, ITranslation entry) {
    this(shell, manager, entry, null);
  }

  protected TranslationNewAction(Shell shell, TranslationManager manager, ITranslation entry, NlsTableController controller) {
    super("New Translation...");
    m_controller = controller;
    m_shell = Ensure.notNull(shell);
    m_manager = Ensure.notNull(manager);
    m_initialEntry = Optional.ofNullable(entry).orElseGet(() -> new Translation(""));

    setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.TextAdd));
    setEnabled(manager.isEditable());
  }

  @Override
  public void run() {
    AbstractTranslationDialog dialog = new TranslationNewDialog(m_shell, m_manager, m_initialEntry);
    dialog.show().ifPresent(entry -> {
      m_createdTranslation = entry;
      m_manager.setTranslation(entry, dialog.getSelectedStore().orElse(null));
      if (m_controller == null) {
        // no controller available -> directly store. Action has been used outside the editor (e.g. the code completion proposal).
        runInEclipseEnvironment(m_manager::flush);
      }
      else {
        // controller available: show created entry. store will be handled by the controller.
        m_controller.reveal(entry.key());
      }
    });
  }

  public Optional<ITranslation> getCreatedTranslation() {
    return Optional.ofNullable(m_createdTranslation);
  }
}
