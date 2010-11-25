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
package org.eclipse.scout.sdk.ui.fields.bundletree;

import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>DdnFilters</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 02.02.2010
 */
public class TreeDropFilters {

  public static IDropFilter getSharedFilter() {
    return new IDropFilter() {
      @Override
      public boolean canDrop(ITreeNode destination) {
        return (destination.getData() instanceof IScoutBundle && ((IScoutBundle) destination.getData()).getType() == IScoutBundle.BUNDLE_SHARED);
      }
    };
  }

  public static IDropFilter getServerFilter() {
    return new IDropFilter() {
      @Override
      public boolean canDrop(ITreeNode destination) {
        return (destination.getData() instanceof IScoutBundle && ((IScoutBundle) destination.getData()).getType() == IScoutBundle.BUNDLE_SERVER);
      }
    };
  }

  public static IDropFilter getClientFilter() {
    return new IDropFilter() {
      @Override
      public boolean canDrop(ITreeNode destination) {
        return (destination.getData() instanceof IScoutBundle && ((IScoutBundle) destination.getData()).getType() == IScoutBundle.BUNDLE_CLIENT);
      }
    };
  }

}
