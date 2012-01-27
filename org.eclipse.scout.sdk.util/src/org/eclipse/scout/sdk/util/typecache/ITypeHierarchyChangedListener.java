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

import java.util.EventListener;

import org.eclipse.jdt.core.IType;

/**
 *
 */
public interface ITypeHierarchyChangedListener extends EventListener {

  public static final int PRE_TYPE_CHANGED = 1;
  public static final int POST_TYPE_CHANGED = 2;
  public static final int PRE_TYPE_ADDING = 3;
  public static final int POST_TYPE_ADDING = 4;
  public static final int PRE_TYPE_REMOVING = 5;
  public static final int POST_TYPE_REMOVING = 6;

  void handleEvent(int eventType, IType type);

}
