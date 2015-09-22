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
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.template.IContentTemplate;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>SequenceBoxNewOperation</h3>
 */
public class SequenceBoxNewOperation implements IOperation {

  private final String m_typeName;
  private final IType m_declaringType;
  private boolean m_formatSource;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private IType m_createdField;
  private IContentTemplate m_contentTemplate;

  public SequenceBoxNewOperation(String boxName, IType declaringType) {
    this(boxName, declaringType, false);
  }

  public SequenceBoxNewOperation(String boxName, IType declaringType, boolean formatSource) {
    m_typeName = boxName;
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    // default
    setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.ISequenceBox, getDeclaringType().getJavaProject()));
  }

  @Override
  public void validate() {
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("typeName is null or empty.");
    }
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    FormFieldNewOperation newOp = new FormFieldNewOperation(getTypeName(), getDeclaringType());
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    newOp.setSibling(getSibling());
    newOp.setFormatSource(isFormatSource());
    // nls text method
    ITypeSourceBuilder formFieldBuilder = newOp.getSourceBuilder();
    if (getNlsEntry() != null) {
      IMethodSourceBuilder getConfiguredLabelBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(formFieldBuilder, SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
      getConfiguredLabelBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      formFieldBuilder.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredLabelBuilder), getConfiguredLabelBuilder);
    }
    if (getContentTemplate() != null) {
      getContentTemplate().apply(formFieldBuilder, getDeclaringType(), monitor, workingCopyManager);
    }
    newOp.validate();
    newOp.run(monitor, workingCopyManager);
    m_createdField = newOp.getCreatedType();
    if (isFormatSource()) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(m_createdField.getCompilationUnit(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  @Override
  public String getOperationName() {
    return "New Sequence box";
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

  public IJavaElement getSibling() {
    return m_sibling;
  }

  public void setSibling(IJavaElement sibling) {
    m_sibling = sibling;
  }

  public void setContentTemplate(IContentTemplate contentTemplate) {
    m_contentTemplate = contentTemplate;
  }

  public IContentTemplate getContentTemplate() {
    return m_contentTemplate;
  }

}