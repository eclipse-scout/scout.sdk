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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.annotation.OrderAnnotationsUpdateOperation;
import org.eclipse.scout.sdk.operation.method.FieldGetterCreateOperation;
import org.eclipse.scout.sdk.operation.util.InnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

/**
 *
 */
public class FormFieldNewOperation implements IOperation {

  private final IType iFormField = ScoutSdk.getType(RuntimeClasses.IFormField);

  // in members
  private final IType m_declaringType;
  private boolean m_formatSource;
  private String m_typeName;
  private String m_superTypeSignature;
  private double m_orderNr;
  private IJavaElement m_siblingField;
  private boolean m_createFormFieldGetterMethod;
  // out members
  private IType m_createdFormField;
  private IMethod m_createdFieldGetterMethod;

  public FormFieldNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public FormFieldNewOperation(IType declaringType, boolean formatSource) {
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    // default
    m_createFormFieldGetterMethod = true;
  }

  @Override
  public String getOperationName() {
    return "Create new formFild '" + getTypeName() + "'.";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name can not be null or empty.");
    }
    if (StringUtility.isNullOrEmpty(getSuperTypeSignature())) {
      throw new IllegalArgumentException("super type signature can not be null.");
    }

  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException, IllegalArgumentException {
    manager.register(getDeclaringType().getCompilationUnit(), monitor);
    updateOrderNumbers(monitor, manager);
    InnerTypeNewOperation createInnerTypeOp = new InnerTypeNewOperation(getTypeName(), getDeclaringType());
    // sibling
    createInnerTypeOp.setSibling(getSiblingField());
    createInnerTypeOp.setSuperTypeSignature(getSuperTypeSignature());
    AnnotationCreateOperation orderAnnotation = new AnnotationCreateOperation(null, Signature.createTypeSignature(RuntimeClasses.Order, true));
    orderAnnotation.addParameter("" + getOrderNr());
    createInnerTypeOp.addAnnotation(orderAnnotation);
    createInnerTypeOp.validate();
    createInnerTypeOp.run(monitor, manager);
    m_createdFormField = createInnerTypeOp.getCreatedType();
    if (isCreateFormFieldGetterMethod()) {
      createFormFieldGetter(monitor, manager);
    }
    if (isFormatSource()) {
      // format
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedFormField(), true);
      formatOp.validate();
      formatOp.run(monitor, manager);
    }
  }

  protected void updateOrderNumbers(IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws IllegalArgumentException, CoreException {
    m_orderNr = -1.0;
    IPrimaryTypeTypeHierarchy primaryHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iFormField);

    ITypeHierarchy localHierarchy = primaryHierarchy.combinedTypeHierarchy(getDeclaringType());
    IType[] innerTypes = localHierarchy.getAllSubtypes(iFormField, TypeFilters.getInnerClasses(getDeclaringType()), TypeComparators.getOrderAnnotationComparator());
    OrderAnnotationsUpdateOperation orderAnnotationOp = new OrderAnnotationsUpdateOperation(getDeclaringType());
    double tempOrderNr = 10.0;
    for (IType innerType : innerTypes) {
      if (innerType.equals(getSiblingField())) {
        m_orderNr = tempOrderNr;
        tempOrderNr += 10.0;
      }
      orderAnnotationOp.addOrderAnnotation(innerType, tempOrderNr);
      tempOrderNr += 10.0;
    }
    if (m_orderNr < 0) {
      m_orderNr = tempOrderNr;
    }
    orderAnnotationOp.validate();
    orderAnnotationOp.run(monitor, manager);
  }

  protected void createFormFieldGetter(IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws IllegalArgumentException, CoreException {
    // find form
    ITypeHierarchy hierarchy = ScoutSdk.getLocalTypeHierarchy(getDeclaringType().getCompilationUnit());
    IType form = TypeUtility.getAncestor(getCreatedFormField(), TypeFilters.getMultiTypeFilterOr(
        TypeFilters.getSubtypeFilter(ScoutSdk.getType(RuntimeClasses.IForm), hierarchy),
        TypeFilters.getToplevelTypeFilter()));

    if (TypeUtility.exists(form)) {
      FieldGetterCreateOperation getterMethodOp = new FieldGetterCreateOperation(getCreatedFormField(), form, true);
      IStructuredType sourceHelper = SdkTypeUtility.createStructuredForm(form);
      IJavaElement sibling = sourceHelper.getSiblingMethodFieldGetter("get" + getTypeName());
      if (sibling == null && getCreatedFormField().getDeclaringType().equals(form)) {
        sibling = getCreatedFormField();
      }
      getterMethodOp.setSibling(sibling);
      getterMethodOp.validate();
      getterMethodOp.run(monitor, manager);
      m_createdFieldGetterMethod = getterMethodOp.getCreatedMethod();
    }
  }

  public IType getCreatedFormField() {
    return m_createdFormField;
  }

  public IMethod getCreatedFieldGetterMethod() {
    return m_createdFieldGetterMethod;
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

  public void setTypeName(String typeName) {
    m_typeName = typeName;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSiblingField(IJavaElement sibling) {
    m_siblingField = sibling;
  }

  public IJavaElement getSiblingField() {
    return m_siblingField;
  }

  public void setCreateFormFieldGetterMethod(boolean createFormFieldGetterMethod) {
    m_createFormFieldGetterMethod = createFormFieldGetterMethod;
  }

  public boolean isCreateFormFieldGetterMethod() {
    return m_createFormFieldGetterMethod;
  }

  public double getOrderNr() {
    return m_orderNr;
  }
}
