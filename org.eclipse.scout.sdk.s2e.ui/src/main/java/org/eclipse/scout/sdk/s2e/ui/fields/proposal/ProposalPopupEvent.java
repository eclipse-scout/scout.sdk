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

import java.util.HashMap;
import java.util.Map;

class ProposalPopupEvent {

  static final int TYPE_PROPOSAL_ACCEPTED = 1 << 1;
  static final int TYPE_PROPOSAL_SELECTED = 1 << 2;
  static final int TYPE_POPUP_CLOSED = 1 << 3;
  static final int TYPE_SEARCH_SHORTENED = 1 << 4;

  static final String IDENTIFIER_SELECTED_PROPOSAL = "selectedProposal";
  static final String IDENTIFIER_MOVE_FOCUS = "moveFocus";

  private final int m_type;
  private final Map<String, Object> m_values;

  ProposalPopupEvent(int type) {
    m_type = type;
    m_values = new HashMap<>();
  }

  void setData(String identifier, Object data) {
    m_values.put(identifier, data);
  }

  Object getData(String identifier) {
    return m_values.get(identifier);
  }

  int getType() {
    return m_type;
  }

  @Override
  public int hashCode() {
    var prime = 31;
    var result = 1;
    result = prime * result + m_type;
    result = prime * result + m_values.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    var other = (ProposalPopupEvent) obj;
    return m_type == other.m_type && m_values.equals(other.m_values);
  }
}
