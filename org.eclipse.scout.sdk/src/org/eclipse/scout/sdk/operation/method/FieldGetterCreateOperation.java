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
package org.eclipse.scout.sdk.operation.method;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.util.ScoutSignature;

/**
 *
 */
public class FieldGetterCreateOperation extends MethodCreateOperation {

  private final IType m_field;

  public FieldGetterCreateOperation(IType field, IType getterDeclaringType) throws JavaModelException {
    this(field, getterDeclaringType, false);

  }

  public FieldGetterCreateOperation(IType field, IType getterDeclaringType, boolean formatSource) throws JavaModelException {
    super(getterDeclaringType, "get" + field.getElementName(), null, formatSource);
    m_field = field;
    setMethodFlags(Flags.AccPublic);
    setReturnTypeSignature(Signature.createTypeSignature(m_field.getFullyQualifiedName(), true));

  }

  @Override
  protected String createMethodBody(IImportValidator validator) throws JavaModelException {
    StringBuilder source = new StringBuilder();
    source.append("return getFieldByClass(");
    source.append(ScoutSignature.getTypeReference(Signature.createTypeSignature(m_field.getFullyQualifiedName(), true), m_field, validator) + ".class");
    source.append(");");
    return source.toString();
  }

  public IType getField() {
    return m_field;
  }

}
