/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
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
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 *
 */
public class BindingBaseTypeWithEcj extends AbstractTypeWithEcj {
  private final BaseTypeBinding m_btb;
  private final String m_elementName;

  protected BindingBaseTypeWithEcj(JavaEnvironmentWithEcj env, BaseTypeBinding binding) {
    super(env);
    m_btb = Ensure.notNull(binding);
    m_elementName = new String(m_btb.simpleName);
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    return getJavaEnvironment().findType(getName());
  }

  @Override
  public BaseTypeBinding getInternalBinding() {
    return m_btb;
  }

  @Override
  protected IType internalCreateApi() {
    return new TypeImplementor(this);
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
  public String getName() {
    return m_elementName;
  }

  @Override
  public String getElementName() {
    return m_elementName;
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
  public List<FieldSpi> getFields() {
    return emptyList();
  }

  @Override
  public PackageSpi getPackage() {
    return javaEnvWithEcj().createDefaultPackage();
  }

  @Override
  public ISourceRange getSource() {
    return null;
  }

  @Override
  public ISourceRange getSourceOfStaticInitializer() {
    return null;
  }

  @Override
  public ISourceRange getJavaDoc() {
    return null;
  }

  @Override
  public boolean isWildcardType() {
    return false;
  }
}
