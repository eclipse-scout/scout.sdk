/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.BuildPropertyWizardPage;

public class BuildPropertyWizard extends AbstractWorkspaceWizard {

  private String m_directive;
  private String m_value;
  private Set<String> m_illegalNames;
  private BuildPropertyWizardPage m_wizardPage;

  public BuildPropertyWizard() {
    setWindowTitle(Texts.get("WsBuildDirective"));
  }

  @Override
  public void addPages() {
    m_wizardPage = new BuildPropertyWizardPage();
    m_wizardPage.setDirective(m_directive);
    m_wizardPage.setValue(m_value);
    m_wizardPage.setIllegalNames(m_illegalNames);
    addPage(m_wizardPage);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_directive = m_wizardPage.getDirective();
    m_value = m_wizardPage.getValue();
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    return true;
  }

  public String getDirective() {
    return m_directive;
  }

  public void setDirective(String directive) {
    m_directive = directive;
  }

  public String getValue() {
    return m_value;
  }

  public void setValue(String value) {
    m_value = value;
  }

  public Set<String> getIllegalNames() {
    return m_illegalNames;
  }

  public void setIllegalNames(Set<String> illegalNames) {
    m_illegalNames = illegalNames;
  }
}
