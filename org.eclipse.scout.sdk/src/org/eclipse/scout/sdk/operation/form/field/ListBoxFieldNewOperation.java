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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>SmartFieldNewOperation</h3> ...
 */
public class ListBoxFieldNewOperation implements IOperation {

  private final String m_typeName;
  private final IType m_declaringType;
  private boolean m_formatSource;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IType m_codeType;
  private IType m_lookupCall;
  private IJavaElement m_sibling;
  private IType m_createdField;

  public ListBoxFieldNewOperation(String typeName, IType declaringType) {
    this(typeName, declaringType, true);
  }

  public ListBoxFieldNewOperation(String typeName, IType declaringType, boolean formatSource) {
    m_typeName = typeName;
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    // default
    setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.getSuperTypeName(IRuntimeClasses.IListBox, getDeclaringType().getJavaProject()) + "<" + Long.class.getName() + ">"));
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
    FormFieldNewOperation newOp = new FormFieldNewOperation(getTypeName(), getDeclaringType());
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    newOp.setSibling(getSibling());

    // getConfiguredLabel method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(newOp.getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
      nlsMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      newOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsMethodBuilder), nlsMethodBuilder);
    }

    if (getCodeType() != null) {
      // code type
      IMethodSourceBuilder getConfiguredCodeTypeBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(newOp.getSourceBuilder(), "getConfiguredCodeType");
      getConfiguredCodeTypeBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("return ");
          source.append(validator.getTypeName(SignatureCache.createTypeSignature(getCodeType().getFullyQualifiedName())));
          source.append(".class;");
        }
      });
      newOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredCodeTypeBuilder), getConfiguredCodeTypeBuilder);
    }
    else if (getLookupCall() != null) {
      // lookup call
      IMethodSourceBuilder getConfiguredLookupCallBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(newOp.getSourceBuilder(), "getConfiguredLookupCall");
      getConfiguredLookupCallBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("return ");
          source.append(validator.getTypeName(SignatureCache.createTypeSignature(getLookupCall().getFullyQualifiedName())));
          source.append(".class;");
        }
      });
      newOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredLookupCallBuilder), getConfiguredLookupCallBuilder);
    }

    newOp.setFormatSource(isFormatSource());
    newOp.validate();
    newOp.run(monitor, workingCopyManager);
    m_createdField = newOp.getCreatedType();
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

  public IType getLookupCall() {
    return m_lookupCall;
  }

  public void setLookupCall(IType lookupCall) {
    m_lookupCall = lookupCall;
  }
}
