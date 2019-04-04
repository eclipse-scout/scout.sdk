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
package org.eclipse.scout.sdk.core.model.ecj;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.model.api.internal.TypeParameterImplementor;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MemberSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 *
 */
public class BindingTypeParameterWithEcj extends AbstractJavaElementWithEcj<ITypeParameter> implements TypeParameterSpi {
  private final AbstractMemberWithEcj<?> m_declaringMember;
  private final TypeVariableBinding m_binding;
  private final int m_index;
  private final FinalValue<String> m_name;
  private final FinalValue<List<TypeSpi>> m_bounds;
  private final FinalValue<ISourceRange> m_source;

  protected BindingTypeParameterWithEcj(AbstractJavaEnvironment env, AbstractMemberWithEcj<?> declaringMember, TypeVariableBinding binding, int index) {
    super(env);
    m_declaringMember = Ensure.notNull(declaringMember);
    m_binding = Ensure.notNull(binding);
    m_index = index;
    m_name = new FinalValue<>();
    m_bounds = new FinalValue<>();
    m_source = new FinalValue<>();
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    MemberSpi newMember = (MemberSpi) getDeclaringMember().internalFindNewElement();
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
    return m_name.computeIfAbsentAndGet(() -> new String(m_binding.sourceName()));
  }

  @Override
  @SuppressWarnings("null")
  public List<TypeSpi> getBounds() {
    return m_bounds.computeIfAbsentAndGet(() -> {
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
        TypeSpi t = SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), superclass);
        if (t != null) {
          bounds.add(t);
        }
      }
      if (hasSuperInterfaces) {
        for (ReferenceBinding b : superInterfaces) {
          TypeSpi t = SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), b);
          if (t != null) {
            bounds.add(t);
          }
        }
      }
      return bounds;
    });
  }

  @Override
  public AbstractMemberWithEcj<?> getDeclaringMember() {
    return m_declaringMember;
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      ClassScope classScope = SpiWithEcjUtils.classScopeOf(this);
      if (classScope == null) {
        return null;
      }
      TypeParameter decl = classScope.referenceContext.typeParameters[m_index];
      if (decl == null) {
        return null;
      }
      CompilationUnitSpi cu = SpiWithEcjUtils.declaringTypeOf(this).getCompilationUnit();
      return javaEnvWithEcj().getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
    });
  }
}
