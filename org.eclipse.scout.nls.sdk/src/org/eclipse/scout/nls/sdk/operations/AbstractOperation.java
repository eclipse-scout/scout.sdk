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
package org.eclipse.scout.nls.sdk.operations;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.NlsStatusDialog;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractOperation implements IWorkspaceRunnable {

  public final void runBlockWorkspace() {
    try {
      JavaCore.run(new P_WorkspaceOperation(), null);
    }
    catch (final Exception e) {
      NlsCore.logError("exception during AbstractOpertion '" + getClass().getName() + "'. ", e);
      Display.getDefault().asyncExec(new Runnable() {
        @Override
        public void run() {
          new NlsStatusDialog(Display.getDefault().getActiveShell(), new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, "could not run operation ", e));
        }
      });
    }
  }

  private class P_WorkspaceOperation implements IWorkspaceRunnable {
    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
      AbstractOperation.this.run(monitor);
    }
  }
}
