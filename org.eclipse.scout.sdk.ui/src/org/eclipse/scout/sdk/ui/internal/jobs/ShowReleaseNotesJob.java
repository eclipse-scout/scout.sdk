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
package org.eclipse.scout.sdk.ui.internal.jobs;

import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.ui.internal.views.HelpView;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Version;

@SuppressWarnings("restriction")
public class ShowReleaseNotesJob extends UIJob {
  private String m_urlString;
  private final Version m_newVersion;

  public ShowReleaseNotesJob(Version newVersion) {
    super("Release Notes");
    m_newVersion = newVersion;
    m_urlString = "/com.bsiag.bsicase.doc/help/general/releasenotes/release" + newVersion.getMajor() + "_" + newVersion.getMinor() + "_" + newVersion.getMicro() + ".html";
  }

  @Override
  public IStatus runInUIThread(IProgressMonitor monitor) {

    IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    URL url = activePage.getWorkbenchWindow().getWorkbench().getHelpSystem().resolve(m_urlString, true);
    try {
      String content = IOUtility.getContent(new InputStreamReader(url.openStream()));
      if (content.contains("<h1>Topic not found</h1>")) {
        ScoutSdkUi.logWarning("no releasenotes found for version " + m_newVersion.toString());
        return Status.OK_STATUS;
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning(e);
      return Status.CANCEL_STATUS;
    }
    try {
      IViewPart part = activePage.showView("org.eclipse.help.ui.HelpView", null, IWorkbenchPage.VIEW_VISIBLE);
      if (part != null) {
        HelpView view = (HelpView) part;
        view.showHelp(m_urlString);
        IWorkbenchPartReference ref = activePage.getReference(part);
        if (ref != null) {
          activePage.setPartState(ref, IWorkbenchPage.STATE_MAXIMIZED);
        }
        return Status.OK_STATUS;
      }
    }
    catch (PartInitException e) {
      ScoutSdkUi.logWarning("could not find help view.", e);
    }
    activePage.getWorkbenchWindow().getWorkbench().getHelpSystem().displayHelpResource(m_urlString);
    return Status.OK_STATUS;
  }

}
