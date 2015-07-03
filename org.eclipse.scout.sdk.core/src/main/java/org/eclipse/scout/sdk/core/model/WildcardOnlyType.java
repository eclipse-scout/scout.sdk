/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.set.ListOrderedSet;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;

/**
 * <h3>{@link WildcardOnlyType}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class WildcardOnlyType implements IType {

  private final ILookupEnvironment m_env;

  public WildcardOnlyType(ILookupEnvironment env) {
    m_env = env;
  }

  @Override
  public int getFlags() {
    return Flags.AccDefault;
  }

  @Override
  public IType getDeclaringType() {
    return null;
  }

  @Override
  public ListOrderedSet<IAnnotation> getAnnotations() {
    return ListOrderedSet.listOrderedSet(new HashSet<IAnnotation>(0));
  }

  @Override
  public IPackage getPackage() {
    return IPackage.DEFAULT_PACKAGE;
  }

  @Override
  public ICompilationUnit getCompilationUnit() {
    return null;
  }

  @Override
  public String getSimpleName() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public List<ITypeParameter> getTypeParameters() {
    return new ArrayList<>(0);
  }

  @Override
  public boolean hasTypeParameters() {
    return false;
  }

  @Override
  public boolean isAnonymous() {
    return false;
  }

  @Override
  public IType getSuperClass() {
    return null;
  }

  @Override
  public ListOrderedSet<IType> getSuperInterfaces() {
    return ListOrderedSet.listOrderedSet(new HashSet<IType>(0));
  }

  @Override
  public ListOrderedSet<IType> getTypes() {
    return ListOrderedSet.listOrderedSet(new HashSet<IType>(0));
  }

  @Override
  public ListOrderedSet<IMethod> getMethods() {
    return ListOrderedSet.listOrderedSet(new HashSet<IMethod>(0));
  }

  @Override
  public List<IType> getTypeArguments() {
    return new ArrayList<>(0);
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public int getArrayDimension() {
    return 0;
  }

  @Override
  public ListOrderedSet<IField> getFields() {
    return ListOrderedSet.listOrderedSet(new HashSet<IField>(0));
  }

  @Override
  public boolean isWildcardType() {
    return true;
  }

  @Override
  public ILookupEnvironment getLookupEnvironment() {
    return m_env;
  }

  @Override
  public String toString() {
    return "?";
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof WildcardOnlyType;
  }

  @Override
  public int hashCode() {
    return '?';
  }
}
