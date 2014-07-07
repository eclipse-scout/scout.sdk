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
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>ManifestExportPackageOperation</h3> Scout SDK keeps the manifest exported packages part updated.
 * This Operation is used to add/remove exported packages from the manifest.
 */
public class ManifestExportPackageOperation implements IOperation {

  private IPackageFragment[] m_packages;
  private final ExportPolicy m_exportPolicy;
  private final boolean m_avoidInternalMarkedExports;

  public ManifestExportPackageOperation(ExportPolicy exportPolicy, IPackageFragment packageFragment, boolean avoidInternalMarkedExports) {
    this(exportPolicy, new IPackageFragment[]{packageFragment}, avoidInternalMarkedExports);
  }

  /**
   * @param exportPolicy
   * @param packageFragment
   *          the packages to operate on
   * @param avoidInternalMarkedExports
   *          to ensure packages with internal in its names will not be exported.
   */
  public ManifestExportPackageOperation(ExportPolicy exportPolicy, IPackageFragment[] packageFragment, boolean avoidInternalMarkedExports) {
    m_exportPolicy = exportPolicy;
    m_packages = packageFragment;
    m_avoidInternalMarkedExports = avoidInternalMarkedExports;
  }

  @Override
  public String getOperationName() {

    switch (getExportPolicy()) {
      case RemovePackage:
      case RemovePackageWhenEmpty:
        return Texts.get("Operation_removeExportedPackage");
      case AddPackage:
      case AddPackageWhenNotEmpty:
        return Texts.get("Operation_addExportedPackage");
      default:
        return "";
    }
  }

  @Override
  public void validate() {

  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    switch (getExportPolicy()) {
      case RemovePackageWhenEmpty:
        runRemoveWhenEmpty(monitor);
        break;
      case RemovePackage:
        runRemove(monitor);
        break;
      case AddPackageWhenNotEmpty:
        runAddWhenNotEmpty(monitor);
        break;
      case AddPackage:
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

  public ExportPolicy getExportPolicy() {
    return m_exportPolicy;
  }
}
