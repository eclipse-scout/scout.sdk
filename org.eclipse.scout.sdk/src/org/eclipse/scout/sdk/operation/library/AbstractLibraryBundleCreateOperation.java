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
package org.eclipse.scout.sdk.operation.library;

import java.io.File;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.validation.BundleValidator;

/**
 * <h3>{@link AbstractLibraryBundleCreateOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 11.03.2012
 */
public abstract class AbstractLibraryBundleCreateOperation implements IOperation {

  private String m_bundleName;
  private boolean m_unpack;
  private Set<File> m_libraryFiles;

  @Override
  public void validate() throws IllegalArgumentException {
    IStatus nameStatus = BundleValidator.validateNewBundleName(getBundleName());
    if (nameStatus.matches(IStatus.ERROR)) {
      throw new IllegalArgumentException(nameStatus.getMessage());
    }
    else if (nameStatus.matches(IStatus.WARNING)) {
      ScoutSdk.logWarning("Create a library bundle with warning bundle name status - " + nameStatus.getMessage());
    }
    if (getLibraryFiles() == null || getLibraryFiles().isEmpty()) {
      throw new IllegalArgumentException("Library files can not be null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
  }

  public String getBundleName() {
    return m_bundleName;
  }

  public void setBundleName(String bundleName) {
    m_bundleName = bundleName;
  }

  public boolean isUnpack() {
    return m_unpack;
  }

  public void setUnpack(boolean unpack) {
    m_unpack = unpack;
  }

  public Set<File> getLibraryFiles() {
    return m_libraryFiles;
  }

  public void setLibraryFiles(Set<File> libraryFiles) {
    m_libraryFiles = libraryFiles;
  }

}
