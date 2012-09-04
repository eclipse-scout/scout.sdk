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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.internal.PlatformVersionUtility;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link SwtProductFileUpgradeOperation}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 19.01.2012
 */
public class SwtProductFileUpgradeOperation extends AbstractScoutProjectNewOperation {

  private IFile[] m_swtProdFiles;

  @Override
  public boolean isRelevant() {
    return PlatformVersionUtility.isE4(getTargetPlatformVersion()) && isNodeChecked(CreateUiSwtPluginOperation.BUNDLE_ID);
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
    return "Upgrade SWT Products to Juno Level";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    final String[] additionalE4Plugins = new String[]{
        "org.eclipse.e4.core.commands",
        "org.eclipse.e4.core.contexts",
        "org.eclipse.e4.core.di",
        "org.eclipse.e4.core.di.extensions",
        "org.eclipse.e4.core.services",
        "org.eclipse.e4.ui.bindings",
        "org.eclipse.e4.ui.css.core",
        "org.eclipse.e4.ui.css.swt",
        "org.eclipse.e4.ui.css.swt.theme",
        "org.eclipse.e4.ui.di",
        "org.eclipse.e4.ui.model.workbench",
        "org.eclipse.e4.ui.services",
        "org.eclipse.e4.ui.widgets",
        "org.eclipse.e4.ui.workbench",
        "org.eclipse.e4.ui.workbench.addons.swt",
        "org.eclipse.e4.ui.workbench.renderers.swt",
        "org.eclipse.e4.ui.workbench.swt",
        "org.eclipse.e4.ui.workbench3",
        "org.eclipse.emf.common",
        "org.eclipse.emf.ecore",
        "org.eclipse.emf.ecore.change",
        "org.eclipse.emf.ecore.xmi",
        "org.eclipse.equinox.concurrent",
        "org.eclipse.equinox.ds",
        "org.eclipse.equinox.event",
        "org.eclipse.equinox.util",
        "org.eclipse.platform",
        "org.eclipse.ui.intro",
        "org.w3c.css.sac",
        "org.w3c.dom.smil",
        "org.w3c.dom.svg",
        "javax.annotation",
        "javax.inject",
        "javax.xml",
        "org.apache.batik.css",
        "org.apache.batik.util",
        "org.apache.batik.util.gui"
    };

    for (IFile f : m_swtProdFiles) {
      ProductFileModelHelper pfmh = new ProductFileModelHelper(f);

      // additional product file dependencies
      for (String plugin : additionalE4Plugins) {
        pfmh.ProductFile.addDependency(plugin);
      }

      // config.ini changes
      final String E4_ADDITIONAL_START = "org.eclipse.equinox.ds@3:start";
      final String INSERT_BEFORE = "org.eclipse.core.runtime@start";
      String oldEntry = pfmh.ConfigurationFile.getOsgiBundlesEntry();
      int pos = oldEntry.indexOf(INSERT_BEFORE);
      if (pos >= 0) {
        StringBuilder newEntry = new StringBuilder(oldEntry.substring(0, pos));
        newEntry.append(E4_ADDITIONAL_START);
        newEntry.append(",");
        newEntry.append(oldEntry.substring(pos));
        pfmh.ConfigurationFile.setOsgiBundlesEntry(newEntry.toString());
      }
      pfmh.save();
    }
  }
}
