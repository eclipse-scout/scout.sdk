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
package org.eclipse.scout.sdk.ui.menu;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.ui.IStartup;

/**
 * This class is called as soon as the eclipse IDE UI is shown.<br>
 * It ensures that the scout SDK plugins get activated.<br>
 * This is important to e.g. ensure that the automatic formdata update
 * is executed even if the scout perspective (or any other scout sdk classes)
 * would never be loaded (e.g. when only working in the java perspective).
 *
 * @author Matthias Villiger
 * @since 3.8.0 24.01.2012
 */
public class ScoutSdkStartupExtension implements IStartup {

  @Override
  public void earlyStartup() {
    Job j = new Job("init PDE") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        JdtUtility.getNewestBundleInActiveTargetPlatform("org.eclipse.core.runtime"); // init PDE
        return Status.OK_STATUS;
      }
    };
    j.setUser(false);
    j.setSystem(true);
    j.schedule();
  }
}
