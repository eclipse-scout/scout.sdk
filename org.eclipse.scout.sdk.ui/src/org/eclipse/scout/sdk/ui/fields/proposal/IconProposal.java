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

import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>BCIconProposal</h3> ...
 */
public class IconProposal implements IContentProposalEx {

  private final Image m_image;
  private final ScoutIconDesc m_imageDescription;

  public IconProposal(ScoutIconDesc imageDescription, Image img) {
    m_imageDescription = imageDescription;
    m_image = img;

  }

  @Override
  public String getLabel(boolean selected, boolean expertMode) {
    return m_imageDescription.getIconName();
  }

  @Override
  public Image getImage(boolean selected, boolean expertMode) {
    return m_image;
  }

  @Override
  public int getCursorPosition(boolean selected, boolean expertMode) {
    return m_imageDescription.getIconName().length();
  }

  public ScoutIconDesc getImageDescription() {
    return m_imageDescription;
  }

  public Image getImage() {
    return m_image;
  }

}
