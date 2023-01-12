/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroupCore.PositionInformation;

/**
 * <h3>{@link ICompletionProposalProvider}</h3>
 *
 * @since 5.2.0
 */
public interface ICompletionProposalProvider {

  Proposal[] getProposals();

  PositionInformation[] getPositions();

  void load();

  void addListener(ILinkedAsyncProposalListener listener);
}
