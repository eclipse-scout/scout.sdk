/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.fields.proposal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <h3>{@link IProposalDescriptionProvider}</h3> The {@link IProposalDescriptionProvider} can be used in
 * {@link ProposalTextField#setProposalDescriptionProvider(IProposalDescriptionProvider)} to display user defined
 * descriptions for a selected proposal.
 *
 * @since 5.2.0
 */
public interface IProposalDescriptionProvider {

  /**
   * Calculates the description content for the given proposal element. This method is executed in a worker thread.
   *
   * @param proposal
   *          The proposal for which the description data should be returned.
   * @param monitor
   *          The {@link IProgressMonitor} of the worker thread.
   * @return The description content object that will be passed to {@link #createDescriptionControl(Composite, Object)}
   *         (only if not {@code null}).
   */
  Object createDescriptionContent(Object proposal, IProgressMonitor monitor);

  /**
   * Create the UI control based on the given parent {@link Composite} and the given content {@link Object}. This method
   * is executed in the SWT display thread.
   *
   * @param parent
   *          The parent {@link Composite}.
   * @param content
   *          The content data as calculated by {@link #createDescriptionContent(Object, IProgressMonitor)} before.
   *          Never is {@code null}.
   * @return The created control to display in the description area or {@code null} if nothing should be displayed.
   */
  Control createDescriptionControl(Composite parent, Object content);

}
