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
package org.eclipse.scout.sdk.internal.workspace.dto;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 * <h3>{@link AbstractTableSourceBuilder}</h3>
 * 
 * @author aho
 * @since 3.10.0 27.08.2013
 */
public abstract class AbstractTableSourceBuilder extends AbstractDtoTypeSourceBuilder {

  /**
   * @param modelType
   * @param elementName
   * @param setup
   */
  public AbstractTableSourceBuilder(IType modelType, String elementName, boolean setup) {
    super(modelType, elementName, setup);
  }

  protected String getColumnSignature(IType type, ITypeHierarchy columnHierarchy) throws IllegalArgumentException, CoreException {
    if (type == null || Object.class.getName().equals(type.getFullyQualifiedName())) {
      return null;
    }
    IType superType = columnHierarchy.getSuperclass(type);
    if (TypeUtility.exists(superType)) {
      if (TypeUtility.isGenericType(superType)) {
        String superTypeSig = type.getSuperclassTypeSignature();
        return SignatureUtility.getResolvedSignature(Signature.getTypeArguments(superTypeSig)[0], type);
      }
      return getColumnSignature(superType, columnHierarchy);
    }
    return null;
  }
}
