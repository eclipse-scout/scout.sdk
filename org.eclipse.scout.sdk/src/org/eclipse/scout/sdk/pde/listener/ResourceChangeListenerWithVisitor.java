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
package org.eclipse.scout.sdk.pde.listener;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.ScoutSdk;

public class ResourceChangeListenerWithVisitor implements IResourceChangeListener, IResourceDeltaVisitor {

  @Override
  public final void resourceChanged(IResourceChangeEvent e) {
    if (e.getDelta() != null) {
      try {
        e.getDelta().accept(this);
      }
      catch (CoreException ex) {
        ScoutSdk.logError(ex);
      }
    }
    else if (e.getResource() != null) {
      switch (e.getType()) {
        case IResourceChangeEvent.PRE_BUILD:
        case IResourceChangeEvent.PRE_CLOSE:
        case IResourceChangeEvent.PRE_DELETE: {
          beforeChange(e.getResource(), e.getType());
          break;
        }
        case IResourceChangeEvent.POST_BUILD:
        case IResourceChangeEvent.POST_CHANGE: {
          afterChange(e.getResource(), e.getType());
          break;
        }
      }
    }
  }

  @Override
  public final boolean visit(IResourceDelta delta) throws CoreException {
    return visitDelta(delta);
  }

  /**
   * override in subclasses
   */
  public boolean visitDelta(IResourceDelta delta) {
    return false;
  }

  /**
   * override in subclasses
   */
  public void beforeChange(IResource r, int type) {
  }

  /**
   * override in subclasses
   */
  public void afterChange(IResource r, int type) {
  }

}
