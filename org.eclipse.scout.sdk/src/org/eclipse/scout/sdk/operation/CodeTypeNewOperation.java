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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
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
    PrimaryTypeNewOperation newOp = new PrimaryTypeNewOperation(getTypeName(), getPackageName(), ScoutUtility.getJavaProject(getSharedBundle()));
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    newOp.setFlags(Flags.AccPublic);
    newOp.setPackageExportPolicy(ExportPolicy.AddPackage);
    // serial version UID
    newOp.addFieldSourceBuilder(FieldSourceBuilderFactory.createSerialVersionUidBuilder());
    // field ID
    FieldSourceBuilder idFieldBuilder = new FieldSourceBuilder("ID") {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        super.createSource(source, lineDelimiter, ownerProject, validator);
        if (StringUtility.isNullOrEmpty(getNextCodeId())) {
          source.append(ScoutUtility.getCommentBlock("Auto-generated value"));
        }
      }

    };

    if (StringUtility.isNullOrEmpty(getNextCodeId())) {
      idFieldBuilder.setValue("null");
    }
    else {
      idFieldBuilder.setValue(getNextCodeId());
    }
    idFieldBuilder.setFlags(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    idFieldBuilder.setSignature(getGenericTypeSignature());
    newOp.addFieldSourceBuilder(idFieldBuilder);
    // constructor
    IMethodSourceBuilder constructorSourceBuilder = MethodSourceBuilderFactory.createConstructorSourceBuilder(getTypeName());
    constructorSourceBuilder.addExceptionSignature(SignatureCache.createTypeSignature(RuntimeClasses.ProcessingException));
    constructorSourceBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("super();"));
    newOp.addMethodSourceBuilder(constructorSourceBuilder);

    // nls
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(newOp.getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TEXT);
      nlsSourceBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      newOp.addMethodSourceBuilder(nlsSourceBuilder);
    }
    // get id method
    IMethodSourceBuilder getIdSourceBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(newOp.getSourceBuilder(), "getId");
    getIdSourceBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return ID;"));
    newOp.addMethodSourceBuilder(getIdSourceBuilder);

    newOp.validate();
    newOp.run(monitor, workingCopyManager);
    m_createdType = newOp.getCreatedType();
    workingCopyManager.register(m_createdType.getCompilationUnit(), monitor);

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
