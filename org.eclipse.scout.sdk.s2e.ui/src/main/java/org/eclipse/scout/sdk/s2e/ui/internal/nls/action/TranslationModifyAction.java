/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.nls.action;

import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
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

  private final ITranslationEntry m_entry;
  private final TranslationStoreStack m_project;
  private final Shell m_parentShell;

  public TranslationModifyAction(Shell parentShell, ITranslationEntry entry, TranslationStoreStack stack) {
    super("Modify Entry...");
    m_entry = entry;
    m_project = stack;
    m_parentShell = parentShell;
    setEnabled(stack.isEditable());
    setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.Text));
  }

  @Override
  public void run() {
    AbstractTranslationDialog dialog = new TranslationModifyDialog(m_parentShell, m_project, m_entry);
    Optional<ITranslation> acceptedTranslation = dialog.show();
    acceptedTranslation.ifPresent(m_project::updateTranslation);
  }
}
