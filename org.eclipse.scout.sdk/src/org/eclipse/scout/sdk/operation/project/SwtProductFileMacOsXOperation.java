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
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link SwtProductFileMacOsXOperation}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.8.0 19.01.2012
 */
@SuppressWarnings("restriction")
public class SwtProductFileMacOsXOperation extends AbstractScoutProjectNewOperation {

  private IFile[] m_swtProdFiles;

  @Override
  public boolean isRelevant() {
    return Platform.OS_MACOSX.equals(Platform.getOS()) &&
        Platform.ARCH_X86.equals(Platform.getOSArch()) &&
        isNodeChecked(CreateUiSwtPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    ArrayList<IFile> productFiles = new ArrayList<IFile>(2);
    IFile dev = getProperties().getProperty(CreateUiSwtPluginOperation.PROP_PRODUCT_FILE_DEV, IFile.class);
    if (dev != null) productFiles.add(dev);

    IFile prod = getProperties().getProperty(CreateUiSwtPluginOperation.PROP_PRODUCT_FILE_PROD, IFile.class);
    if (prod != null) productFiles.add(prod);

    m_swtProdFiles = productFiles.toArray(new IFile[productFiles.size()]);
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    if (m_swtProdFiles == null || m_swtProdFiles.length != 2) {
      throw new IllegalArgumentException("dev or prod swt product file not found.");
    }
  }

  @Override
  public String getOperationName() {
    return "Change SWT Products for Mac OS X";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // in OS X the SWT fragment Name is without the ARCH suffix -> remove suffix when present
    final Pattern PLUGIN_PATTERN = Pattern.compile("org\\.eclipse\\.swt\\..*macosx.*x86.*");
    for (IFile f : m_swtProdFiles) {
      ProductFileModelHelper pfmh = new ProductFileModelHelper(f);
      for (IProductPlugin p : pfmh.ProductFile.getProduct().getPlugins()) {
        String id = p.getId();
        if (PLUGIN_PATTERN.matcher(id).matches()) {
          pfmh.ProductFile.removeDependency(id);
          String newName = id.replace(".x86", "");
          pfmh.ProductFile.addDependency(newName);
        }
      }
      pfmh.save();
    }
  }
}
