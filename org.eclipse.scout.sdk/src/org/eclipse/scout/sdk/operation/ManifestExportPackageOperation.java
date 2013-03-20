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
package org.eclipse.scout.sdk.operation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>ManifestExportPackageOperation</h3> Scout SDK keeps the manifest exported packages part updated.
 * This Operation is used to add/remove exported packages from the manifest.
 */
public class ManifestExportPackageOperation implements IOperation {
  public static final int TYPE_REMOVE = 1 << 1;
  public static final int TYPE_REMOVE_WHEN_EMTPY = 1 << 2;
  public static final int TYPE_ADD = 1 << 5;
  public static final int TYPE_ADD_WHEN_NOT_EMTPY = 1 << 6;

  private IPackageFragment[] m_packages;
  private final int m_type;
  private final boolean m_avoidInternalMarkedExports;

  /**
   * @param type
   *          on of: {@link ManifestExportPackageOperation}{@value #TYPE_REMOVE}, {@link ManifestExportPackageOperation}
   *          {@value #TYPE_REMOVE_WHEN_EMTPY}, {@link ManifestExportPackageOperation}{@value #TYPE_ADD},
   *          {@link ManifestExportPackageOperation}{@value #TYPE_ADD_WHEN_NOT_EMTPY}
   * @param packageFragment
   *          the packages to operate on
   * @param avoidInternalMarkedExports
   *          to ensure packages with internal in its names will not be exported.
   */
  public ManifestExportPackageOperation(int type, IPackageFragment[] packageFragment, boolean avoidInternalMarkedExports) {
    m_type = type;
    m_packages = packageFragment;
    m_avoidInternalMarkedExports = avoidInternalMarkedExports;
  }

  @Override
  public String getOperationName() {
    switch (m_type) {
      case TYPE_REMOVE:
      case TYPE_REMOVE_WHEN_EMTPY:
        return Texts.get("Operation_removeExportedPackage");
      case TYPE_ADD:
      case TYPE_ADD_WHEN_NOT_EMTPY:
        return Texts.get("Operation_addExportedPackage");
      default:
        return "";
    }
  }

  @Override
  public void validate() throws IllegalArgumentException {

  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    switch (m_type) {
      case TYPE_REMOVE_WHEN_EMTPY:
        runRemoveWhenEmpty(monitor);
        break;
      case TYPE_REMOVE:
        runRemove(monitor);
        break;
      case TYPE_ADD_WHEN_NOT_EMTPY:
        runAddWhenNotEmpty(monitor);
        break;
      case TYPE_ADD:
        runAdd(monitor);
        break;
    }
  }

  protected void runAddWhenNotEmpty(IProgressMonitor p) throws CoreException {
    for (IPackageFragment pck : m_packages) {
      if (ScoutUtility.hasExistingChildren(pck, false)) {
        addPackage(pck);
      }
    }
  }

  protected void runAdd(IProgressMonitor p) throws CoreException {
    for (IPackageFragment pck : m_packages) {
      addPackage(pck);
    }
  }

  protected void addPackage(IPackageFragment pck) throws CoreException {
    if (m_avoidInternalMarkedExports && pck.getElementName().indexOf("internal") >= 0) {
      ScoutSdk.logInfo("the package: " + pck.getElementName() + " has not be added to the manifests exported packages. (internal package)");
      return;
    }
    PluginModelHelper h = new PluginModelHelper(pck.getJavaProject().getProject());
    h.Manifest.addExportPackage(pck);
    h.save();
  }

  protected void runRemoveWhenEmpty(IProgressMonitor p) throws CoreException {
    for (IPackageFragment pck : m_packages) {
      if (ScoutUtility.hasExistingChildren(pck, false)) {
        return;
      }
      else {
        removePackage(pck);
      }
    }
  }

  protected void runRemove(IProgressMonitor p) throws CoreException {
    for (IPackageFragment pck : m_packages) {
      removePackage(pck);
    }
  }

  protected void removePackage(IPackageFragment pck) throws CoreException {
    PluginModelHelper h = new PluginModelHelper(pck.getJavaProject().getProject());
    h.Manifest.removeExportPackage(pck);
    h.save();
  }
}
