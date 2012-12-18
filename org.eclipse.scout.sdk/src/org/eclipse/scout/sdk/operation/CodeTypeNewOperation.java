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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.field.FieldCreateOperation;
import org.eclipse.scout.sdk.operation.method.ConstructorCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class CodeTypeNewOperation implements IOperation {

  private String m_nextCodeId;
  private String m_typeName;
  private String m_packageName;
  private String m_superTypeSignature;
  private String m_genericTypeSignature;
  private INlsEntry m_nlsEntry;
  private IScoutBundle m_sharedBundle;

  private IType m_createdType;
  private boolean m_formatSource;

  @Override
  public String getOperationName() {
    return "New Code Type...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getSharedBundle() == null) {
      throw new IllegalArgumentException("shared bundle can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name can not be null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ScoutTypeNewOperation newOp = new ScoutTypeNewOperation(getTypeName(), getPackageName(), getSharedBundle());
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    newOp.run(monitor, workingCopyManager);
    m_createdType = newOp.getCreatedType();
    workingCopyManager.register(m_createdType.getCompilationUnit(), monitor);

    FieldCreateOperation versionUidOp = new FieldCreateOperation(getCreatedType(), "serialVersionUID", false);
    versionUidOp.setFlags(Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    versionUidOp.setSignature(Signature.SIG_LONG);
    versionUidOp.setSimpleInitValue("1L");
    versionUidOp.validate();
    versionUidOp.run(monitor, workingCopyManager);

    final boolean isCodeIdUndef = StringUtility.isNullOrEmpty(getNextCodeId());
    final String todo = isCodeIdUndef ? ScoutUtility.getCommentBlock("Auto-generated value") : "";
    final String codeId = isCodeIdUndef ? "null" : getNextCodeId();
    FieldCreateOperation idOp = new FieldCreateOperation(getCreatedType(), "ID", false) {
      @Override
      public void buildSource(StringBuilder builder, IImportValidator validator) throws JavaModelException {
        super.buildSource(builder, validator);
        builder.append(todo);
      }
    };

    idOp.setFlags(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    idOp.setSignature(getGenericTypeSignature());
    idOp.setSimpleInitValue(codeId);
    idOp.validate();
    idOp.run(monitor, workingCopyManager);

    // constructor
    ConstructorCreateOperation constructorOp = new ConstructorCreateOperation(getCreatedType(), false);
    constructorOp.addExceptionSignature(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));
    constructorOp.setMethodFlags(Flags.AccPublic);
    constructorOp.setSimpleBody("super();");
    constructorOp.validate();
    constructorOp.run(monitor, workingCopyManager);

    if (getNlsEntry() != null) {
      NlsTextMethodUpdateOperation confTextOp = new NlsTextMethodUpdateOperation(getCreatedType(), NlsTextMethodUpdateOperation.GET_CONFIGURED_TEXT, false);
      confTextOp.setNlsEntry(getNlsEntry());
      confTextOp.validate();
      confTextOp.run(monitor, workingCopyManager);
    }
    // getid method
    MethodOverrideOperation getIdOp = new MethodOverrideOperation(getCreatedType(), "getId", false);
    getIdOp.setSimpleBody("return ID;");
    getIdOp.setReturnTypeSignature(getGenericTypeSignature());
    getIdOp.validate();
    getIdOp.run(monitor, workingCopyManager);

    // add to exported packages
    ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_ADD_WHEN_NOT_EMTPY,
        new IPackageFragment[]{m_createdType.getPackageFragment()}, true);
    manifestOp.run(monitor, workingCopyManager);

    if (isFormatSource()) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedType(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  public IType getCreatedType() {
    return m_createdType;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setSharedBundle(IScoutBundle sharedBundle) {
    m_sharedBundle = sharedBundle;
  }

  public IScoutBundle getSharedBundle() {
    return m_sharedBundle;
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

  public void setNlsEntry(INlsEntry nlsKey) {
    m_nlsEntry = nlsKey;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public void setGenericTypeSignature(String genericTypeSignature) {
    m_genericTypeSignature = genericTypeSignature;
  }

  public String getGenericTypeSignature() {
    return m_genericTypeSignature;
  }

  public String getPackageName() {
    return m_packageName;
  }

  public void setPackageName(String packageName) {
    m_packageName = packageName;
  }
}
