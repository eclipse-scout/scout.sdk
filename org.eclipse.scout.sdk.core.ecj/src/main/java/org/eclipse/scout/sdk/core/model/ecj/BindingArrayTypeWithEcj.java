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
import static java.util.Collections.singletonList;
import static org.eclipse.scout.sdk.core.util.Strings.repeat;

import java.util.List;

import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.internal.TypeImplementor;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 * <h3>{@link BindingArrayTypeWithEcj}</h3>
 *
 * @since 5.1.0
 */
public class BindingArrayTypeWithEcj extends AbstractTypeWithEcj {
  private static final FieldBinding LENGTH_FIELD = new FieldBinding("length".toCharArray(), TypeBinding.INT, Flags.AccPublic | Flags.AccFinal, null, null);

  private final ArrayBinding m_binding;
  private final boolean m_isWildcard;
  private final int m_arrayDimension;
  private final FinalValue<TypeSpi> m_leafComponentType;
  private final FinalValue<String> m_name;
  private final FinalValue<PackageSpi> m_package;
  private final FinalValue<String> m_elementName;
  private final FinalValue<List<BindingAnnotationWithEcj>> m_annotations;
  private final FinalValue<List<FieldSpi>> m_fields;

  protected BindingArrayTypeWithEcj(JavaEnvironmentWithEcj env, ArrayBinding binding, boolean isWildcard) {
    super(env);
    m_binding = Ensure.notNull(binding);
    m_isWildcard = isWildcard;
    m_arrayDimension = binding.dimensions;
    m_package = new FinalValue<>();
    m_elementName = new FinalValue<>();
    m_annotations = new FinalValue<>();
    m_fields = new FinalValue<>();
    m_leafComponentType = new FinalValue<>();
    m_name = new FinalValue<>();
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    return getJavaEnvironment().findType(getName());
  }

  @Override
  protected IType internalCreateApi() {
    return new TypeImplementor(this);
  }

  @Override
  public int getArrayDimension() {
    return m_arrayDimension;
  }

  @Override
  public TypeSpi getLeafComponentType() {
    return m_leafComponentType.computeIfAbsentAndGet(() -> SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), m_binding.leafComponentType));
  }

  @Override
  public ArrayBinding getInternalBinding() {
    return m_binding;
  }

  @Override
  public CompilationUnitSpi getCompilationUnit() {
    return null;
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public PackageSpi getPackage() {
    return m_package.computeIfAbsentAndGet(() -> BindingTypeWithEcj.packageOf(m_binding, javaEnvWithEcj()));
  }

  @Override
  public String getName() {
    return m_name.computeIfAbsentAndGet(() -> {
      var componentTypeName = getLeafComponentType().getName();
      var b = new StringBuilder(componentTypeName.length() + (2 * m_arrayDimension));
      b.append(componentTypeName);
      b.append(repeat("[]", m_arrayDimension));
      return b.toString();
    });
  }

  @Override
  public String getElementName() {
    return m_elementName.computeIfAbsentAndGet(() -> new String(m_binding.sourceName()));
  }

  @Override
  public List<FieldSpi> getFields() {
    return m_fields.computeIfAbsentAndGet(() -> singletonList(javaEnvWithEcj().createBindingField(this, LENGTH_FIELD)));
  }

  @Override
  public List<MethodSpi> getMethods() {
    return emptyList();
  }

  @Override
  public List<TypeSpi> getTypes() {
    return emptyList();
  }

  @Override
  public BindingTypeWithEcj getDeclaringType() {
    return null;
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
  public boolean isWildcardType() {
    return m_isWildcard;
  }

  @Override
  public List<TypeSpi> getTypeArguments() {
    return emptyList();
  }

  @Override
  public boolean hasTypeParameters() {
    return false;
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    return emptyList();
  }

  @Override
  public List<BindingAnnotationWithEcj> getAnnotations() {
    return m_annotations.computeIfAbsentAndGet(() -> SpiWithEcjUtils.createBindingAnnotations(this, m_binding));
  }

  @Override
  public int getFlags() {
    return 0;
  }

  @Override
  public boolean isAnonymous() {
    return false;
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
}
