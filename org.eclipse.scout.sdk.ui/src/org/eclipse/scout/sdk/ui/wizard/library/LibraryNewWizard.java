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
package org.eclipse.scout.sdk.ui.wizard.library;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.operation.library.LibraryBundleCreateOperation;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link LibraryNewWizard}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 29.02.2012
 */
public class LibraryNewWizard extends AbstractWorkspaceWizard {

  private JarSelectionWizardPage m_jarSelectionWizardPage;
  private LibraryTypeWizardPage m_libraryWizardPage;
  private final IScoutBundle m_ownerBundle;

  public LibraryNewWizard(IScoutBundle ownerBundle) {
    m_ownerBundle = ownerBundle;
    m_jarSelectionWizardPage = new JarSelectionWizardPage();
    // listener to track jar selection
    m_jarSelectionWizardPage.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(JarSelectionWizardPage.PROP_JAR_FILES)) {
          // recompute bundle name
          StringBuilder nameBuilder = new StringBuilder();
          if (getOwnerBundle() != null) {
            String projectName = getOwnerBundle().getSymbolicName();
            if (StringUtility.hasText(projectName)) {
              nameBuilder.append(projectName).append(".");
            }
          }
          Set<File> jarFiles = m_jarSelectionWizardPage.getJarFiles();
          if (jarFiles != null && jarFiles.size() > 0) {
            String jarFileName = CollectionUtility.firstElement(jarFiles).getName();
            int dotIndex = jarFileName.lastIndexOf(".");
            if (dotIndex > 0) {
              nameBuilder.append(jarFileName.substring(0, dotIndex));
            }
            else {
              nameBuilder.append(jarFileName);
            }
          }
          else {
            nameBuilder.append("library");
          }
          int index = 1;
          String bundleName = nameBuilder.toString();
          while (Platform.getBundle(bundleName) != null) {
            bundleName = nameBuilder.toString() + index++;
          }
          m_libraryWizardPage.setBundleName(bundleName);
        }
      }
    });
    addPage(m_jarSelectionWizardPage);
    m_libraryWizardPage = new LibraryTypeWizardPage(ownerBundle);
    addPage(m_libraryWizardPage);
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // super call to call perform finish on all pages.
    super.performFinish(monitor, workingCopyManager);
    LibraryBundleCreateOperation operation = new LibraryBundleCreateOperation();
    operation.setBundleName(getLibraryWizardPage().getBundleName());
    operation.setLibraryFiles(getJarSelectionWizardPage().getJarFiles());
    switch (getLibraryWizardPage().getLibraryType()) {
      case PLUGIN:
        operation.setLibraryUserBundles(getLibraryWizardPage().getLibraryUserBundles());
        break;
      case FRAGMENT:
        operation.setFragmentHost(getLibraryWizardPage().getFragmentHost());
        break;
      case SYSTEM_BUNDLE_FRAGMENT:
        operation.setFragmentHost("system.bundle");
        break;
    }
    operation.validate();
    operation.run(monitor, workingCopyManager);
    return true;
  }

  public JarSelectionWizardPage getJarSelectionWizardPage() {
    return m_jarSelectionWizardPage;
  }

  public LibraryTypeWizardPage getLibraryWizardPage() {
    return m_libraryWizardPage;
  }

  public IScoutBundle getOwnerBundle() {
    return m_ownerBundle;
  }
}
