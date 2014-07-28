/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.util.resources;

import java.lang.ref.WeakReference;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * <h3>{@link WeakResourceChangeListener}</h3>
 *
 * @author Matthias Villiger
 * @since 3.9.0 16.05.2013
 */
public class WeakResourceChangeListener implements IResourceChangeListener {

  private final WeakReference<IResourceChangeListener> m_weakListener;

  public WeakResourceChangeListener(IResourceChangeListener referent) {
    m_weakListener = new WeakReference<IResourceChangeListener>(referent);
  }

  @Override
  public void resourceChanged(IResourceChangeEvent event) {
    IResourceChangeListener listener = m_weakListener.get();
    if (listener != null) {
      listener.resourceChanged(event);
    }
    else {
      ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }
  }
}
