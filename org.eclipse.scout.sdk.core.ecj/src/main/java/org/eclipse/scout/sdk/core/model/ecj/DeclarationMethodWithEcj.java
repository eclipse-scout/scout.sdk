/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.MethodImplementor;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

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
  private int m_flags;

  protected DeclarationMethodWithEcj(JavaEnvironmentWithEcj env, DeclarationTypeWithEcj declaringType, AbstractMethodDeclaration astNode) {
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
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    return SpiWithEcjUtils.findNewMethodIn(this);
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
    return m_returnType.computeIfAbsentAndGet(() -> {
      if (m_astNode instanceof MethodDeclaration) {
        var ref = ((MethodDeclaration) m_astNode).returnType;
        if (ref.resolvedType == null) {
          synchronized (javaEnvWithEcj().lock()) {
            ref.resolveType(m_astNode.scope);
          }
        }
        return SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), ref.resolvedType);
      }
      return null;
    });

  }

  @Override
  public List<DeclarationAnnotationWithEcj> getAnnotations() {
    return m_annotations.computeIfAbsentAndGet(() -> SpiWithEcjUtils.createDeclarationAnnotations(javaEnvWithEcj(), this, m_astNode.annotations));
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = SpiWithEcjUtils.getMethodFlags(m_astNode.modifiers, isVarArgs(), SpiWithEcjUtils.hasDeprecatedAnnotation(getAnnotations()));
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
    return m_exceptions.computeIfAbsentAndGet(() -> {
      var exceptions = m_astNode.thrownExceptions;
      if (exceptions == null || exceptions.length < 1) {
        return emptyList();
      }

      List<TypeSpi> result = new ArrayList<>(exceptions.length);
      for (var r : exceptions) {
        if (r.resolvedType == null) {
          synchronized (javaEnvWithEcj().lock()) {
            r.resolveType(SpiWithEcjUtils.classScopeOf(this));
          }
        }
        var t = SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), r.resolvedType);
        if (t != null) {
          result.add(t);
        }
      }
      return result;
    });
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    return m_typeParameters.computeIfAbsentAndGet(() -> SpiWithEcjUtils.toTypeParameterSpi(m_astNode.typeParameters(), this, javaEnvWithEcj()));
  }

  @Override
  public boolean hasTypeParameters() {
    var typeParams = m_astNode.typeParameters();
    return typeParams != null && typeParams.length > 0;
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      var cu = m_declaringType.getCompilationUnit();
      var decl = m_astNode;
      return javaEnvWithEcj().getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
    });
  }

  @Override
  public ISourceRange getSourceOfBody() {
    return m_bodySource.computeIfAbsentAndGet(() -> {
      var cu = m_declaringType.getCompilationUnit();
      var decl = m_astNode;
      return javaEnvWithEcj().getSource(cu, decl.bodyStart, decl.bodyEnd);
    });
  }

  @Override
  public ISourceRange getJavaDoc() {
    return m_javaDocSource.computeIfAbsentAndGet(() -> {
      var cu = m_declaringType.getCompilationUnit();
      var doc = m_astNode.javadoc;
      if (doc == null) {
        return null;
      }
      return javaEnvWithEcj().getSource(cu, doc.sourceStart, doc.sourceEnd);
    });
  }
}
