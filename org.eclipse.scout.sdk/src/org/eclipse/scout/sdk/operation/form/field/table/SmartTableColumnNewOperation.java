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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>MenuNewOperation</h3>
 */
public class SmartTableColumnNewOperation extends TableColumnNewOperation {

  private IType m_lookupCall;
  private IType m_codeType;

  public SmartTableColumnNewOperation(String columnName, IType declaringType) {
    this(columnName, declaringType, false);
  }

  public SmartTableColumnNewOperation(String columnName, IType declaringType, boolean formatSource) {
    super(columnName, declaringType, formatSource);
  }

  @Override
  public String getOperationName() {
    return "new smart table column...";
  }

  @Override
  protected void appendToColumnBuilder(ITypeSourceBuilder columnBuilder, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (getCodeType() != null) {
      // getConfiguredCodeType method
      IMethodSourceBuilder getConfiguredCodeTypeBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(columnBuilder, "getConfiguredCodeType");
      getConfiguredCodeTypeBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("return ").append(validator.getTypeName(SignatureCache.createTypeSignature(getCodeType().getFullyQualifiedName()))).append(".class;");
        }
      });
      columnBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredCodeTypeBuilder), getConfiguredCodeTypeBuilder);
    }
    if (TypeUtility.exists(getLookupCall())) {
      // getConfiguredLookupCall method
      IMethodSourceBuilder getConfiguredLookupCallBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(columnBuilder, "getConfiguredLookupCall");
      getConfiguredLookupCallBuilder.setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {

        @Override
        public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
          source.append("return ").append(validator.getTypeName(SignatureCache.createTypeSignature(getLookupCall().getFullyQualifiedName()))).append(".class;");
        }
      });
      columnBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredLookupCallBuilder), getConfiguredLookupCallBuilder);
    }
  }

  public IType getLookupCall() {
    return m_lookupCall;
  }

  public void setLookupCall(IType lookupCall) {
    m_lookupCall = lookupCall;
  }

  public IType getCodeType() {
    return m_codeType;
  }

  public void setCodeType(IType codeType) {
    m_codeType = codeType;
  }

}
