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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.model.api.internal.TypeParameterImplementor;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MemberSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 *
 */
public class BindingTypeParameterWithJdt extends AbstractJavaElementWithJdt<ITypeParameter> implements TypeParameterSpi {
  private final AbstractMemberWithJdt<?> m_declaringMember;
  private final TypeVariableBinding m_binding;
  private final int m_index;
  private String m_name;
  private List<TypeSpi> m_bounds;
  private ISourceRange m_source;

  BindingTypeParameterWithJdt(JavaEnvironmentWithJdt env, AbstractMemberWithJdt<?> declaringMember, TypeVariableBinding binding, int index) {
    super(env);
    m_declaringMember = Validate.notNull(declaringMember);
    m_binding = Validate.notNull(binding);
    m_index = index;
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    MemberSpi newMember = (MemberSpi) getDeclaringMember().internalFindNewElement(newEnv);
    if (newMember != null && newMember.getTypeParameters().size() > m_index) {
      return newMember.getTypeParameters().get(m_index);
    }
    return null;
  }

  @Override
  protected ITypeParameter internalCreateApi() {
    return new TypeParameterImplementor(this);
  }

  public TypeVariableBinding getInternalBinding() {
    return m_binding;
  }

  @Override
  public String getElementName() {
    if (m_name == null) {
      m_name = new String(m_binding.sourceName());
    }
    return m_name;
  }

  @Override
  @SuppressWarnings("null")
  public List<TypeSpi> getBounds() {
    if (m_bounds != null) {
      return m_bounds;
    }

    ReferenceBinding superclass = m_binding.superclass();
    ReferenceBinding[] superInterfaces = m_binding.superInterfaces();
    boolean hasSuperClass = superclass != null && !CharOperation.equals(superclass.compoundName, TypeConstants.JAVA_LANG_OBJECT);
    boolean hasSuperInterfaces = superInterfaces != null && superInterfaces.length > 0;
    int size = 0;
    if (hasSuperClass) {
      size++;
    }
    if (hasSuperInterfaces) {
      size += superInterfaces.length;
    }

    List<TypeSpi> bounds = new ArrayList<>(size);
    if (hasSuperClass) {
      TypeSpi t = SpiWithJdtUtils.bindingToType(m_env, superclass);
      if (t != null) {
        bounds.add(t);
      }
    }
    if (hasSuperInterfaces) {
      for (ReferenceBinding b : superInterfaces) {
        TypeSpi t = SpiWithJdtUtils.bindingToType(m_env, b);
        if (t != null) {
          bounds.add(t);
        }
      }
    }
    m_bounds = Collections.unmodifiableList(bounds);
    return m_bounds;
  }

  @Override
  public AbstractMemberWithJdt<?> getDeclaringMember() {
    return m_declaringMember;
  }

  @Override
  public ISourceRange getSource() {
    if (m_source == null) {
      TypeParameter decl = SpiWithJdtUtils.classScopeOf(this).referenceContext.typeParameters[m_index];
      if (decl != null) {
        CompilationUnitSpi cu = SpiWithJdtUtils.declaringTypeOf(this).getCompilationUnit();
        m_source = m_env.getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
      }
      else {
        m_source = ISourceRange.NO_SOURCE;
      }
    }
    return m_source;
  }
}
