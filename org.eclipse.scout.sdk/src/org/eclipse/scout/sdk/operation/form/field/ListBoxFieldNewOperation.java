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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>SmartFieldNewOperation</h3> ...
 */
public class ListBoxFieldNewOperation implements IOperation {

  private final IType m_declaringType;
  private boolean m_formatSource;
  private String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IType m_codeType;
  private IJavaElement m_sibling;
  private IType m_createdField;

  public ListBoxFieldNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public ListBoxFieldNewOperation(IType declaringType, boolean formatSource) {
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    // default
    setSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractListBox + "<" + Long.class.getName() + ">", true));
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("typeName is null or empty.");
    }
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
    String[] typeArguments = Signature.getTypeArguments(getSuperTypeSignature());
    if (typeArguments == null || typeArguments.length < 1) {
      ScoutSdk.logWarning("create a class with a generic super type but no generic type set.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    FormFieldNewOperation newOp = new FormFieldNewOperation(getDeclaringType());
    newOp.setTypeName(getTypeName());
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    newOp.setSiblingField(getSibling());
    newOp.validate();
    newOp.run(monitor, workingCopyManager);
    m_createdField = newOp.getCreatedFormField();
    // nls text
    if (getNlsEntry() != null) {
      NlsTextMethodUpdateOperation labelOp = new NlsTextMethodUpdateOperation(getCreatedField(), NlsTextMethodUpdateOperation.GET_CONFIGURED_LABEL);
      labelOp.setNlsEntry(getNlsEntry());
      labelOp.validate();
      labelOp.run(monitor, workingCopyManager);
    }
    // code type
    if (getCodeType() != null) {
      MethodOverrideOperation codetypeOp = new MethodOverrideOperation(getCreatedField(), "getConfiguredCodeType") {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          StringBuilder source = new StringBuilder();
          source.append("return ");
          source.append(SignatureUtility.getTypeReference(Signature.createTypeSignature(getCodeType().getFullyQualifiedName(), true), validator));
          source.append(".class;");
          return source.toString();
        }
      };
      codetypeOp.validate();
      codetypeOp.run(monitor, workingCopyManager);
    }
    if (isFormatSource()) {
      // format
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedField(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  @Override
  public String getOperationName() {
    return "New list box";
  }

  public IType getCreatedField() {
    return m_createdField;
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

  public IType getCodeType() {
    return m_codeType;
  }

  public void setCodeType(IType codeType) {
    m_codeType = codeType;
  }

  public IJavaElement getSibling() {
    return m_sibling;
  }

  public void setSibling(IJavaElement sibling) {
    m_sibling = sibling;
  }

}
