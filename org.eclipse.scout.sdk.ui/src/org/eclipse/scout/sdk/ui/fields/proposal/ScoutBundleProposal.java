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

import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.graphics.Image;

public class ScoutBundleProposal implements IContentProposalEx {

  private String m_name;
  private final IScoutBundle m_bundle;

  public ScoutBundleProposal(IScoutBundle bundle) {
    m_bundle = bundle;
    String name = bundle.getBundleName();
    m_name = name;
  }

  @Override
  public int getCursorPosition(boolean selected, boolean expertMode) {
    return m_name.length();
  }

  @Override
  public Image getImage(boolean selected, boolean expertMode) {
    return null;
  }

  @Override
  public String getLabel(boolean selected, boolean expertMode) {
    return m_name;
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

}
