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
package org.eclipse.scout.sdk.ui.wizard;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

/**
 * <h3>AbstractWizard</h3> ...
 */
public abstract class AbstractWizard extends Wizard implements IWizard {

  public AbstractWizard(AbstractScoutWizardPage... pages) {
    for (AbstractScoutWizardPage page : pages) {
      addPage(page);
    }
    setDialogSettings(ScoutSdkUi.getDefault().getDialogSettingsSection(getDialogSettingsKey(), true));
  }

  /**
   * may be overwritten to provide a special dialog settings key.
   * 
   * @return a dialog settings key (default is the fqn of the wizard.
   */
  protected String getDialogSettingsKey() {
    return getClass().getName();
  }

  @Override
  public void addPage(IWizardPage page) {
    if (page instanceof AbstractScoutWizardPage) {
      super.addPage(page);
    }
    else {
      throw new IllegalArgumentException("Expecting an instance of '" + AbstractScoutWizardPage.class.getName() + "'.");
    }
  }

  @Override
  public AbstractScoutWizardPage getPage(String name) {
    return (AbstractScoutWizardPage) super.getPage(name);
  }

  @Override
  public IWizardPage getStartingPage() {
    AbstractScoutWizardPage startingPage = (AbstractScoutWizardPage) super.getStartingPage();
    if (startingPage.isExcludePage()) {
      return getNextPage(startingPage);
    }
    return startingPage;
  }

  @Override
  public IWizardPage getNextPage(IWizardPage page) {
    List<IWizardPage> pages = Arrays.asList(getPages());
    int index = pages.indexOf(page);
    if (index == pages.size() - 1 || index == -1) {
      return null;
    }
    AbstractScoutWizardPage nextPage = (AbstractScoutWizardPage) pages.get(index + 1);
    if (nextPage.isExcludePage()) {
      return getNextPage(nextPage);
    }
    else {
      return nextPage;
    }
  }

  @Override
  public IWizardPage getPreviousPage(IWizardPage page) {
    AbstractScoutWizardPage prevPage = (AbstractScoutWizardPage) super.getPreviousPage(page);
    if (prevPage != null && prevPage.isExcludePage()) {
      return getPreviousPage(prevPage);
    }
    else {
      return prevPage;
    }
  }

  @Override
  public boolean canFinish() {
    for (IWizardPage page : getPages()) {
      AbstractScoutWizardPage bcPage = (AbstractScoutWizardPage) page;
      if (!bcPage.isExcludePage() && !bcPage.isPageComplete()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean performFinish() {
    for (IWizardPage page : getPages()) {
      AbstractScoutWizardPage bcPage = (AbstractScoutWizardPage) page;
      if (!bcPage.performFinish()) {
        return false;
      }
    }
    return true;
  }

}
