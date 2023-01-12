/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.fields.proposal;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.scout.sdk.s2e.ui.util.NormalizedPattern;

/**
 * <h3>{@link IProposalContentProvider}</h3> Content provider interface for {@link ProposalTextField}s.
 *
 * @since 5.2.0
 * @see ProposalTextField#setContentProvider(IProposalContentProvider)
 */
@FunctionalInterface
public interface IProposalContentProvider extends IContentProvider {
  /**
   * Gets the proposals optionally filtered using the given {@link NormalizedPattern}.
   *
   * @param searchPattern
   *          The {@link NormalizedPattern} to filter the proposals based on the user input search string.
   * @param monitor
   *          The {@link IProgressMonitor} of the background proposal computation job.
   * @return A {@link Collection} or {@code null} with all proposals that should be shown to the user.
   */
  Collection<Object> getProposals(NormalizedPattern searchPattern, IProgressMonitor monitor);
}
