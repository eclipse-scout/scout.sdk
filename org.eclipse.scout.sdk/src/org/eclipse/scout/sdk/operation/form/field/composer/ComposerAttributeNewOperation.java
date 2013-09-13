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
package org.eclipse.scout.sdk.operation.form.field.composer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.jdt.type.InnerTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>WizardStepNewOperation</h3> ...
 */
public class ComposerAttributeNewOperation implements IOperation {

  final IType iDataModelAttribute = TypeUtility.getType(RuntimeClasses.IDataModelAttribute);

  // in member
  private final IType m_declaringType;
  private String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private boolean m_formatSource;
  // out member
  private IType m_createdComposerAttribute;

  public ComposerAttributeNewOperation(String attributeName, IType declaringType) {
    this(attributeName, declaringType, false);
  }

  public ComposerAttributeNewOperation(String attributeName, IType declaringType, boolean formatSource) {
    m_typeName = attributeName;
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    // default values
    m_superTypeSignature = RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IDataModelAttribute, getDeclaringType().getJavaProject());
  }

  @Override
  public String getOperationName() {
    return "New composer attribute...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!TypeUtility.exists(getDeclaringType())) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name is null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    InnerTypeNewOperation typeNewOp = new InnerTypeNewOperation(getTypeName(), getDeclaringType());
    typeNewOp.setFormatSource(true);
    typeNewOp.setSibling(getSibling());
    typeNewOp.setSuperTypeSignature(getSuperTypeSignature());
    typeNewOp.setFlags(Flags.AccPublic);
    // serial version uid
    typeNewOp.addFieldSourceBuilder(FieldSourceBuilderFactory.createSerialVersionUidBuilder());
    // nls method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsTextMethod = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(typeNewOp.getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TEXT);
      nlsTextMethod.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      typeNewOp.addMethodSourceBuilder(nlsTextMethod);
    }
    typeNewOp.validate();
    typeNewOp.run(monitor, workingCopyManager);
    m_createdComposerAttribute = typeNewOp.getCreatedType();

    if (m_formatSource) {
      JavaElementFormatOperation foramtOp = new JavaElementFormatOperation(getCreatedAttribute(), true);
      foramtOp.validate();
      foramtOp.run(monitor, workingCopyManager);
    }
  }

  public IType getCreatedAttribute() {
    return m_createdComposerAttribute;
  }

  public IType getDeclaringType() {
    return m_declaringType;
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

  public IJavaElement getSibling() {
    return m_sibling;
  }

  public void setSibling(IJavaElement sibling) {
    m_sibling = sibling;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

}
