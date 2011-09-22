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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.method.NlsTextMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * <h3>FormHandlerNewOperation</h3> ...
 */
public class ToolbuttonNewOperation implements IOperation {

  final IType iToolbutton = ScoutSdk.getType(RuntimeClasses.IToolButton);

  private final IType m_declaringType;
  private INlsEntry m_nlsEntry;
  private String m_typeName;
  private String m_superTypeSignature;
  private IJavaElement m_sibling;
  private IType m_createdToolButton;
  private boolean m_formatSource;

  public ToolbuttonNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public ToolbuttonNewOperation(IType declaringType, boolean formatSource) {
    m_declaringType = declaringType;
    m_formatSource = formatSource;
    // default values
    m_superTypeSignature = Signature.createTypeSignature(RuntimeClasses.AbstractToolButton, true);
  }

  @Override
  public String getOperationName() {
    return "new tool button...";
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

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    OrderedInnerTypeNewOperation toolButtonOp = new OrderedInnerTypeNewOperation(getTypeName(), getDeclaringType(), false);
    toolButtonOp.setOrderDefinitionType(iToolbutton);
    toolButtonOp.setSibling(getSibling());
    toolButtonOp.setSuperTypeSignature(getSuperTypeSignature());
    toolButtonOp.setTypeModifiers(Flags.AccPublic);
    toolButtonOp.validate();
    toolButtonOp.run(monitor, workingCopyManager);
    m_createdToolButton = toolButtonOp.getCreatedType();
    if (getNlsEntry() != null) {
      NlsTextMethodUpdateOperation nlsOp = new NlsTextMethodUpdateOperation(getCreatedToolButton(), NlsTextMethodUpdateOperation.GET_CONFIGURED_TEXT, false);
      nlsOp.setNlsEntry(getNlsEntry());
      nlsOp.validate();
      nlsOp.run(monitor, workingCopyManager);
    }
    if (m_formatSource) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedToolButton(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  public IType getCreatedToolButton() {
    return m_createdToolButton;
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
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

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }
}
