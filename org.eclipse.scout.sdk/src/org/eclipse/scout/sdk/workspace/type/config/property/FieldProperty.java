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
package org.eclipse.scout.sdk.workspace.type.config.property;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;

/**
 * <h3>{@link FieldProperty}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 27.02.2013
 */
public class FieldProperty<T> {
  private IField m_constant;

  public FieldProperty(IField field) {
    m_constant = field;
  }

  public IField getConstant() {
    return m_constant;
  }

  @SuppressWarnings("unchecked")
  public T getSourceValue() throws JavaModelException {
    Object constant = m_constant.getConstant();
    if (constant instanceof String) {
      String stringConstant = (String) constant;
      stringConstant = stringConstant.replaceAll("^\\\"", "");
      stringConstant = stringConstant.replaceAll("\\\"$", "");
      constant = stringConstant;
    }
    return (T) constant;
  }
}
