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
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * <h3>FormFieldDeleteOperation</h3> The operation to delete a inner class (form field) and its getter on the
 * top level type.
 */
public class FormFieldDeleteOperation implements IOperation {

  private final IType m_formFieldType;
  private final boolean m_formatSource;

  public FormFieldDeleteOperation(IType formFieldType, boolean formatSource) {
    m_formFieldType = formFieldType;
    m_formatSource = formatSource;
  }

  @Override
  public String getOperationName() {
    return "Delete field '" + getFormFieldType().getElementName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getFormFieldType() == null) {
      throw new IllegalArgumentException("type to delete is null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    workingCopyManager.register(getFormFieldType().getCompilationUnit(), true, monitor);
    IType declaringType = getFormFieldType().getDeclaringType();

    JavaElementDeleteOperation op = new JavaElementDeleteOperation();
    // getter method
    IMethod getter = SdkTypeUtility.getFormFieldGetterMethod(getFormFieldType());
    if (TypeUtility.exists(getter)) {
      op.addMember(getter);
    }
    // import
    String normalizedTypeName = getFormFieldType().getFullyQualifiedName().replace('$', '.');
    for (IImportDeclaration imp : getFormFieldType().getCompilationUnit().getImports()) {
      String normalizedImport = imp.getElementName().replace('$', '.');
      if (normalizedImport.startsWith(normalizedTypeName)) {
        op.addMember(imp);
      }
    }
    // field
    op.addMember(getFormFieldType());
    op.run(monitor, workingCopyManager);
    // form field
    if (TypeUtility.exists(declaringType) && isFormatSource()) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(declaringType, true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  public IType getFormFieldType() {
    return m_formFieldType;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }
}
