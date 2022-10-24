/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.bindingToType;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.bindingsToTypes;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.createDeclarationAnnotations;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.createSourceRange;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.findNewMethodIn;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.getMethodFlags;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.hasDeprecatedAnnotation;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.toTypeParameterSpi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.MethodImplementor;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 *
 */
public class DeclarationMethodWithEcj extends AbstractMemberWithEcj<IMethod> implements MethodSpi {
  private final DeclarationTypeWithEcj m_declaringType;
  private final AbstractMethodDeclaration m_astNode;
  private final FinalValue<TypeSpi> m_returnType;
  private final FinalValue<List<DeclarationAnnotationWithEcj>> m_annotations;
  private final FinalValue<List<MethodParameterSpi>> m_arguments;
  private final FinalValue<String> m_name;
  private final FinalValue<List<TypeSpi>> m_exceptions;
  private final FinalValue<List<TypeParameterSpi>> m_typeParameters;
  private final FinalValue<ISourceRange> m_source;
  private final FinalValue<ISourceRange> m_bodySource;
  private final FinalValue<ISourceRange> m_javaDocSource;
  private final FinalValue<String> m_methodId;
  private int m_flags;

  protected DeclarationMethodWithEcj(AbstractJavaEnvironment env, DeclarationTypeWithEcj declaringType, AbstractMethodDeclaration astNode) {
    super(env);
    m_declaringType = Ensure.notNull(declaringType);
    m_astNode = Ensure.notNull(astNode);
    m_flags = -1; // mark as uninitialized
    m_returnType = new FinalValue<>();
    m_annotations = new FinalValue<>();
    m_arguments = new FinalValue<>();
    m_name = new FinalValue<>();
    m_exceptions = new FinalValue<>();
    m_typeParameters = new FinalValue<>();
    m_source = new FinalValue<>();
    m_bodySource = new FinalValue<>();
    m_javaDocSource = new FinalValue<>();
    m_methodId = new FinalValue<>();
  }

  @Override
  public MethodSpi internalFindNewElement() {
    return findNewMethodIn(getDeclaringType(), getMethodId());
  }

  @Override
  public String getMethodId() {
    return m_methodId.computeIfAbsentAndGet(() -> JavaTypes.createMethodIdentifier(this));
  }

  @Override
  protected IMethod internalCreateApi() {
    return new MethodImplementor(this);
  }

  public AbstractMethodDeclaration getInternalMethodDeclaration() {
    return m_astNode;
  }

  @Override
  public boolean isConstructor() {
    return m_astNode.isConstructor();
  }

  @Override
  public TypeSpi getReturnType() {
    return m_returnType.computeIfAbsentAndGet(() -> bindingToType(javaEnvWithEcj(), resolveReturnType(this), () -> withNewElement(this::resolveReturnType)));
  }

  protected TypeBinding resolveReturnType(DeclarationMethodWithEcj declaration) {
    if (!(declaration.m_astNode instanceof MethodDeclaration methodDeclaration)) {
      return null;
    }
    var ref = methodDeclaration.returnType;
    if (ref.resolvedType != null) {
      return ref.resolvedType;
    }
    synchronized (javaEnvWithEcj().lock()) {
      return ref.resolveType(methodDeclaration.scope);
    }
  }

  @Override
  public List<DeclarationAnnotationWithEcj> getAnnotations() {
    return m_annotations.computeIfAbsentAndGet(() -> createDeclarationAnnotations(javaEnvWithEcj(), this, m_astNode.annotations));
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = getMethodFlags(m_astNode.modifiers, isVarArgs(), hasDeprecatedAnnotation(getAnnotations()));
    }
    return m_flags;
  }

  private boolean isVarArgs() {
    var arguments = m_astNode.arguments;
    return arguments != null && arguments.length > 0 && arguments[arguments.length - 1].isVarArgs();
  }

  @Override
  public List<MethodParameterSpi> getParameters() {
    return m_arguments.computeIfAbsentAndGet(() -> {
      var arguments = m_astNode.arguments;
      if (arguments == null || arguments.length < 1) {
        return emptyList();
      }
      return IntStream.range(0, arguments.length)
          .mapToObj(i -> javaEnvWithEcj().createDeclarationMethodParameter(this, arguments[i], i))
          .collect(toList());
    });
  }

  @Override
  public DeclarationTypeWithEcj getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public String getElementName() {
    return m_name.computeIfAbsentAndGet(() -> {
      if (m_astNode.selector != null) {
        return new String(m_astNode.selector);
      }
      else if (m_astNode.binding.selector != null) {
        return new String(m_astNode.binding.selector);
      }
      else {
        // m_md.binding is null for static constructors
        return "<clinit>";
      }
    });
  }

  @Override
  public List<TypeSpi> getExceptionTypes() {
    return m_exceptions.computeIfAbsentAndGet(() -> bindingsToTypes(javaEnvWithEcj(), resolveExceptionTypes(this), () -> withNewElement(this::resolveExceptionTypes)));
  }

  protected TypeBinding[] resolveExceptionTypes(DeclarationMethodWithEcj method) {
    var exceptions = method.m_astNode.thrownExceptions;
    if (exceptions == null || exceptions.length < 1) {
      return Binding.NO_TYPES;
    }

    List<TypeBinding> result = new ArrayList<>(exceptions.length);
    for (var r : exceptions) {
      var resolved = r.resolvedType;
      if (resolved == null) {
        synchronized (javaEnvWithEcj().lock()) {
          resolved = r.resolveType(method.m_astNode.scope);
        }
      }
      if (resolved != null) {
        result.add(resolved);
      }
    }
    return result.toArray(new TypeBinding[0]);
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    return m_typeParameters.computeIfAbsentAndGet(() -> toTypeParameterSpi(m_astNode.typeParameters(), this, javaEnvWithEcj()));
  }

  @Override
  public boolean hasTypeParameters() {
    var typeParams = m_astNode.typeParameters();
    return typeParams != null && typeParams.length > 0;
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> javaEnvWithEcj().getSource(m_declaringType.getCompilationUnit(), m_astNode.declarationSourceStart, m_astNode.declarationSourceEnd));
  }

  @Override
  public ISourceRange getSourceOfBody() {
    return m_bodySource.computeIfAbsentAndGet(() -> javaEnvWithEcj().getSource(m_declaringType.getCompilationUnit(), m_astNode.bodyStart, m_astNode.bodyEnd));
  }

  @Override
  public ISourceRange getJavaDoc() {
    return m_javaDocSource.computeIfAbsentAndGet(() -> createSourceRange(m_astNode.javadoc, m_declaringType.getCompilationUnit(), javaEnvWithEcj()));
  }

  @Override
  public ISourceRange getSourceOfDeclaration() {
    return createSourceRange(m_astNode, m_declaringType.getCompilationUnit(), javaEnvWithEcj());
  }
}
