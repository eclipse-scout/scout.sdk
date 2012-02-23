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
package org.eclipse.scout.sdk.ui.wizard.page;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class PageNewWizard extends AbstractWorkspaceWizard {

  // members
  private final IScoutBundle m_clientBundle;
  private IType m_holderType;
  private IType m_superType;
  // pages
  private PageNewTemplatesWizardPage m_templatePage;
  private PageNewAttributesWizardPage m_pageAttributePage;

  // private ClientBundleLocationWizardPage m_clientLocationPage;

  public PageNewWizard(IScoutBundle clientBundle) {
    m_clientBundle = clientBundle;
    setWindowTitle(Texts.get("NewPage"));
    m_templatePage = new PageNewTemplatesWizardPage(clientBundle);
    addPage(m_templatePage);
    m_pageAttributePage = new PageNewAttributesWizardPage(clientBundle);
    addPage(m_pageAttributePage);
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public void setHolderType(IType holderType) {
    m_holderType = holderType;
    m_pageAttributePage.setHolderType(holderType);
  }

  public IType getHolderType() {
    return m_holderType;
  }

  public void setSuperType(IType superType) {
    m_superType = superType;
    m_templatePage.setExcludePage(superType != null);
    m_pageAttributePage.setSuperType(superType);
  }

  public IType getSuperType() {
    return m_superType;
  }

}
