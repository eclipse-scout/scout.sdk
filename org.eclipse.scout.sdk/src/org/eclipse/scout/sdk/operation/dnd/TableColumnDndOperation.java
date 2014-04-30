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
package org.eclipse.scout.sdk.operation.dnd;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.operation.method.InnerTypeGetterCreateOperation;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 *
 */
public class TableColumnDndOperation extends AbstractTypeDndOperation {

  /**
   * @param type
   * @param targetDeclaringType
   * @param typeCategory
   */
  public TableColumnDndOperation(IType type, IType targetDeclaringType, String newName, int mode) {
    super(type, targetDeclaringType, newName, CATEGORIES.TYPE_COLUMN, mode);
  }

  @Override
  protected IType createNewType(IType declaringType, String simpleName, String source, String[] fqImports, IJavaElement sibling, IStructuredType structuredType, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    final IType newColumn = super.createNewType(declaringType, simpleName, source, fqImports, sibling, structuredType, monitor, manager);

    InnerTypeGetterCreateOperation getterOp = new InnerTypeGetterCreateOperation(newColumn, declaringType, false);
    getterOp.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder sourceBuilder, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        sourceBuilder.append("return getColumnSet().getColumnByClass(").append(validator.getTypeName(SignatureCache.createTypeSignature(newColumn.getFullyQualifiedName()))).append(".class);");
      }
    });
    getterOp.setSibling(structuredType.getSiblingMethodFieldGetter(getterOp.getElementName()));
    getterOp.validate();
    getterOp.run(monitor, manager);

    return newColumn;
  }

  @Override
  protected void deleteType(IType type, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
    delOp.addMember(type);
    IMethod getter = ScoutTypeUtility.getColumnGetterMethod(type);
    if (TypeUtility.exists(getter)) {
      delOp.addMember(getter);
    }
    delOp.run(monitor, workingCopyManager);
  }

}
