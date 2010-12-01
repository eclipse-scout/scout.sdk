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
package org.eclipse.scout.sdk.ui.internal.fields.proposal;

import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalEx;
import org.eclipse.swt.graphics.Image;

public class BundleTypeProposal implements IContentProposalEx {

  private final String m_label;
  private final int m_type;

  public BundleTypeProposal(int type, String label) {
    m_type = type;
    m_label = label;

  }

  @Override
  public int getCursorPosition(boolean selected, boolean expertMode) {
    return getLabel().length();
  }

  @Override
  public Image getImage(boolean selected, boolean expertMode) {
    return ScoutSdkUi.getImage(ScoutSdkUi.Default);
  }

  @Override
  public String getLabel(boolean selected, boolean expertMode) {
    return getLabel();
  }

  public String getLabel() {
    return m_label;
  }

  public int getType() {
    return m_type;
  }

  @Override
  public int hashCode() {
    return getType();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BundleTypeProposal) {
      return ((BundleTypeProposal) obj).getType() == getType();
    }
    return false;
  }

}
