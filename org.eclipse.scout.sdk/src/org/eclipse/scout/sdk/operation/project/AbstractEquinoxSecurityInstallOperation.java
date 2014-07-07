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
package org.eclipse.scout.sdk.operation.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper.DependencyType;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link AbstractEquinoxSecurityInstallOperation}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.8.0 10.12.2012
 */
public abstract class AbstractEquinoxSecurityInstallOperation extends AbstractScoutProjectNewOperation {

  private IFile[] m_productFiles;

  @Override
  public boolean isRelevant() {
    return Platform.OS_MACOSX.equals(Platform.getOS()) || Platform.OS_WIN32.equals(Platform.getOS());
  }

  @Override
  public void init() {
    ArrayList<IFile> productFiles = new ArrayList<IFile>(2);
    contributeProductFiles(productFiles);
    m_productFiles = productFiles.toArray(new IFile[productFiles.size()]);
  }

  protected abstract void contributeProductFiles(List<IFile> l);

  @Override
  public String getOperationName() {
    return "Add platform dependent Equinox security fragments to product files.";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // equinox security fragments only exist for mac and windows platforms
    if (Platform.OS_MACOSX.equals(Platform.getOS()) || Platform.OS_WIN32.equals(Platform.getOS())) {
      for (IFile f : m_productFiles) {
        ProductFileModelHelper pfmh = new ProductFileModelHelper(f);
        pfmh.ProductFile.addDependency(getFragmentName(), DependencyType.Fragment);
        pfmh.save();
      }
    }
  }

  private String getFragmentName() {
    String name = "org.eclipse.equinox.security." + Platform.getOS();
    if (Platform.OS_WIN32.equals(Platform.getOS())) {
      name += "." + Platform.getOSArch();
    }
    return name;
  }
}
