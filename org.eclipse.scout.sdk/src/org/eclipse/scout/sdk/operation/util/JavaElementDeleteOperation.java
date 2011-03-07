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
package org.eclipse.scout.sdk.operation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.ui.ide.undo.DeleteResourcesOperation;

/**
 * <h3> {@link JavaElementDeleteOperation}</h3> To delete any of method, type, import declaration, compilation unit.
 */
public class JavaElementDeleteOperation implements IOperation {

  private List<IJavaElement> m_typesToDelete;

  public JavaElementDeleteOperation() {
    m_typesToDelete = new ArrayList<IJavaElement>();
  }

  public void setMembers(IJavaElement[] members) {
    m_typesToDelete = new ArrayList<IJavaElement>(Arrays.asList(members));
  }

  public void addMember(IJavaElement type) {
    m_typesToDelete.add(type);
  }

  public boolean removeMember(IJavaElement type) {
    return m_typesToDelete.remove(type);
  }

  public String getOperationName() {
    StringBuilder builder = new StringBuilder();
    builder.append("delete ");
    for (IJavaElement t : m_typesToDelete) {
      builder.append(t.getElementName() + ", ");
    }
    builder.replace(builder.length() - 2, builder.length(), "");
    builder.append("...");
    return builder.toString();
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_typesToDelete == null) {
      throw new IllegalArgumentException("null argument for members not allowed.");
    }
    for (IJavaElement m : m_typesToDelete) {
      if (m == null) {
        throw new IllegalArgumentException("null member in the member array.");
      }
      if (!m.exists()) {
        throw new IllegalArgumentException("member '" + m.getElementName() + "' does not exist.");
      }
    }
  }

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    for (IJavaElement m : m_typesToDelete) {
      deleteMember(m, monitor, workingCopyManager);
    }
  }

  protected void deleteMember(IJavaElement member, IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
    if (!member.exists()) {
      ScoutSdk.logWarning("Can not delete a non existing member '" + member.getElementName() + "'.");
      return;
    }
    switch (member.getElementType()) {
      case IJavaElement.IMPORT_DECLARATION:
        IImportDeclaration imp = (IImportDeclaration) member;
        manager.register((ICompilationUnit) imp.getPrimaryElement(), monitor);
        imp.delete(true, monitor);
        break;
      case IJavaElement.TYPE:
        IType type = (IType) member;
        if (type.getDeclaringType() == null) {
          manager.unregister(type.getCompilationUnit(), monitor);
          deleteMember(type.getCompilationUnit(), monitor, manager);
        }
        else {
          ICompilationUnit icu = type.getCompilationUnit();
          manager.register(icu, false, monitor);
          // clear imports
          IImportDeclaration importDeclaration = icu.getImport(((IType) member).getFullyQualifiedName());
          deleteMember(importDeclaration, monitor, manager);
          type.delete(true, monitor);
          OrganizeImportOperation op = new OrganizeImportOperation(icu);
          op.run(monitor, manager);
        }
        break;
      case IJavaElement.COMPILATION_UNIT:
        deleteCompilationUnit((ICompilationUnit) member, monitor, manager);
        break;
      case IJavaElement.METHOD:
      case IJavaElement.FIELD:
        IMember method = (IMember) member;
        manager.register(method.getCompilationUnit(), monitor);
        method.delete(true, monitor);
        break;
      case IJavaElement.ANNOTATION:
        IAnnotation annotation = (IAnnotation) member;
        IJavaElement primEle = annotation.getAncestor(IJavaElement.COMPILATION_UNIT);
        manager.register((ICompilationUnit) primEle, monitor);
        member.getParent().getJavaModel().delete(new IJavaElement[]{member}, true, monitor);
        break;
      default:
        ScoutSdk.logWarning("no delete routine found for '" + member.getElementName() + "'.");
        break;
    }
  }

  private void deleteCompilationUnit(ICompilationUnit icu, IProgressMonitor monitor, IScoutWorkingCopyManager manager) {
    IPackageFragment packageFragment = (IPackageFragment) icu.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
    String resourceName = icu.getElementName();
    DeleteResourcesOperation op = new DeleteResourcesOperation(new IResource[]{icu.getResource()}, "Delete", true);
    try {
      op.execute(monitor, null);
      boolean deletePackage = true;
      for (IJavaElement e : packageFragment.getChildren()) {
        if (TypeUtility.exists(e)) {
          deletePackage = false;
        }
      }
      if (deletePackage) {
        PackageDeleteOperation packageOp = new PackageDeleteOperation(packageFragment);
        packageOp.validate();
        packageOp.run(monitor, manager);
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("error during deleting '" + resourceName + "'.", e);
    }
  }
}
