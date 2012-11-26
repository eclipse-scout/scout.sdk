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
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.method.InnerTypeGetterCreateOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>MenuNewOperation</h3> ...
 */
public class TableColumnNewOperation implements IOperation {
  final IType iColumn = TypeUtility.getType(RuntimeClasses.IColumn);

  // in members
  private final IType m_declaringType;
  private String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private boolean m_formatSource;
  // out members
  private IType m_createdColumn;

  public TableColumnNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public TableColumnNewOperation(IType declaringType, boolean formatSource) {
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    // default values
    m_superTypeSignature = RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IColumn, getDeclaringType().getJavaProject());
  }

  @Override
  public String getOperationName() {
    return "New column '" + getTypeName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name is null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    OrderedInnerTypeNewOperation columnOp = new OrderedInnerTypeNewOperation(getTypeName(), getDeclaringType(), false);
    columnOp.setOrderDefinitionType(iColumn);
    columnOp.setSibling(getSibling());
    columnOp.setSuperTypeSignature(getSuperTypeSignature());
    columnOp.setTypeModifiers(Flags.AccPublic);
    columnOp.validate();
    columnOp.run(monitor, workingCopyManager);
    m_createdColumn = columnOp.getCreatedType();
    // getter on declaring table

    InnerTypeGetterCreateOperation getterOp = new InnerTypeGetterCreateOperation(getCreatedColumn(), getDeclaringType(), true) {
      @Override
      protected String createMethodBody(IImportValidator validator) throws JavaModelException {
        StringBuilder source = new StringBuilder();
        source.append("return getColumnSet().getColumnByClass(");
        source.append(SignatureUtility.getTypeReference(SignatureCache.createTypeSignature(getField().getFullyQualifiedName()), getDeclaringType(), validator) + ".class");
        source.append(");");
        return source.toString();
      }
    };
    IStructuredType structuredType = ScoutTypeUtility.createStructuredTable(getDeclaringType());
    getterOp.setSibling(structuredType.getSiblingMethodFieldGetter(getterOp.getMethodName()));
    getterOp.validate();
    getterOp.run(monitor, workingCopyManager);
    // nls entry
    if (getNlsEntry() != null) {
      // text
      NlsTextMethodUpdateOperation nlsOp = new NlsTextMethodUpdateOperation(getCreatedColumn(), NlsTextMethodUpdateOperation.GET_CONFIGURED_HEADER_TEXT, false);
      nlsOp.setNlsEntry(getNlsEntry());
      nlsOp.validate();
      nlsOp.run(monitor, workingCopyManager);
    }
    if (m_formatSource) {
      JavaElementFormatOperation foramtOp = new JavaElementFormatOperation(getCreatedColumn(), true);
      foramtOp.validate();
      foramtOp.run(monitor, workingCopyManager);
    }
  }

  public IType getCreatedColumn() {
    return m_createdColumn;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public IJavaElement getSibling() {
    return m_sibling;
  }

  public void setSibling(IJavaElement sibling) {
    m_sibling = sibling;
  }

}
