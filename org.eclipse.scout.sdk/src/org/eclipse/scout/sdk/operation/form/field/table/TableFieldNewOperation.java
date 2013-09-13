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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.form.field.FormFieldNewOperationNew;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>TableFieldNewOperation</h3> ...
 */
public class TableFieldNewOperation implements IOperation {

  private final String m_typeName;
  private final IType m_declaringType;
  private boolean m_formatSource;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private String m_tableSuperTypeSignature;
  private IJavaElement m_sibling;
  private IJavaElement m_getterMethodSibling;
  private IType m_formType;
  private double m_orderNr;

  private IType m_createdField;
  private IType m_createdTable;
  private IMethod m_createdFieldGetterMethod;

  public TableFieldNewOperation(String typeName, IType declaringType) {
    this(typeName, declaringType, true);
  }

  public TableFieldNewOperation(String typeName, IType declaringType, boolean formatSource) {
    m_typeName = typeName;
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    // default
    setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ITableField, getDeclaringType().getJavaProject()));
    setTableSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ITable, getDeclaringType().getJavaProject()));
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("typeName is null or empty.");
    }
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    FormFieldNewOperationNew newOp = new FormFieldNewOperationNew(getTypeName(), getDeclaringType());
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    newOp.setSibling(getSibling());

    // getConfiguredLabel method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(newOp.getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
      nlsMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      newOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsMethodBuilder), nlsMethodBuilder);
    }
    String superTypeFqn = SignatureUtility.getFullyQuallifiedName(getSuperTypeSignature());
    // TODO aho check generic type table!!!
    if (CompareUtility.equals(superTypeFqn, RuntimeClasses.AbstractTableField)) {
      // create inner type table
      ITypeSourceBuilder tableBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_TABLEFIELD_TABLE);
      tableBuilder.setFlags(Flags.AccPublic);
      if (getTableSuperTypeSignature() == null) {
        setTableSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ITable, getDeclaringType().getJavaProject()));
      }
      tableBuilder.setSuperTypeSignature(getTableSuperTypeSignature());
      tableBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(10.0));
      newOp.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeTableKey(tableBuilder), tableBuilder);
      // update generic in supertype signature
      StringBuilder superTypeSigBuilder = new StringBuilder(superTypeFqn);
      superTypeSigBuilder.append("<").append(newOp.getElementName()).append(".").append(SdkProperties.TYPE_NAME_TABLEFIELD_TABLE).append(">");
      setSuperTypeSignature(Signature.createTypeSignature(superTypeSigBuilder.toString(), true));
    }

    newOp.setFormatSource(isFormatSource());
    newOp.validate();
    newOp.run(monitor, workingCopyManager);
    m_createdField = newOp.getCreatedType();
  }

  @Override
  public String getOperationName() {
    return "New table field";
  }

  public IType getCreatedField() {
    return m_createdField;
  }

  public IType getCreatedTable() {
    return m_createdTable;
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
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

  public double getOrderNr() {
    return m_orderNr;
  }

  public void setOrderNr(double orderNr) {
    m_orderNr = orderNr;
  }

  public String getTableSuperTypeSignature() {
    return m_tableSuperTypeSignature;
  }

  public void setTableSuperTypeSignature(String tableSuperTypeSignature) {
    m_tableSuperTypeSignature = tableSuperTypeSignature;
  }
}
