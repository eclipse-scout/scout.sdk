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
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.internal.TypeImplementor;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 * <h3>{@link WildcardOnlyTypeWithJdt}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class WildcardOnlyTypeWithJdt extends AbstractTypeWithJdt {

  WildcardOnlyTypeWithJdt(JavaEnvironmentWithJdt env) {
    super(env);
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    return newEnv.createWildcardOnlyType();
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
    return new ArrayList<>(0);
  }

  @Override
  public PackageSpi getPackage() {
    return m_env.createDefaultPackage();
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
  public TypeSpi getSuperClass() {
    return null;
  }

  @Override
  public List<TypeSpi> getSuperInterfaces() {
    return new ArrayList<>(0);
  }

  @Override
  public List<TypeSpi> getTypes() {
    return new ArrayList<>(0);
  }

  @Override
  public List<MethodSpi> getMethods() {
    return new ArrayList<>(0);
  }

  @Override
  public List<TypeSpi> getTypeArguments() {
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
  public TypeSpi getLeafComponentType() {
    return null;
  }

  @Override
  public List<FieldSpi> getFields() {
    return new ArrayList<>(0);
  }

  @Override
  public boolean isWildcardType() {
    return true;
  }

  @Override
  public TypeSpi getOriginalType() {
    return this;
  }

  @Override
  public ISourceRange getSource() {
    return ISourceRange.NO_SOURCE;
  }

  @Override
  public ISourceRange getSourceOfStaticInitializer() {
    return ISourceRange.NO_SOURCE;
  }

  @Override
  public ISourceRange getJavaDoc() {
    return ISourceRange.NO_SOURCE;
  }

  @Override
  public String toString() {
    return "?";
  }
}
