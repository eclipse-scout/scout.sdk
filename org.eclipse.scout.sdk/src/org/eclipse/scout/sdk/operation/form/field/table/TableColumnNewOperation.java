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
package org.eclipse.scout.sdk.operation.form.field.table;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.type.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.method.InnerTypeGetterCreateOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>TableColumnNewOperation</h3> ...
 */
public class TableColumnNewOperation implements IOperation {

  // in members
  private final IType m_declaringType;
  private final String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private boolean m_formatSource;
  // out members
  private IType m_createdColumn;

  public TableColumnNewOperation(String columnName, IType declaringType) {
    this(columnName, declaringType, true);
  }

  public TableColumnNewOperation(String columnName, IType declaringType, boolean formatSource) {
    m_typeName = columnName;
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    // default values
    m_superTypeSignature = RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IColumn, getDeclaringType().getJavaProject());
  }

  @Override
  public String getOperationName() {
    return "New column '" + getTypeName() + "'...";
  }

  @Override
  public void validate() {
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name is null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    OrderedInnerTypeNewOperation columnOp = new OrderedInnerTypeNewOperation(getTypeName(), getDeclaringType(), isFormatSource());
    columnOp.setOrderDefinitionType(TypeUtility.getType(IRuntimeClasses.IColumn));
    columnOp.setSibling(getSibling());
    columnOp.setSuperTypeSignature(getSuperTypeSignature());
    columnOp.setFlags(Flags.AccPublic);
    // getConfiguredLabel method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(columnOp.getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_HEADER_TEXT);
      nlsMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      columnOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsMethodBuilder), nlsMethodBuilder);
    }
    appendToColumnBuilder(columnOp.getSourceBuilder(), monitor, workingCopyManager);

    columnOp.validate();
    columnOp.run(monitor, workingCopyManager);
    m_createdColumn = columnOp.getCreatedType();
    // getter on declaring table

    InnerTypeGetterCreateOperation getterOp = new InnerTypeGetterCreateOperation(getCreatedColumn(), getDeclaringType(), true);
    getterOp.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return getColumnSet().getColumnByClass(");
        source.append(SignatureUtility.getTypeReference(SignatureCache.createTypeSignature(getCreatedColumn().getFullyQualifiedName()), getDeclaringType(), validator) + ".class");
        source.append(");");
      }
    });
    IStructuredType structuredType = ScoutTypeUtility.createStructuredTable(getDeclaringType());
    getterOp.setSibling(structuredType.getSiblingMethodFieldGetter(getterOp.getElementName()));
    getterOp.validate();
    getterOp.run(monitor, workingCopyManager);
  }

  protected void appendToColumnBuilder(ITypeSourceBuilder columnBuilder, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {

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
