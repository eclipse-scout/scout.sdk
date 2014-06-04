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
package org.eclipse.scout.sdk.util.typecache;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.jdt.IJavaResourceChangedListener;

/**
 * <h3>{@link IJavaResourceChangedEmitter}</h3>
 * 
 * @see IJavaResourceChangedListener
 */
public interface IJavaResourceChangedEmitter {
  /**
   * Adds a {@link IJavaResourceChangedListener} which will be notified when the inner types of the given {@link IType}
   * change.
   * 
   * @param type
   *          The listener will be notified when the inner types of this type change.
   * @param listener
   *          The listener to add.
   */
  void addInnerTypeChangedListener(IType type, IJavaResourceChangedListener listener);

  /**
   * Removes the given listener.
   * 
   * @param type
   *          The type the given listener was registered with.
   * @param listener
   *          The listener to remove.
   */
  void removeInnerTypeChangedListener(IType type, IJavaResourceChangedListener listener);

  /**
   * Adds the given {@link IJavaResourceChangedListener} which will be notified about any Java resource change.
   * 
   * @param listener
   *          The listener to add.
   */
  void addJavaResourceChangedListener(IJavaResourceChangedListener listener);

  /**
   * Removes the given {@link IJavaResourceChangedListener} from the list.
   * 
   * @param listener
   *          The {@link IJavaResourceChangedListener} to remove.
   */
  void removeJavaResourceChangedListener(IJavaResourceChangedListener listener);

  /**
   * Adds a {@link IJavaResourceChangedListener} which will be notified when the methods of the given {@link IType}
   * change.
   * 
   * @param type
   *          The listener will be notified when the methods of this type change.
   * @param listener
   *          The listener to add.
   */
  void addMethodChangedListener(IType type, IJavaResourceChangedListener listener);

  /**
   * Removes the given listener.
   * 
   * @param type
   *          The type the given listener was registered with.
   * @param listener
   *          The listener to remove
   */
  void removeMethodChangedListener(IType type, IJavaResourceChangedListener listener);

  /**
   * Stops the emitter from firing events and removes all listeners.
   */
  void dispose();
}
