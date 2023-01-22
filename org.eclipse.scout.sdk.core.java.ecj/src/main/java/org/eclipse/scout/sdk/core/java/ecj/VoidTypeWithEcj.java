/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.internal.TypeImplementor;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.java.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.java.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.java.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.java.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class VoidTypeWithEcj extends AbstractTypeWithEcj {

  protected VoidTypeWithEcj(AbstractJavaEnvironment env) {
    super(env);
  }

  @Override
  public VoidTypeWithEcj internalFindNewElement() {
    return ((JavaEnvironmentWithEcj) getJavaEnvironment()).createVoidType();
  }

  @Override
  protected IType internalCreateApi() {
    return new TypeImplementor(this);
  }

  @Override
  public TypeBinding getInternalBinding() {
    return null;
  }

  @Override
  public List<AnnotationSpi> getAnnotations() {
    return emptyList();
  }

  @Override
  public int getArrayDimension() {
    return 0;
  }

  @Override
  public TypeSpi getLeafComponentType() {
    return null;
  }

  @Override
  public CompilationUnitSpi getCompilationUnit() {
    return null;
  }

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public PackageSpi getPackage() {
    return javaEnvWithEcj().createDefaultPackage();
  }

  @Override
  public String getName() {
    return JavaTypes._void;
  }

  @Override
  public String getElementName() {
    return JavaTypes._void;
  }

  @Override
  public TypeSpi getDeclaringType() {
    return null;
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    return emptyList();
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
  public TypeSpi getSuperClass() {
    return null;
  }

  @Override
  public List<TypeSpi> getSuperInterfaces() {
    return emptyList();
  }

  @Override
  public List<TypeSpi> getTypes() {
    return emptyList();
  }

  @Override
  public List<MethodSpi> getMethods() {
    return emptyList();
  }

  @Override
  public List<TypeSpi> getTypeArguments() {
    return emptyList();
  }

  @Override
  public String toString() {
    return JavaTypes._void;
  }

  @Override
  public List<FieldSpi> getFields() {
    return emptyList();
  }

  @Override
  public boolean isWildcardType() {
    return false;
  }

  @Override
  public SourceRange getSource() {
    return null;
  }

  @Override
  public SourceRange getSourceOfStaticInitializer() {
    return null;
  }

  @Override
  public SourceRange getJavaDoc() {
    return null;
  }
}
