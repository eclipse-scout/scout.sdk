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
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.field.FieldCreateOperation;
import org.eclipse.scout.sdk.operation.method.ConstructorCreateOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.ScoutTypeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class CodeTypeNewOperation implements IOperation {

  private String m_nextCodeId;
  private String m_typeName;
  private String m_superTypeSignature;
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
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    ScoutTypeNewOperation newOp = new ScoutTypeNewOperation(getTypeName(), getSharedBundle().getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_CODE), getSharedBundle());
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

    // XXX handle id behaviour unboxing!!! Long -> long
    String genericTypeSimpleName = "";
    String unboxedFieldName = "";
    if (!StringUtility.isNullOrEmpty(getSuperTypeSignature())) {
      String[] typeArguments = Signature.getTypeArguments(getSuperTypeSignature());
      if (typeArguments != null && typeArguments.length > 0) {
        genericTypeSimpleName = Signature.getSignatureSimpleName(typeArguments[0]);
        if (Signature.createTypeSignature(Long.class.getName(), true).equals(typeArguments[0])) {
          unboxedFieldName = "long";
        }
      }
    }

    String codeId = getNextCodeId();
    final String todo = (StringUtility.isNullOrEmpty(codeId)) ? ("//TODO") : ("");
    if (StringUtility.isNullOrEmpty(codeId)) {
      codeId = "0L";
    }
    FieldCreateOperation idOp = new FieldCreateOperation(getCreatedType(), "ID", false) {
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

    // constructor
    ConstructorCreateOperation constructorOp = new ConstructorCreateOperation(getCreatedType(), false);
    constructorOp.setExceptionSignatures(new String[]{Signature.createTypeSignature(RuntimeClasses.ProcessingException, true)});
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
}
