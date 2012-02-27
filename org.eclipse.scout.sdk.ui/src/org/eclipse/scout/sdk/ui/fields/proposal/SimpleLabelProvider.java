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

import org.eclipse.scout.sdk.ui.fields.proposal.styled.SearchRangeStyledLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link SimpleLabelProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 12.02.2012
 */
public class SimpleLabelProvider extends SearchRangeStyledLabelProvider {

  @Override
  public String getText(Object element) {
    if (element instanceof SimpleProposal) {
      return ((SimpleProposal) element).getText();
    }
    else {
      throw new IllegalArgumentException("SimpleLabelProvider works only with elements instanceof SimpleProposal!");
    }
  }

  @Override
  public String getTextSelected(Object element) {
    if (element instanceof SimpleProposal) {
      return ((SimpleProposal) element).getTextSelected();
    }
    else {
      throw new IllegalArgumentException("SimpleLabelProvider works only with elements instanceof SimpleProposal!");
    }
  }

  @Override
  public Image getImage(Object element) {
    if (element instanceof SimpleProposal) {
      return ((SimpleProposal) element).getImage();
    }
    else {
      throw new IllegalArgumentException("SimpleLabelProvider works only with elements instanceof SimpleProposal!");
    }
  }

  @Override
  public Image getImageSelected(Object element) {
    if (element instanceof SimpleProposal) {
      return ((SimpleProposal) element).getImageSelected();
    }
    else {
      throw new IllegalArgumentException("SimpleLabelProvider works only with elements instanceof SimpleProposal!");
    }
  }

}
