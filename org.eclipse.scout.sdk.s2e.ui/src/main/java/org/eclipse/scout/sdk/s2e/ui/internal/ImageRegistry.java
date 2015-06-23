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
package org.eclipse.scout.sdk.s2e.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ImageRegistry extends org.eclipse.jface.resource.ImageRegistry {

  private final Map<ImageDescriptor, Image> m_registry;
  private final Display m_device;

  public ImageRegistry(Display device) {
    super(device);
    m_registry = new HashMap<>();
    m_device = device;
  }

  @Override
  public void dispose() {
    super.dispose();
    for (Image image : m_registry.values()) {
      image.dispose();
    }
    m_registry.clear();
  }

  /**
   * Returns the image associated with the given image descriptor.
   *
   * @param descriptor
   *          the image descriptor for which the registry manages an image
   * @return the image associated with the image descriptor or <code>null</code> if the image descriptor can't create
   *         the requested image.
   */
  public Image get(ImageDescriptor descriptor) {
    if (descriptor == null) {
      descriptor = ImageDescriptor.getMissingImageDescriptor();
    }

    Image result = m_registry.get(descriptor);
    if (result != null) {
      return result;
    }
    result = descriptor.createImage(m_device);
    if (result != null) {
      m_registry.put(descriptor, result);
    }
    return result;
  }
}
