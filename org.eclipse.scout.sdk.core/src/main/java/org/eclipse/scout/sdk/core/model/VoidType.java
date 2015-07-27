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
import java.util.List;

import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;

/**
 *
 */
final class VoidType implements IType {

  static final IType INSTANCE = new VoidType();

  private VoidType() {
  }

  @Override
  public List<IAnnotation> getAnnotations() {
    return new ArrayList<>(0);
  }

  @Override
  public int getArrayDimension() {
    return 0;
  }

  @Override
  public ICompilationUnit getCompilationUnit() {
    return null;
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public String getSimpleName() {
    return "void";
  }

  @Override
  public IPackage getPackage() {
    return IPackage.DEFAULT_PACKAGE;
  }

  @Override
  public String getName() {
    return getSimpleName();
  }

  @Override
  public IType getDeclaringType() {
    return null;
  }

  @Override
  public List<ITypeParameter> getTypeParameters() {
    return new ArrayList<>(0);
  }

  @Override
  public int getFlags() {
    return Flags.AccDefault;
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
  public List<IType> getSuperInterfaces() {
    return new ArrayList<>(0);
  }

  @Override
  public List<IType> getTypes() {
    return new ArrayList<>(0);
  }

  @Override
  public List<IMethod> getMethods() {
    return new ArrayList<>(0);
  }

  @Override
  public List<IType> getTypeArguments() {
    return new ArrayList<>(0);
  }

  @Override
  public String toString() {
    return getSimpleName();
  }

  @Override
  public List<IField> getFields() {
    return new ArrayList<>(0);
  }

  @Override
  public boolean isWildcardType() {
    return false;
  }

  @Override
  public ILookupEnvironment getLookupEnvironment() {
    return null;
  }
}
