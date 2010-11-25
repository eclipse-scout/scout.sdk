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
package org.eclipse.scout.sdk.workspace;

public interface IScoutElement {
  public static final int PROJECT = 1;
  public static final int BUNDLE = 2;
  public static final int BUNDLE_CLIENT = 3;
  public static final int BUNDLE_SHARED = 4;
  public static final int BUNDLE_SERVER = 5;
  public static final int BUNDLE_UI_SWING = 6;
  public static final int BUNDLE_UI_SWT = 7;
  public static final int TYPE = 10;
  public static final int METHOD = 11;
  public static final int METHOD_EXEC = 12;
  public static final int METHOD_CONFIG_PROPERTY = 13;
  public static final int SERVICE_EXTENSION = 14;

  int getType();

}
