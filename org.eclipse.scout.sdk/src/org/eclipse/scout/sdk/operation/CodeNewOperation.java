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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.field.FieldCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.util.ScoutUtility;

/**
 * <h3>CodeNewOperation</h3> ...
 */
public class CodeNewOperation implements IOperation {

  final IType iCode = ScoutSdk.getType(RuntimeClasses.ICode);

  // in members
  private final IType m_declaringType;
  private String m_nextCodeId;
  private String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private boolean m_formatSource;
  // out members
  private IType m_createdCode;

  public CodeNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public CodeNewOperation(IType declaringType, boolean formatSource) {
    m_declaringType = declaringType;
    m_formatSource = formatSource;
  }

  public String getOperationName() {
    return "Create Code '" + getTypeName() + "'...";
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

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    OrderedInnerTypeNewOperation codeOp = new OrderedInnerTypeNewOperation(getTypeName(), getDeclaringType());
    codeOp.setFormatSource(false);
    codeOp.setSibling(getSibling());
    codeOp.setOrderDefinitionType(iCode);
    codeOp.setSuperTypeSignature(getSuperTypeSignature());
    codeOp.setTypeModifiers(Flags.AccPublic);
    codeOp.validate();
    codeOp.run(monitor, workingCopyManager);
    m_createdCode = codeOp.getCreatedType();

    FieldCreateOperation versionUidOp = new FieldCreateOperation(getCreatedCode(), "serialVersionUID", false);
    versionUidOp.setFlags(Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    versionUidOp.setSignature(Signature.SIG_LONG);
    versionUidOp.setSimpleInitValue("1L");
    versionUidOp.validate();
    versionUidOp.run(monitor, workingCopyManager);

    IJavaElement nlsMethodSibling = null;
    String[] typeArguments = Signature.getTypeArguments(getSuperTypeSignature());
    if (typeArguments != null && typeArguments.length > 0) {
      String longSignature = Signature.createTypeSignature(Long.class.getName(), true);
      // is Long
      if (longSignature.equals(typeArguments[0])) {
        String codeId = getNextCodeId();
        final String todo = (StringUtility.isNullOrEmpty(codeId)) ? (ScoutUtility.getCommentBlock("create code id.")) : ("");
        if (StringUtility.isNullOrEmpty(codeId)) {
          codeId = "0L";
        }
        FieldCreateOperation idOp = new FieldCreateOperation(getCreatedCode(), "ID", false) {
          @Override
          public void buildSource(StringBuilder builder, IImportValidator validator) throws JavaModelException {
            super.buildSource(builder, validator);
            builder.append(todo);
          }
        };

        idOp.setFlags(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
        idOp.setSignature(Signature.SIG_LONG);
        idOp.setSimpleInitValue("" + codeId);
        idOp.validate();
        idOp.run(monitor, workingCopyManager);
        MethodOverrideOperation getIdOp = new MethodOverrideOperation(getCreatedCode(), "getId", false);
        getIdOp.setSimpleBody("return ID;");
        getIdOp.validate();
        getIdOp.run(monitor, workingCopyManager);
        nlsMethodSibling = getIdOp.getCreatedMethod();
      }

    }
    if (getNlsEntry() != null) {
      final IJavaElement finalSibing = nlsMethodSibling;
      NlsTextMethodUpdateOperation confTextOp = new NlsTextMethodUpdateOperation(getCreatedCode(), NlsTextMethodUpdateOperation.GET_CONFIGURED_TEXT, false) {
        @Override
        protected IJavaElement computeSibling() {
          return finalSibing;
        }
      };
      confTextOp.setNlsEntry(getNlsEntry());
      confTextOp.validate();
      confTextOp.run(monitor, workingCopyManager);
    }

    if (isFormatSource()) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedCode(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  public IType getCreatedCode() {
    return m_createdCode;
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

  public void setNextCodeId(String nextCodeId) {
    m_nextCodeId = nextCodeId;
  }

  public String getNextCodeId() {
    return m_nextCodeId;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public void setSibling(IJavaElement sibling) {
    m_sibling = sibling;
  }

  public IJavaElement getSibling() {
    return m_sibling;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }
}
