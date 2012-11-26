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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.util.OrderedInnerTypeNewOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>KeyStrokeNewOperation</h3> ...
 */
public class KeyStrokeNewOperation implements IOperation {

  final IType iKeyStroke = TypeUtility.getType(RuntimeClasses.IKeyStroke);
  // in members
  private final IType m_declaringType;
  private String m_typeName;
  private String m_superTypeSignature;
  private String m_keyStroke;
  private IJavaElement m_sibling;
  private boolean m_formatSource;
  // out members
  private IType m_createdKeyStroke;

  public KeyStrokeNewOperation(IType declaringType) {
    this(declaringType, false);
  }

  public KeyStrokeNewOperation(IType declaringType, boolean formatSource) {
    m_declaringType = declaringType;
    m_formatSource = formatSource;
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
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    OrderedInnerTypeNewOperation keyStrokeOp = new OrderedInnerTypeNewOperation(getTypeName(), getDeclaringType(), false);
    keyStrokeOp.setOrderDefinitionType(iKeyStroke);
    keyStrokeOp.setSibling(getSibling());
    keyStrokeOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IKeyStroke, getDeclaringType().getJavaProject()));
    keyStrokeOp.setTypeModifiers(Flags.AccPublic);
    keyStrokeOp.validate();
    keyStrokeOp.run(monitor, workingCopyManager);
    m_createdKeyStroke = keyStrokeOp.getCreatedType();
    if (!StringUtility.isNullOrEmpty(getKeyStroke())) {
      MethodOverrideOperation confKeyStrokeOp = new MethodOverrideOperation(getCreatedKeyStroke(), "getConfiguredKeyStroke", false);
      confKeyStrokeOp.setSimpleBody("return \"" + getKeyStroke() + "\";");
      confKeyStrokeOp.validate();
      confKeyStrokeOp.run(monitor, workingCopyManager);
    }
    if (m_formatSource) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedKeyStroke(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }

  }

  @Override
  public String getOperationName() {
    return "New key stroke";
  }

  public IType getCreatedKeyStroke() {
    return m_createdKeyStroke;
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

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public void setKeyStroke(String keyStroke) {
    m_keyStroke = keyStroke;
  }

  public String getKeyStroke() {
    return m_keyStroke;
  }

  public IJavaElement getSibling() {
    return m_sibling;
  }

  public void setSibling(IJavaElement sibling) {
    m_sibling = sibling;
  }
}
