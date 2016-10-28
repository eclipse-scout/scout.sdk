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
package org.eclipse.scout.sdk.ui.workingset;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

public class ScoutBundleWorkingSetFactory implements IElementFactory {

  public static final String ID = "org.eclipse.scout.sdk.ui.workingset.factory";
  public static final String TAG_SYMBOLIC_NAME = "scoutBundleSymbolicName";

  @Override
  public IAdaptable createElement(IMemento memento) {
    String symbolicName = memento.getString(TAG_SYMBOLIC_NAME);
    if (StringUtility.hasText(symbolicName)) {
      symbolicName = symbolicName.trim();
      return ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(symbolicName);
    }
    return null;
  }
}