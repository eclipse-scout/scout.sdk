/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.wizard;

import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;

/**
 * <h3>AbstractWizard</h3>
 */
public abstract class AbstractWizard extends Wizard {

  protected AbstractWizard() {
    setDialogSettings(S2ESdkUiActivator.getDefault().getDialogSettingsSection(getDialogSettingsKey()));
  }

  @Override
  public boolean performFinish() {
    if (!allPagesCanFinish()) {
      return false;
    }

    runInEclipseEnvironment(getFinishTask());

    return true;
  }

  public abstract WizardFinishTask<?> getFinishTask();

  protected boolean allPagesCanFinish() {
    for (IWizardPage page : getPages()) {
      if (page instanceof AbstractWizardPage) {
        AbstractWizardPage wizPage = (AbstractWizardPage) page;
        if (!wizPage.performFinish()) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * may be overwritten to provide a special dialog settings key.
   *
   * @return a dialog settings key (default is the fqn of the wizard.
   */
  protected String getDialogSettingsKey() {
    return getClass().getName();
  }

  /**
   * Helper method to create an instance of an {@link AbstractWizardPage} taking a {@link PackageContainer} instance in
   * the constructor.
   *
   * @param pageClass
   *          The page class to create an instance from.
   * @param packageContainer
   *          The {@link PackageContainer} to pass.
   * @return The created {@link AbstractWizardPage}.
   */
  protected static <T extends AbstractWizardPage> T createWizardPage(Class<T> pageClass, PackageContainer packageContainer) {
    try {
      return pageClass.getConstructor(PackageContainer.class).newInstance(packageContainer);
    }
    catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
      throw new SdkException(e);
    }
  }

  /**
   * Helper method to initialize this wizard using a primary page.
   * <p>
   * By default help is activated and a default page image is set.
   *
   * @param pageClass
   *          The page class that should be created.
   * @param packageContainer
   *          The {@link PackageContainer} passed to the constructor of the specified page class when creating the
   *          instance.
   * @return The created page.
   */
  protected <T extends AbstractWizardPage> T initNewClassWizardWithPage(Class<T> pageClass, PackageContainer packageContainer) {
    T page = createWizardPage(pageClass, packageContainer);
    addPage(page);
    setWindowTitle(page.getTitle());
    setHelpAvailable(true);
    setDefaultPageImageDescriptor(JavaPluginImages.DESC_WIZBAN_NEWCLASS);
    return page;
  }

  @Override
  public void addPage(IWizardPage page) {
    if (page instanceof AbstractWizardPage) {
      super.addPage(page);
    }
    else {
      throw new IllegalArgumentException("Expecting an instance of '" + AbstractWizardPage.class.getName() + "'.");
    }
  }

  @Override
  public AbstractWizardPage getPage(String name) {
    return (AbstractWizardPage) super.getPage(name);
  }

  @Override
  public IWizardPage getStartingPage() {
    AbstractWizardPage startingPage = (AbstractWizardPage) super.getStartingPage();
    if (startingPage != null && startingPage.isExcludePage()) {
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
    AbstractWizardPage nextPage = (AbstractWizardPage) pages.get(index + 1);
    if (nextPage.isExcludePage()) {
      return getNextPage(nextPage);
    }
    return nextPage;
  }

  @Override
  public IWizardPage getPreviousPage(IWizardPage page) {
    AbstractWizardPage prevPage = (AbstractWizardPage) super.getPreviousPage(page);
    if (prevPage != null && prevPage.isExcludePage()) {
      return getPreviousPage(prevPage);
    }
    return prevPage;
  }

  @Override
  public boolean canFinish() {
    for (IWizardPage page : getPages()) {
      AbstractWizardPage bcPage = (AbstractWizardPage) page;
      if (!bcPage.isExcludePage() && !bcPage.isPageComplete()) {
        return false;
      }
    }
    return true;
  }
}
