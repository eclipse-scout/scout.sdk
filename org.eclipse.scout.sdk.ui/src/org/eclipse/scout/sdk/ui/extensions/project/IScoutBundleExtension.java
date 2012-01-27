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
package org.eclipse.scout.sdk.ui.extensions.project;

import org.eclipse.scout.sdk.ui.extensions.bundle.IScoutBundleProvider;

/**
 *
 */
public interface IScoutBundleExtension {
  public static enum BundleTypes {
    GUI_Bundle,
    Client_Bundle,
    Shared_Bundle,
    Server_Bundle,
    Other
  }

  /**
   * @return
   */
  String getBundleID();

  /**
   * @return
   */
  String getBundleName();

  /**
   * @return
   */
  int getOrderNumber();

  /**
   * @return
   */
  IScoutBundleProvider getBundleExtention();

  /**
   * @return
   */
  String getIconPath();

  /**
   * @return
   */
  BundleTypes getBundleType();
}
