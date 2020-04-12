/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
