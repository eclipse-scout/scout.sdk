/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.trigger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.core.util.CoreUtils;

/**
 * <h3>{@link AbstractDerivedResourceHandler}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public abstract class AbstractDerivedResourceHandler implements IDerivedResourceHandler {

  @Override
  public final void run(IProgressMonitor monitor) throws CoreException {
    String backup = CoreUtils.getUsername();
    try {
      CoreUtils.setUsernameForThread("Scout robot");
      runImpl(monitor);
    }
    finally {
      CoreUtils.setUsernameForThread(backup);
    }
  }

  protected abstract void runImpl(IProgressMonitor monitor) throws CoreException;
}
