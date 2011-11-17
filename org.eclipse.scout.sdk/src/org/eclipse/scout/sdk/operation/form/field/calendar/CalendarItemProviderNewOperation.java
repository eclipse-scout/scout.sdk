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
package org.eclipse.scout.sdk.operation.form.field.calendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.util.InnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>CalendarItemProviderNewOperation</h3> ...
 */
public class CalendarItemProviderNewOperation implements IOperation {

  private final IType m_declaringType;
  private boolean m_formatSource;
  private String m_typeName;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private IType m_createdField;

  public CalendarItemProviderNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public CalendarItemProviderNewOperation(IType declaringType, boolean foramtSource) {
    m_declaringType = declaringType;
    m_formatSource = foramtSource;
  }

  @Override
  public String getOperationName() {
    return "new calendar item provider...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name can not be null. ");
    }
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    InnerTypeNewOperation tableNewOp = new InnerTypeNewOperation(getTypeName(), getDeclaringType(), false);
    tableNewOp.setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractCalendarItemProvider, true));
    tableNewOp.addTypeModifier(Flags.AccPublic);
    AnnotationCreateOperation orderAnnotOp = new AnnotationCreateOperation(null, Signature.createTypeSignature(RuntimeClasses.Order, true));
    orderAnnotOp.addParameter("10.0");
    tableNewOp.addAnnotation(orderAnnotOp);
    tableNewOp.validate();
    tableNewOp.run(monitor, workingCopyManager);
    m_createdField = tableNewOp.getCreatedType();
    if (isFormatSource()) {
      // format
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedField(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  public IType getCreatedField() {
    return m_createdField;
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public void setFormatSource(boolean sourceFormat) {
    m_formatSource = sourceFormat;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
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
