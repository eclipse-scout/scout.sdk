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
package org.eclipse.scout.sdk.ui.fields.proposal;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * The IProposalDescriptionProvider can be used in the ProposalPopup to display user defined data.
 * If the user selects a proposal from the ProposalPopup, additional data can be displayed. This information
 * can be updated with every new selection the user takes.
 */
public interface IProposalDescriptionProvider {

  /**
   * @param parent
   *          Composite to add custom content as a description of the selected proposal
   * @param proposal
   *          the selected proposal or null
   * @return the content to display or null if no description is available.
   */
  public Control createDescriptionContent(Composite parent, Object proposal);

}
