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

import java.util.HashMap;

public class ProposalPopupEvent {

  public static final int TYPE_PROPOSAL_ACCEPTED = 1 << 1;
  public static final int TYPE_PROPOSAL_SELECTED = 1 << 2;
  public static final int TYPE_POPUP_CLOSED = 1 << 3;
  public static final int TYPE_SEARCH_SHORTENED = 1 << 4;

  public static final String IDENTIFIER_SELECTION_SEARCH_SHORTENED = "selectionSearchShortened";
  public static final String IDENTIFIER_SELECTED_PROPOSAL = "selectedProposal";

  private final int m_type;
  private HashMap<String, Object> m_values = new HashMap<>();

  public ProposalPopupEvent(int type) {
    m_type = type;
  }

  public void setData(String identifier, Object data) {
    m_values.put(identifier, data);
  }

  public Object getData(String identifier) {
    return m_values.get(identifier);
  }

  public int getType() {
    return m_type;
  }
}
