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
package org.eclipse.scout.sdk.s2e.ui.fields.proposal;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;

/**
 * <h3>{@link IDialogSettingsProvider}</h3> Optional feature interface for dialog settings for
 * {@link IProposalContentProvider}s. If a content provider implements this interface and returns valid dialog settings,
 * the {@link ProposalPopup} remembers its size.
 *
 * @since 3.8.0 2012-02-22
 * @see IProposalContentProvider
 */
@FunctionalInterface
public interface IDialogSettingsProvider {

  /**
   * @return The {@link IDialogSettings} that stores the {@link PopupDialog} size.
   */
  IDialogSettings getDialogSettings();
}
