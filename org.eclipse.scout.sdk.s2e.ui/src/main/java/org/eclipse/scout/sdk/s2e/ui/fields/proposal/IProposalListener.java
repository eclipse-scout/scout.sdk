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

import java.util.EventListener;

/**
 * <h3>{@link IProposalListener}</h3> Listener to be notified when a new proposal has been selected by the user.
 *
 * @since 5.2.0
 */
@FunctionalInterface
public interface IProposalListener extends EventListener {
  /**
   * Notification that a proposal was selected by the user.
   *
   * @param proposal
   *          The selected proposal.
   */
  void proposalAccepted(Object proposal);
}
