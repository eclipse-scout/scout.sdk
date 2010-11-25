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
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.TypeDeleteOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.util.ScoutSourceUtilities;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

public class BoxDeleteOperation implements IOperation {

  private final IType iColumn = ScoutSdk.getType(RuntimeClasses.IColumn);
  private final IType m_boxType;
  private String m_name;

  public BoxDeleteOperation(IType boxType) {
    m_boxType = boxType;
    m_name = Texts.get("Action_deleteTypeX", ScoutSourceUtilities.getTranslatedMethodStringValue(boxType, "getConfiguredLabel"));
  }

  public String getOperationName() {
    return m_name;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getBoxType() == null) {
      throw new IllegalArgumentException("type to delete is null.");
    }
  }

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    deleteGetterMethodsAndImportsRec(getBoxType(), monitor, workingCopyManager);
    TypeDeleteOperation op = new TypeDeleteOperation(getBoxType());
    op.run(monitor, workingCopyManager);
  }

  protected void deleteGetterMethodsAndImportsRec(IType type, IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
    IPrimaryTypeTypeHierarchy columnHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iColumn);
    ITypeHierarchy localHierarchy = columnHierarchy.combinedTypeHierarchy(type.getCompilationUnit());
    for (IType innerType : type.getTypes()) {
      deleteGetterMethodsAndImportsRec(innerType, monitor, manager);
    }
    IMethod getter = null;
    if (localHierarchy.isSubtype(iColumn, type)) {
      getter = SdkTypeUtility.getColumnGetterMethod(type);
    }
    else {
      getter = SdkTypeUtility.getFormFieldGetterMethod(type);
    }
    manager.register(type.getCompilationUnit(), false, monitor);
    if (TypeUtility.exists(getter)) {
      getter.delete(true, monitor);
    }
    // import
    IImportDeclaration importDec = type.getCompilationUnit().getImport(type.getFullyQualifiedName().replaceAll("\\$", "."));
    if (importDec != null && importDec.exists()) {
      importDec.delete(true, monitor);
    }
  }

  public IType getBoxType() {
    return m_boxType;
  }

}
