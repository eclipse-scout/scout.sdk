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

/**
 * <h3>{@link WildcardOnlyTypeWithEcj}</h3>
 *
 * @since 5.1.0
 */
public class WildcardOnlyTypeWithEcj extends AbstractTypeWithEcj {

  protected WildcardOnlyTypeWithEcj(AbstractJavaEnvironment env) {
    super(env);
  }

  @Override
  public WildcardOnlyTypeWithEcj internalFindNewElement() {
    return ((JavaEnvironmentWithEcj) getJavaEnvironment()).createWildcardOnlyType();
  }

  @Override
  public TypeBinding getInternalBinding() {
    return null;
  }

  @Override
  protected IType internalCreateApi() {
    return new TypeImplementor(this);
  }

  @Override
  public int getFlags() {
    return Flags.AccDefault;
  }

  @Override
  public TypeSpi getDeclaringType() {
    return null;
  }

  @Override
  public List<AnnotationSpi> getAnnotations() {
    return emptyList();
  }

  @Override
  public PackageSpi getPackage() {
    return javaEnvWithEcj().createDefaultPackage();
  }

  @Override
  public CompilationUnitSpi getCompilationUnit() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getElementName() {
    return "?";
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    return emptyList();
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
  public boolean isPrimitive() {
    return false;
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
  public List<FieldSpi> getFields() {
    return emptyList();
  }

  @Override
  public boolean isWildcardType() {
    return true;
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

  @Override
  public String toString() {
    return "?";
  }
}
