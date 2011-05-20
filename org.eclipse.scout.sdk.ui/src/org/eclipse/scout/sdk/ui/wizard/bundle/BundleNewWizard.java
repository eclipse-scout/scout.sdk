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
package org.eclipse.scout.sdk.ui.wizard.bundle;

import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class BundleNewWizard extends AbstractWorkspaceWizard {

  private BundleNewWizardPage m_page1;
  private final IScoutProject m_project;
  private IScoutBundle m_parentBundle;

  public BundleNewWizard(IScoutBundle parentBundle) {
    this(parentBundle.getScoutProject());
    setParentBundle(parentBundle);
    // create pages
  }

  public BundleNewWizard(IScoutProject project) {
    setWindowTitle("New Scout Bundle");
    m_project = project;
  }

  public IScoutProject getProject() {
    return m_project;
  }

  public void setParentBundle(IScoutBundle parentBundle) {
    m_parentBundle = parentBundle;
  }

  public IScoutBundle getParentBundle() {
    return m_parentBundle;
  }
}
