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
package org.eclipse.scout.sdk.process.bc.type;

/**
 * <h3>{@link IMoveTypes}</h3> ...
 * 
 * @deprecated
 */
@Deprecated
public interface IMoveTypes {
  static final int UP = 1;
  static final int DOWN = 2;
  static final int LEFT = 3;
  static final int RIGHT = 4;
  static final int TOP = 5;
  static final int BOTTOM = 6;
  static final int BEGIN = 7;
  static final int END = 8;

  // XXX add to NLS
  static final String[] TEXTS = new String[]{"", "up", "down", "left", "right", "top", "bottom", "begin", "end"};
  // static final String[] ICONS = new String[] { "", SDEUI.IMG_MOVE_UP, Icons.IMG_MOVE_DOWN, "left", "right", Icons.IMG_MOVE_TOP, Icons.IMG_MOVE_BOTTOM, "begin", "end" };
}
