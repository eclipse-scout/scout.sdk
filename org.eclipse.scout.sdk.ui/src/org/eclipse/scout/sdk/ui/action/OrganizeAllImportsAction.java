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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.actions.OrganizeImportsAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

public class OrganizeAllImportsAction extends AbstractScoutHandler {
  private IScoutBundle m_scoutProject;

  public OrganizeAllImportsAction() {
    super(Texts.get("OrganizeAllImports"), null, null, false, Category.IMPORT);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    IWorkbenchPartSite site = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite();
    OrganizeImportsAction a = new OrganizeImportsAction(site);
    ArrayList<IJavaProject> list = new ArrayList<IJavaProject>();
    for (IScoutBundle b : getScoutProject().getChildBundles(ScoutBundleFilters.getAllBundlesFilter(), true)) {
      list.add(ScoutUtility.getJavaProject(b));
    }
    StructuredSelection sel = new StructuredSelection(list.toArray());
    a.run(sel);
    return null;
  }

  @Override
  public boolean isVisible() {
    return !m_scoutProject.isBinary();
  }

  public IScoutBundle getScoutProject() {
    return m_scoutProject;
  }

  public void setScoutProject(IScoutBundle scoutProject) {
    m_scoutProject = scoutProject;
  }
}
