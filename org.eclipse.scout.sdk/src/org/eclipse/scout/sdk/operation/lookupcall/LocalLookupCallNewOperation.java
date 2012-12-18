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
package org.eclipse.scout.sdk.operation.lookupcall;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.field.FieldCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public class LocalLookupCallNewOperation implements IOperation {
  // in members
  private String m_lookupCallName;
  private String m_lookupCallSuperTypeSignature;
  private IScoutBundle m_clientBundle;
  private boolean m_formatSource;
  private String m_packageName;

  //out members
  private IType m_outLookupCall;

  @Override
  public String getOperationName() {
    return "New Local LookupCall '" + getLookupCallName() + "'";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {

    // lookup call
    ScoutTypeNewOperation lookupCallOp = new ScoutTypeNewOperation(getLookupCallName(), getPackageName(), getBundle());
    lookupCallOp.setSuperTypeSignature(getLookupCallSuperTypeSignature());
    lookupCallOp.validate();
    lookupCallOp.run(monitor, workingCopyManager);
    m_outLookupCall = lookupCallOp.getCreatedType();

    FieldCreateOperation serialVersionUidOp = new FieldCreateOperation(getOutLookupCall(), "serialVersionUID", false);
    serialVersionUidOp.setFlags(Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    serialVersionUidOp.setSignature(Signature.SIG_LONG);
    serialVersionUidOp.setSimpleInitValue("1L");
    serialVersionUidOp.validate();
    serialVersionUidOp.run(monitor, workingCopyManager);

    MethodOverrideOperation execCreateLookupRowsMethodOp = new MethodOverrideOperation(m_outLookupCall, "execCreateLookupRows", false) {
      @Override
      protected String createMethodBody(IImportValidator validator) throws JavaModelException {
        String refLookupRow = validator.getTypeName(SignatureCache.createTypeSignature(RuntimeClasses.LookupRow));
        String refArrayList = validator.getTypeName(SignatureCache.createTypeSignature(ArrayList.class.getName()));
        StringBuilder body = new StringBuilder();
        body.append(refArrayList + "<" + refLookupRow + "> rows = new " + refArrayList + "<" + refLookupRow + ">();\n");
        body.append("  " + ScoutUtility.getCommentBlock("create lookup rows here.") + "\n");
        body.append("  return rows;");
        return body.toString();
      }
    };
    execCreateLookupRowsMethodOp.validate();
    execCreateLookupRowsMethodOp.run(monitor, workingCopyManager);

    if (isFormatSource()) {
      // format
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getOutLookupCall(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getPackageName())) {
      throw new IllegalArgumentException("package can not be null or empty.");
    }
  }

  public String getLookupCallName() {
    return m_lookupCallName;
  }

  public void setLookupCallName(String lookupCallName) {
    m_lookupCallName = lookupCallName;
  }

  public void setLookupCallSuperTypeSignature(String lookupCallSuperTypeSignature) {
    m_lookupCallSuperTypeSignature = lookupCallSuperTypeSignature;
  }

  public String getLookupCallSuperTypeSignature() {
    return m_lookupCallSuperTypeSignature;
  }

  public IScoutBundle getBundle() {
    return m_clientBundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_clientBundle = bundle;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public IType getOutLookupCall() {
    return m_outLookupCall;
  }

  public String getPackageName() {
    return m_packageName;
  }

  public void setPackageName(String packageName) {
    m_packageName = packageName;
  }
}
