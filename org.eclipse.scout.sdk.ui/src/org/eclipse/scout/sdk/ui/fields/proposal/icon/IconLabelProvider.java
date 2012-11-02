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
package org.eclipse.scout.sdk.ui.fields.proposal.icon;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * <h3>{@link IconLabelProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 14.02.2012
 */
public class IconLabelProvider extends LabelProvider {
  private ImageRegistry m_imageRegistry;

  public IconLabelProvider(Display display) {
    m_imageRegistry = new ImageRegistry(display);
  }

  @Override
  public void dispose() {
    m_imageRegistry.dispose();
  }

  @Override
  public String getText(Object element) {
    ScoutIconDesc scoutDesc = (ScoutIconDesc) element;
    return scoutDesc.getIconName();
  }

  @Override
  public Image getImage(Object element) {
    ScoutIconDesc scoutDesc = (ScoutIconDesc) element;
    ImageDescriptor imageDescriptor = scoutDesc.getImageDescriptor();
    Image img = m_imageRegistry.get(scoutDesc.getId());
    if (img == null) {
      m_imageRegistry.put(scoutDesc.getId(), imageDescriptor);
      img = m_imageRegistry.get(scoutDesc.getId());
    }
    return img;
  }
}
