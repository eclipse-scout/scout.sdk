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
package org.eclipse.scout.sdk.ui.view.properties.part.multipage;

import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>UnknownPropertyViewPart</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 21.07.2010
 */
public class UnknownMultiPagePropertyViewPart extends AbstractMultiPageSectionBasedViewPart {

  @Override
  protected void updatePageName(Label l) {
    StringBuilder labelBuilder = new StringBuilder();
    labelBuilder.append("Pages '");
    IPage[] pages = getPages();
    for (int i = 0; i < pages.length; i++) {
      labelBuilder.append(pages[i].getName());
      if (i < pages.length - 1) {
        labelBuilder.append(", ");
      }
    }
    if (labelBuilder.length() > 150) {
      l.setToolTipText(labelBuilder.toString());
      l.setText(labelBuilder.subSequence(0, 147) + "...");
    }
    else {
      l.setText(labelBuilder.toString());
    }
  }
}
