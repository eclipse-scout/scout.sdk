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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link SelectionStateLabelProvider}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 09.02.2012
 */
public class SelectionStateLabelProvider extends LabelProvider implements ISelectionStateLabelProvider {

  @Override
  public Image getImageSelected(Object element) {
    return getImage(element);
  }

  @Override
  public String getTextSelected(Object element) {
    return getText(element);
  }
}
