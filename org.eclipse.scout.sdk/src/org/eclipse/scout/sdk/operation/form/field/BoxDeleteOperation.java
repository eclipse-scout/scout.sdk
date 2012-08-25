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
package org.eclipse.scout.sdk.operation.form.field;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.util.ScoutSourceUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

public class BoxDeleteOperation implements IOperation {

  private final IType iColumn = TypeUtility.getType(RuntimeClasses.IColumn);
  private final IType m_boxType;
  private String m_name;

  public BoxDeleteOperation(IType boxType) {
    m_boxType = boxType;
    m_name = Texts.get("Action_deleteTypeX", ScoutSourceUtility.getTranslatedMethodStringValue(boxType, "getConfiguredLabel"));
  }

  @Override
  public String getOperationName() {
    return m_name;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getBoxType() == null) {
      throw new IllegalArgumentException("type to delete is null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    JavaElementDeleteOperation deleteOperation = new JavaElementDeleteOperation();
    deleteGetterMethodsAndImportsRec(getBoxType(), deleteOperation, monitor, workingCopyManager);
    deleteOperation.addMember(getBoxType());
    // collect all imports
    String normalizedTypeName = getBoxType().getFullyQualifiedName().replace('$', '.');
    for (IImportDeclaration imp : getBoxType().getCompilationUnit().getImports()) {
      String normalizedImport = imp.getElementName().replace('$', '.');
      if (normalizedImport.startsWith(normalizedTypeName)) {
        deleteOperation.addMember(imp);
      }
    }
    deleteOperation.run(monitor, workingCopyManager);
  }

  protected void deleteGetterMethodsAndImportsRec(IType type, JavaElementDeleteOperation deleteOperation, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    IPrimaryTypeTypeHierarchy columnHierarchy = TypeUtility.getPrimaryTypeHierarchy(iColumn);
    ITypeHierarchy localHierarchy = columnHierarchy.combinedTypeHierarchy(type.getCompilationUnit());
    for (IType innerType : type.getTypes()) {
      deleteGetterMethodsAndImportsRec(innerType, deleteOperation, monitor, manager);
    }
    IMethod getter = null;
    if (localHierarchy.isSubtype(iColumn, type)) {
      getter = ScoutTypeUtility.getColumnGetterMethod(type);
    }
    else {
      getter = ScoutTypeUtility.getFormFieldGetterMethod(type);
    }
    manager.register(type.getCompilationUnit(), monitor);
    if (TypeUtility.exists(getter)) {
      deleteOperation.addMember(getter);
    }
  }

  public IType getBoxType() {
    return m_boxType;
  }

}
