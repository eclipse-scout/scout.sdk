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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>MenuNewOperation</h3> ...
 */
public class SmartTableColumnNewOperation extends TableColumnNewOperation {

  private IType m_lookupCall;
  private IType m_codeType;
  private boolean m_localFormatSource;

  public SmartTableColumnNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public SmartTableColumnNewOperation(IType declaringType, boolean formatSource) {
    super(declaringType, false);
    m_localFormatSource = formatSource;
  }

  @Override
  public String getOperationName() {
    return "new smart table column...";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    if (getCodeType() != null) {
      MethodOverrideOperation codeTypeOp = new MethodOverrideOperation(getCreatedColumn(), "getConfiguredCodeType", false) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          StringBuilder sourceBuilder = new StringBuilder();
          String codeTypeRef = validator.getTypeName(Signature.createTypeSignature(getCodeType().getFullyQualifiedName(), true));
          sourceBuilder.append("return " + codeTypeRef + ".class;\n");
          return sourceBuilder.toString();
        }
      };
      codeTypeOp.validate();
      codeTypeOp.run(monitor, workingCopyManager);
    }
    if (TypeUtility.exists(getLookupCall())) {
      MethodOverrideOperation lookupCallOp = new MethodOverrideOperation(getCreatedColumn(), "getConfiguredLookupCall", false) {
        @Override
        protected String createMethodBody(IImportValidator validator) throws JavaModelException {
          StringBuilder sourceBuilder = new StringBuilder();
          String lookupCallRef = validator.getTypeName(Signature.createTypeSignature(getLookupCall().getFullyQualifiedName(), true));
          sourceBuilder.append("return " + lookupCallRef + ".class;\n");
          return sourceBuilder.toString();
        }
      };
      lookupCallOp.validate();
      lookupCallOp.run(monitor, workingCopyManager);
    }

    if (m_localFormatSource) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedColumn(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  @Override
  public void setFormatSource(boolean formatSource) {
    m_localFormatSource = formatSource;
  }

  @Override
  public boolean isFormatSource() {
    return m_localFormatSource;
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
