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
