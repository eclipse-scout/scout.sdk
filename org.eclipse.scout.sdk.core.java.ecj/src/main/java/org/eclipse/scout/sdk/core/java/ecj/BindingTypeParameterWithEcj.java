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

import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.bindingToType;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.bindingsToTypes;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.classScopeOf;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.declaringTypeOf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.scout.sdk.core.java.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.java.model.api.internal.TypeParameterImplementor;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.MemberSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class BindingTypeParameterWithEcj extends AbstractJavaElementWithEcj<ITypeParameter> implements TypeParameterSpi {
  private final AbstractMemberWithEcj<?> m_declaringMember;
  private final TypeVariableBinding m_binding;
  private final int m_index;
  private final FinalValue<String> m_name;
  private final FinalValue<List<TypeSpi>> m_bounds;
  private final FinalValue<SourceRange> m_source;

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
  public TypeParameterSpi internalFindNewElement() {
    var newMember = (MemberSpi) getDeclaringMember().internalFindNewElement();
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

  protected static ReferenceBinding getSuperClassBinding(BindingTypeParameterWithEcj tp) {
    return tp.m_binding.superclass();
  }

  protected static ReferenceBinding[] getSuperInterfaceBindings(BindingTypeParameterWithEcj tp) {
    return tp.m_binding.superInterfaces();
  }

  @Override
  @SuppressWarnings("null")
  public List<TypeSpi> getBounds() {
    return m_bounds.computeIfAbsentAndGet(() -> {
      ReferenceBinding superclass;
      ReferenceBinding[] superInterfaces;
      var javaEnv = javaEnvWithEcj();
      synchronized (javaEnv.lock()) {
        superclass = getSuperClassBinding(this);
        superInterfaces = getSuperInterfaceBindings(this);
      }

      var hasSuperClass = superclass != null && !CharOperation.equals(superclass.compoundName, TypeConstants.JAVA_LANG_OBJECT);
      var hasSuperInterfaces = superInterfaces != null && superInterfaces.length > 0;
      var size = 0;
      if (hasSuperClass) {
        size++;
      }
      if (hasSuperInterfaces) {
        size += superInterfaces.length;
      }

      List<TypeSpi> bounds = new ArrayList<>(size);
      if (hasSuperClass) {
        var t = bindingToType(javaEnv, superclass, () -> withNewElement(BindingTypeParameterWithEcj::getSuperClassBinding));
        if (t != null) {
          bounds.add(t);
        }
      }
      if (hasSuperInterfaces) {
        bounds.addAll(bindingsToTypes(javaEnv, superInterfaces, () -> withNewElement(BindingTypeParameterWithEcj::getSuperInterfaceBindings)));
      }
      return bounds;
    });
  }

  @Override
  public AbstractMemberWithEcj<?> getDeclaringMember() {
    return m_declaringMember;
  }

  @Override
  public SourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      var classScope = classScopeOf(this);
      if (classScope == null) {
        return null;
      }
      var decl = classScope.referenceContext.typeParameters[m_index];
      if (decl == null) {
        return null;
      }
      var cu = declaringTypeOf(this).getCompilationUnit();
      return javaEnvWithEcj().getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
    });
  }
}
