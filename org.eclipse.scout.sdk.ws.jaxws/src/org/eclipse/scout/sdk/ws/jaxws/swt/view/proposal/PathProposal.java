/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.swt.view.proposal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalEx;
import org.eclipse.swt.graphics.Image;

public class PathProposal implements IContentProposalEx {

  private IPath m_path;

  public PathProposal(IPath path) {
    m_path = path;
  }

  public IPath getPath() {
    return m_path;
  }

  @Override
  public int getCursorPosition(boolean selected, boolean expertMode) {
    return 0;
  }

  @Override
  public Image getImage(boolean selected, boolean expertMode) {
    return ScoutSdkUi.getImage(ScoutSdkUi.Package);
  }

  @Override
  public String getLabel(boolean selected, boolean expertMode) {
    return m_path.toPortableString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_path == null) ? 0 : m_path.hashCode());
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
    PathProposal other = (PathProposal) obj;
    if (m_path == null) {
      if (other.m_path != null) {
        return false;
      }
    }
    else if (!m_path.equals(other.m_path)) {
      return false;
    }
    return true;
  }
}
