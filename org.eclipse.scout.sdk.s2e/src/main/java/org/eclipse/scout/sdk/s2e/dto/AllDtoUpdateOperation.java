/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.dto;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.s.IRuntimeClasses;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.s2e.IOperation;
import org.eclipse.scout.sdk.s2e.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 *
 */
public class AllDtoUpdateOperation implements IOperation {

  private final Map<IJavaProject, ILookupEnvironment> m_lookupEnvs;

  public AllDtoUpdateOperation() {
    m_lookupEnvs = new HashMap<>();
  }

  @Override
  public String getOperationName() {
    return "Update DTOs...";
  }

  @Override
  public void validate() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    Collection<org.eclipse.jdt.core.IType> jdtTypesToBuild = getTypesToBuild();
    monitor.beginTask("Updating DTOs", jdtTypesToBuild.size());
    try {
      for (org.eclipse.jdt.core.IType jdtType : jdtTypesToBuild) {
        processType(jdtType, monitor, workingCopyManager);
        if (monitor.isCanceled()) {
          return;
        }
        monitor.worked(1);
      }
    }
    finally {
      m_lookupEnvs.clear();
      monitor.done();
    }
  }

  private void processType(org.eclipse.jdt.core.IType jdtType, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    try {
      IType t = jdtTypeToScoutType(jdtType);
      if (t == null) {
        return;
      }
      if (monitor.isCanceled()) {
        return;
      }
      IProject modelProject = jdtType.getJavaProject().getProject();
      for (IDtoAutoUpdateHandler updateHandler : ScoutSdkCore.getDtoAutoUpdateManager().getUpdateHandlers()) {
        IDtoAutoUpdateOperation operation = updateHandler.createUpdateOperation(t, modelProject);
        if (operation != null) {
          operation.validate();
          operation.run(monitor, workingCopyManager);
          return;
        }
      }
    }
    catch (Exception e) {
      S2ESdkActivator.logError("Error while updating DTO for '" + jdtType.getFullyQualifiedName() + "'.", e);
    }
  }

  private ILookupEnvironment getLookupEnv(IJavaProject jp) throws CoreException {
    ILookupEnvironment lookupEnvironment = m_lookupEnvs.get(jp);
    if (lookupEnvironment == null) {
      lookupEnvironment = ScoutSdkCore.createLookupEnvironment(jp, true);
      m_lookupEnvs.put(jp, lookupEnvironment);
    }
    return lookupEnvironment;
  }

  private IType jdtTypeToScoutType(org.eclipse.jdt.core.IType jdtType) throws CoreException {
    ILookupEnvironment lookupEnv = getLookupEnv(jdtType.getJavaProject());
    return JdtUtils.jdtTypeToScoutType(jdtType, lookupEnv);
  }

  protected Collection<org.eclipse.jdt.core.IType> getTypesToBuild() throws CoreException {
    String[] baseTypes = new String[]{IRuntimeClasses.IFormField, IRuntimeClasses.ITableExtension, IRuntimeClasses.IForm, IRuntimeClasses.IPageWithTable, IRuntimeClasses.IPageWithTableExtension};
    Map<CompositeObject, org.eclipse.jdt.core.IType> result = new TreeMap<>();
    for (int i = 0; i < baseTypes.length; i++) {
      for (org.eclipse.jdt.core.IType baseType : JdtUtils.resolveJdtTypes(baseTypes[i])) {
        ITypeHierarchy typeHierarchy = baseType.newTypeHierarchy(null);
        for (org.eclipse.jdt.core.IType candidate : typeHierarchy.getAllClasses()) {
          if (acceptType(candidate)) {
            result.put(new CompositeObject(i, candidate.getElementName(), candidate.getFullyQualifiedName(), Integer.valueOf(candidate.hashCode())), candidate);
          }
        }
      }
    }
    return result.values();
  }

  protected boolean acceptType(org.eclipse.jdt.core.IType jdtType) throws CoreException {
    return JdtUtils.exists(jdtType) && !jdtType.isAnonymous() && !jdtType.isBinary() && jdtType.getDeclaringType() == null && Flags.isPublic(jdtType.getFlags());
  }
}
