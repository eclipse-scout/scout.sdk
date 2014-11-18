/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation;

/**
 * <h3>{@link TypeParameter}</h3>
 * 
 * @author aho
 * @since 4.1.0 09.11.2014
 */
public class TypeParameter implements ITypeParameter {

  private final String m_parameterName;
  private final String m_parameterSignature;

  public TypeParameter(String parameterSignature) {
    this(null, parameterSignature);
  }

  public TypeParameter(String parameterName, String parameterSignature) {
    m_parameterName = parameterName;
    m_parameterSignature = parameterSignature;

  }

  @Override
  public String getParameterName() {
    return m_parameterName;
  }

  @Override
  public String getParameterSignature() {
    return m_parameterSignature;
  }

}
