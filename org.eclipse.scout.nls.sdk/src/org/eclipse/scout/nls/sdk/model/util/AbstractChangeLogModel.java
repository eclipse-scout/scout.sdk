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
package org.eclipse.scout.nls.sdk.model.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractChangeLogModel {
  private List<IChangeLogListener> m_changeLogListeners = new LinkedList<IChangeLogListener>();

  public void addChangeLogListener(IChangeLogListener listener) {
    m_changeLogListeners.add(listener);
  }

  public void removeChangeLogListener(IChangeLogListener listener) {
    m_changeLogListeners.remove(listener);
  }

  protected void fireModelChanged() {
    List<IChangeLogListener> listeners = new ArrayList<IChangeLogListener>(m_changeLogListeners);
    for (IChangeLogListener listener : listeners) {
      listener.modelChanged();
    }
  }
}
