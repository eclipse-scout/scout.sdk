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
package org.eclipse.scout.sdk.ui.internal.view.outline.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

/**
 * only load page data and children
 * no gui actions are performed
 */
public class LoadOutlineChildrenJob extends JobEx {
  private IPage m_page;
  private boolean m_isDone;

  public LoadOutlineChildrenJob(IPage page) {
    super("Updating " + page.getName());
    m_page = page;
    setDone(false);
  }

  @Override
  public IStatus run(IProgressMonitor monitor) {
    try {
      if (!m_page.isChildrenLoaded()) {
        m_page.loadChildren();
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logError("failed to load children.", e);
    }
    finally {
      setDone(true);
    }
    return Status.OK_STATUS;
  }

  private void setDone(boolean isDone) {
    m_isDone = isDone;
  }

  public boolean isDone() {
    return m_isDone;
  }
}
