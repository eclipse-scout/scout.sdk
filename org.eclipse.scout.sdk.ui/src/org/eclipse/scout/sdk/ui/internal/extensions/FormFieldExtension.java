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
package org.eclipse.scout.sdk.ui.internal.extensions;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.extensions.AbstractFormFieldWizard;
import org.eclipse.scout.sdk.ui.extensions.IFormFieldExtension;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;

public class FormFieldExtension implements IFormFieldExtension {

  private final IType m_modelType;
  private final String m_name;
  private Class<? extends AbstractFormFieldWizard> m_newWizardClazz;
  private Class<? extends AbstractScoutTypePage> m_nodePageClazz;

  private boolean m_inShortList;

  public FormFieldExtension(String name, IType modelType) {
    m_name = name;
    m_modelType = modelType;
  }

  @Override
  public AbstractWorkspaceWizard createNewWizard() {
    if (m_newWizardClazz == null) {
      ScoutSdkUi.logWarning("new Wizard is not defined");
      return null;
    }
    try {
      return m_newWizardClazz.newInstance();
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not instansiate new wizard '" + m_newWizardClazz.getName() + "'.", e);
      return null;
    }
  }

  @Override
  public IPage createNodePage() {
    if (m_nodePageClazz == null) {
      ScoutSdkUi.logWarning("node page is not defined");
      return null;
    }
    try {
      AbstractScoutTypePage nodePage = m_nodePageClazz.newInstance();
      return nodePage;
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not instansiate nodePage '" + m_nodePageClazz.getName() + "'.", e);
      return null;
    }
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public IType getModelType() {
    return m_modelType;
  }

  @Override
  public Class<? extends AbstractFormFieldWizard> getNewWizardClazz() {
    return m_newWizardClazz;
  }

  public void setNewWizardClazz(Class<? extends AbstractFormFieldWizard> newWizardClazz) {
    m_newWizardClazz = newWizardClazz;
  }

  @Override
  public Class<? extends AbstractScoutTypePage> getNodePage() {
    return m_nodePageClazz;
  }

  public void setNodePage(Class<? extends AbstractScoutTypePage> nodePage) {
    m_nodePageClazz = nodePage;
  }

  @Override
  public boolean isInShortList() {
    return m_inShortList;
  }

  public void setInShortList(boolean isShortList) {
    m_inShortList = isShortList;
  }

}
