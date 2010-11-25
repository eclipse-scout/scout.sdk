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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.InnerTypeNewOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * <h3>WizardStepNewOperation</h3> ...
 */
public class ComposerEntityNewOperation implements IOperation {

  final IType iComposerEntity = ScoutSdk.getType(RuntimeClasses.IComposerEntity);

  // in member
  private final IType m_declaringType;
  private String m_typeName;
  private INlsEntry m_nlsEntry;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private boolean m_formatSource;
  // out member
  private IType m_createdComposerEntry;

  public ComposerEntityNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public ComposerEntityNewOperation(IType declaringType, boolean formatSource) {
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    // default values
    m_superTypeSignature = Signature.createTypeSignature(RuntimeClasses.AbstractComposerEntity, true);
  }

  public String getOperationName() {
    return "New composer entry...";
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

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    InnerTypeNewOperation typeNewOp = new InnerTypeNewOperation(getTypeName(), getDeclaringType(), false);
    typeNewOp.setSibling(getSibling());
    typeNewOp.setSuperTypeSignature(getSuperTypeSignature());
    typeNewOp.setTypeModifiers(Flags.AccPublic);
    typeNewOp.validate();
    typeNewOp.run(monitor, workingCopyManager);
    m_createdComposerEntry = typeNewOp.getCreatedType();

    // nls entry
    if (getNlsEntry() != null) {
      // text
      NlsTextMethodUpdateOperation nlsOp = new NlsTextMethodUpdateOperation(getCreatedEntry(), NlsTextMethodUpdateOperation.GET_CONFIGURED_TEXT, false);
      nlsOp.setNlsEntry(getNlsEntry());
      nlsOp.validate();
      nlsOp.run(monitor, workingCopyManager);
    }
    if (m_formatSource) {
      JavaElementFormatOperation foramtOp = new JavaElementFormatOperation(getCreatedEntry(), true);
      foramtOp.validate();
      foramtOp.run(monitor, workingCopyManager);
    }
  }

  public IType getCreatedEntry() {
    return m_createdComposerEntry;
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
