/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.operation.codetype;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.sourcebuilder.codetype.CodeTypeSourceBuilder;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerationContext;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerators;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.uniqueid.UniqueIdExtensionPoint;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link CodeTypeNewOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class CodeTypeNewOperation implements IOperation {

  private final IJavaEnvironmentProvider m_javaEnvironmentProvider;

  // in
  private String m_codeTypeName;
  private IPackageFragmentRoot m_sharedSourceFolder;
  private String m_package;
  private String m_superTypeSignature;
  private String m_codeTypeIdSignature;

  //out
  private IType m_createdCodeType;

  public CodeTypeNewOperation() {
    this(new CachingJavaEnvironmentProvider());
  }

  protected CodeTypeNewOperation(IJavaEnvironmentProvider provider) {
    m_javaEnvironmentProvider = Validate.notNull(provider);
  }

  @Override
  public String getOperationName() {
    return "Create CodeType '" + getCodeTypeName() + "'.";
  }

  @Override
  public void validate() {
    Validate.isTrue(StringUtils.isNotBlank(getCodeTypeName()), "No codetype name provided");
    Validate.isTrue(S2eUtils.exists(getSharedSourceFolder()), "No source folder provided");
    Validate.isTrue(StringUtils.isNotBlank(getPackage()), "No package name provided");
    Validate.isTrue(StringUtils.isNotBlank(getSuperTypeSignature()), "No supertype provided");
    Validate.isTrue(StringUtils.isNotBlank(getCodeTypeIdSignature()), "No codetype id datatype provided");
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 1);
    setCreatedCodeType(createCodeType(progress.newChild(1), workingCopyManager));
  }

  protected IType createCodeType(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    IJavaEnvironment env = getEnvProvider().get(getSharedSourceFolder().getJavaProject());

    CodeTypeSourceBuilder codeTypeBuilder = new CodeTypeSourceBuilder(getCodeTypeName(), getPackage(), env);
    codeTypeBuilder.setSuperTypeSignature(getSuperTypeSignature());
    codeTypeBuilder.setCodeTypeIdSignature(getCodeTypeIdSignature());

    String idValue = UniqueIdExtensionPoint.getNextUniqueId(null, getCodeTypeIdSignature());
    if (StringUtils.isBlank(idValue)) {
      idValue = CoreUtils.getDefaultValueOf(SignatureUtils.unboxToPrimitiveSignature(getCodeTypeIdSignature()));
    }
    if (StringUtils.isNotBlank(idValue) && !"null".equals(idValue)) {
      codeTypeBuilder.setIdValueBuilder(new RawSourceBuilder(idValue));
    }
    if (ClassIdGenerators.isAutomaticallyCreateClassIdAnnotation()) {
      codeTypeBuilder.setClassIdValue(ClassIdGenerators.generateNewId(new ClassIdGenerationContext(getPackage() + '.' + getCodeTypeName())));
    }

    codeTypeBuilder.setup();

    return S2eUtils.writeType(getSharedSourceFolder(), codeTypeBuilder, env, monitor, workingCopyManager);
  }

  public String getCodeTypeName() {
    return m_codeTypeName;
  }

  public void setCodeTypeName(String codeTypeName) {
    m_codeTypeName = codeTypeName;
  }

  public IPackageFragmentRoot getSharedSourceFolder() {
    return m_sharedSourceFolder;
  }

  public void setSharedSourceFolder(IPackageFragmentRoot sharedSourceFolder) {
    m_sharedSourceFolder = sharedSourceFolder;
  }

  protected void setCreatedCodeType(IType createdCodeType) {
    m_createdCodeType = createdCodeType;
  }

  public IType getCreatedCodeType() {
    return m_createdCodeType;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public String getPackage() {
    return m_package;
  }

  public void setPackage(String package1) {
    m_package = package1;
  }

  public String getCodeTypeIdSignature() {
    return m_codeTypeIdSignature;
  }

  public void setCodeTypeIdSignature(String codeTypeIdSignature) {
    m_codeTypeIdSignature = codeTypeIdSignature;
  }

  protected IJavaEnvironmentProvider getEnvProvider() {
    return m_javaEnvironmentProvider;
  }
}
