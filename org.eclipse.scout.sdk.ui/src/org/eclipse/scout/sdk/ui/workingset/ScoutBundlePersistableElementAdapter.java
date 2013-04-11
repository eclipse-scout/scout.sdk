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

import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * <h3>{@link ScoutBundlePersistableElementAdapter}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 10.04.2013
 */
public class ScoutBundlePersistableElementAdapter implements IPersistableElement {

  private final IScoutBundle m_bundleToAdapt;

  public ScoutBundlePersistableElementAdapter(IScoutBundle bundleToAdapt) {
    m_bundleToAdapt = bundleToAdapt;
  }

  @Override
  public void saveState(IMemento memento) {
    memento.putString(ScoutBundleWorkingSetFactory.TAG_SYMBOLIC_NAME, m_bundleToAdapt.getSymbolicName());
  }

  @Override
  public String getFactoryId() {
    // specifies which factory will take care of loading the persisted element again
    return ScoutBundleWorkingSetFactory.ID;
  }
}
