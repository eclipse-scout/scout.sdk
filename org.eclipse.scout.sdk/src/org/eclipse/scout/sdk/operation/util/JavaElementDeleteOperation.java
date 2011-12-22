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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

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

  @Override
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

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    HashSet<ICompilationUnit> icuForOrganizeImports = new HashSet<ICompilationUnit>();
    for (IJavaElement m : m_typesToDelete) {
      deleteMember(m, icuForOrganizeImports, monitor, workingCopyManager);
    }
  }

  protected void deleteMember(IJavaElement member, Set<ICompilationUnit> icuForOrganizeImports, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    if (!member.exists()) {
      ScoutSdk.logWarning("Can not delete a non existing member '" + member.getElementName() + "'.");
      return;
    }
    switch (member.getElementType()) {
      case IJavaElement.IMPORT_DECLARATION:
        IImportDeclaration imp = (IImportDeclaration) member;
        IJavaElement ancestor = imp.getAncestor(IJavaElement.COMPILATION_UNIT);
        if (TypeUtility.exists(ancestor)) {
          manager.register((ICompilationUnit) ancestor, monitor);
        }
        imp.delete(true, monitor);
        break;
      case IJavaElement.TYPE:
        IType type = (IType) member;
        if (type.getDeclaringType() == null) {
          deleteCompilationUnit(type.getCompilationUnit(), monitor, manager);
        }
        else {
          ICompilationUnit icu = type.getCompilationUnit();
          manager.register(icu, false, monitor);
          type.delete(true, monitor);
          icuForOrganizeImports.add(icu);
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
        icuForOrganizeImports.add(method.getCompilationUnit());
        break;
      case IJavaElement.ANNOTATION:
        IAnnotation annotation = (IAnnotation) member;
        IJavaElement primEle = annotation.getAncestor(IJavaElement.COMPILATION_UNIT);
        manager.register((ICompilationUnit) primEle, monitor);
        member.getParent().getJavaModel().delete(new IJavaElement[]{member}, true, monitor);
        icuForOrganizeImports.add((ICompilationUnit) primEle);
        break;
      default:
        ScoutSdk.logWarning("no delete routine found for '" + member.getElementName() + "'.");
        break;
    }
  }

  private void deleteCompilationUnit(ICompilationUnit icu, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    IPackageFragment packageFragment = (IPackageFragment) icu.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
    manager.unregister(icu, monitor);
    icu.delete(true, monitor);
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
}
