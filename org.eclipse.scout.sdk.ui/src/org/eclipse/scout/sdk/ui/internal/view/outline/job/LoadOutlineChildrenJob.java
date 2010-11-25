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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.jobs.AbstractWorkspaceBlockingJob;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

/**
 * only load page data and children
 * no gui actions are performed
 */
public class LoadOutlineChildrenJob extends AbstractWorkspaceBlockingJob {
  private IPage m_page;

  public LoadOutlineChildrenJob(IPage page) {
    super("Updating " + page.getName());
    m_page = page;
  }

  @Override
  protected void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    if (!m_page.isChildrenLoaded()) {
      m_page.loadChildren();
    }
  }
}
