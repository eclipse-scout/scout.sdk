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
package org.eclipse.scout.sdk.ui.action;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.actions.OrganizeImportsAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

public class OrganizeAllImportsAction extends Action {
  private IFile m_productFile;
  private IScoutProject m_scoutProject;

  private boolean m_debug;
  private ArrayList<ICompilationUnit> m_icuList = new ArrayList<ICompilationUnit>();

  public OrganizeAllImportsAction(IScoutProject group) {
    super("Organize all imports");
    m_scoutProject = group;
  }

  @Override
  public void run() {
    IWorkbenchPartSite site = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite();
    OrganizeImportsAction a = new OrganizeImportsAction(site);
    ArrayList<IJavaProject> list = new ArrayList<IJavaProject>();
    for (IScoutBundle b : getScoutProject().getAllScoutBundles()) {
      list.add(b.getJavaProject());
    }
    StructuredSelection sel = new StructuredSelection(list.toArray());
    a.run(sel);
  }

  public IScoutProject getScoutProject() {
    return m_scoutProject;
  }
}
