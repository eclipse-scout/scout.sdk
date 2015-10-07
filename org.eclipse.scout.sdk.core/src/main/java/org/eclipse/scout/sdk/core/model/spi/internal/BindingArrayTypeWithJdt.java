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

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
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

/**
 * <h3>{@link BindingArrayTypeWithJdt}</h3>
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class BindingArrayTypeWithJdt extends AbstractTypeWithJdt {
  private static final FieldBinding LENGTH_FIELD = new FieldBinding("length".toCharArray(), TypeBinding.INT, Modifier.PUBLIC | Modifier.FINAL, null, null);

  private final ArrayBinding m_binding;
  private final boolean m_isWildcard;
  private final int m_arrayDimension;
  private final TypeSpi m_leafComponentType;
  private final String m_name;
  private PackageSpi m_package;
  private String m_elementName;
  private List<BindingAnnotationWithJdt> m_annotations;
  private List<FieldSpi> m_fields;

  BindingArrayTypeWithJdt(JavaEnvironmentWithJdt env, ArrayBinding binding, boolean isWildcard) {
    super(env);
    m_binding = Validate.notNull(binding);
    m_isWildcard = isWildcard;
    m_arrayDimension = binding.dimensions;
    m_leafComponentType = SpiWithJdtUtils.bindingToType(env, binding.leafComponentType);
    m_name = m_leafComponentType.getName() + StringUtils.leftPad("", m_arrayDimension * 2, "[]");
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    return newEnv.findType(getName());
  }

  @Override
  protected IType internalCreateApi() {
    return new TypeImplementor(this);
  }

  @Override
  public boolean isArray() {
    return true;
  }

  @Override
  public int getArrayDimension() {
    return m_arrayDimension;
  }

  @Override
  public TypeSpi getLeafComponentType() {
    return m_leafComponentType;
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
    if (m_package == null) {
      char[] qualifiedPackageName = m_binding.qualifiedPackageName();
      if (qualifiedPackageName == null || qualifiedPackageName.length < 1) {
        m_package = m_env.createDefaultPackage();
      }
      else {
        m_package = m_env.createPackage(new String(qualifiedPackageName));
      }
    }
    return m_package;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public String getElementName() {
    if (m_elementName == null) {
      m_elementName = new String(m_binding.sourceName());
    }
    return m_elementName;
  }

  @Override
  public List<FieldSpi> getFields() {
    if (m_fields == null) {
      m_fields = Collections.<FieldSpi> singletonList(m_env.createBindingField(this, LENGTH_FIELD));
    }
    return m_fields;
  }

  @Override
  public List<MethodSpi> getMethods() {
    return Collections.emptyList();
  }

  @Override
  public List<TypeSpi> getTypes() {
    return Collections.emptyList();
  }

  @Override
  public BindingTypeWithJdt getDeclaringType() {
    return null;
  }

  @Override
  public TypeSpi getSuperClass() {
    return null;
  }

  @Override
  public List<TypeSpi> getSuperInterfaces() {
    return Collections.emptyList();
  }

  @Override
  public boolean isWildcardType() {
    return m_isWildcard;
  }

  @Override
  public List<TypeSpi> getTypeArguments() {
    return Collections.emptyList();
  }

  @Override
  public boolean hasTypeParameters() {
    return false;
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    return Collections.emptyList();
  }

  @Override
  public TypeSpi getOriginalType() {
    return this;
  }

  @Override
  public List<BindingAnnotationWithJdt> getAnnotations() {
    if (m_annotations == null) {
      m_annotations = SpiWithJdtUtils.createBindingAnnotations(m_env, this, m_binding.getAnnotations());
    }
    return m_annotations;
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
