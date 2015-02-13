/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.executor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.jdt.JdtRenameTransaction;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link ServiceRenameExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class ServiceRenameExecutor extends AbstractRenameExecutor {

  private IType m_serviceImplementation;
  private IType m_serviceInterface;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    boolean superRun = super.canRun(selection);
    if (!superRun) {
      return false;
    }

    Object selectedElement = selection.getFirstElement();

    if (selectedElement instanceof AbstractServiceNodePage) {
      AbstractServiceNodePage asnp = (AbstractServiceNodePage) selectedElement;
      m_serviceImplementation = asnp.getType();
      m_serviceInterface = asnp.getInterfaceType();
    }

    return isEditable(m_serviceImplementation);
  }

  @Override
  protected void fillTransaction(JdtRenameTransaction transaction, String newName) throws CoreException {
    if (TypeUtility.exists(m_serviceImplementation)) {
      transaction.add(m_serviceImplementation, newName);
    }

    final String newInterfaceName = "I" + newName;
    if (TypeUtility.exists(m_serviceInterface)) {
      transaction.add(m_serviceInterface, newInterfaceName);
    }

    final IScoutBundle interfaceBundle = ScoutTypeUtility.getScoutBundle(m_serviceInterface);
    final String oldFqn = m_serviceInterface.getFullyQualifiedName();
    final String newFqn = oldFqn.replace(m_serviceInterface.getElementName(), newInterfaceName);

    // rename the client proxies manually because they are are not renamed by JDT above!
    new OperationJob(new IOperation() {
      @Override
      public void validate() {
      }

      @Override
      public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
        IScoutBundleFilter filter = ScoutBundleFilters.getMultiFilterAnd(ScoutBundleFilters.getWorkspaceBundlesFilter(), ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT));
        for (IScoutBundle clientBundle : interfaceBundle.getChildBundles(filter, true)) {
          String attribName = "class";
          PluginModelHelper h = new PluginModelHelper(clientBundle.getProject());
          Map<String, String> attributes = new HashMap<>(1);
          attributes.put(attribName, oldFqn);
          List<IPluginElement> simpleExtensions = h.PluginXml.getSimpleExtensions(IRuntimeClasses.EXTENSION_POINT_CLIENT_SERVICE_PROXIES, IRuntimeClasses.EXTENSION_ELEMENT_CLIENT_SERVICE_PROXY, attributes);
          for (IPluginElement proxy : simpleExtensions) {
            proxy.setAttribute(attribName, newFqn);
          }
          h.save(); // only saves if there are changes
        }
      }

      @Override
      public String getOperationName() {
        return "Rename client proxies";
      }
    }).schedule();
  }

  @Override
  protected IStatus validate(String newName) {
    IStatus inheritedStatus = ScoutUtility.validateJavaName(newName, getReadOnlySuffix());
    if (inheritedStatus.matches(IStatus.ERROR)) {
      return inheritedStatus;
    }
    if (m_serviceImplementation != null) {
      String packName = m_serviceImplementation.getPackageFragment().getElementName();
      if (TypeUtility.existsType(packName + "." + newName)) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
      }
    }
    if (m_serviceInterface != null) {
      String packName = m_serviceInterface.getPackageFragment().getElementName();
      if (TypeUtility.existsType(packName + ".I" + newName)) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
      }
    }
    return inheritedStatus;
  }

}
