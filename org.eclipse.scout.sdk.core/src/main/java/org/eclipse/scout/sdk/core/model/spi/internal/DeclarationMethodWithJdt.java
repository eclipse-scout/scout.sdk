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

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.MethodImplementor;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 *
 */
public class DeclarationMethodWithJdt extends AbstractMemberWithJdt<IMethod> implements MethodSpi {
  private final DeclarationTypeWithJdt m_declaringType;
  private final AbstractMethodDeclaration m_astNode;
  private TypeSpi m_returnType;
  private List<DeclarationAnnotationWithJdt> m_annotations;
  private int m_flags;
  private List<MethodParameterSpi> m_arguments;
  private String m_name;
  private List<TypeSpi> m_exceptions;
  private List<TypeParameterSpi> m_typeParameters;

  DeclarationMethodWithJdt(JavaEnvironmentWithJdt env, DeclarationTypeWithJdt declaringType, AbstractMethodDeclaration astNode) {
    super(env);
    m_declaringType = Validate.notNull(declaringType);
    m_astNode = Validate.notNull(astNode);
    m_flags = -1; // mark as uninitialized
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    TypeSpi newType = (TypeSpi) getDeclaringType().internalFindNewElement(newEnv);
    if (newType != null) {
      final String oldSig = SpiWithJdtUtils.createMethodId(this);
      for (MethodSpi newM : newType.getMethods()) {
        if (oldSig.equals(SpiWithJdtUtils.createMethodId(newM))) {
          return newM;
        }
      }
    }
    return null;
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
    if (m_returnType == null && m_astNode instanceof MethodDeclaration) {
      TypeReference ref = ((MethodDeclaration) m_astNode).returnType;
      if (ref.resolvedType == null) {
        ref.resolveType(m_astNode.scope);
      }
      TypeSpi result = SpiWithJdtUtils.bindingToType(m_env, ref.resolvedType);
      m_returnType = result;
    }
    return m_returnType;
  }

  @Override
  public List<DeclarationAnnotationWithJdt> getAnnotations() {
    if (m_annotations == null) {
      Annotation[] annots = m_astNode.annotations;
      m_annotations = SpiWithJdtUtils.createDeclarationAnnotations(m_env, this, annots);
    }
    return m_annotations;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = SpiWithJdtUtils.getMethodFlags(m_astNode.modifiers, isVarArgs(), SpiWithJdtUtils.hasDeprecatedAnnotation(m_astNode.annotations));
    }
    return m_flags;
  }

  private boolean isVarArgs() {
    Argument[] arguments = m_astNode.arguments;
    if (arguments != null && arguments.length > 0) {
      return arguments[arguments.length - 1].isVarArgs();
    }
    return false;
  }

  @Override
  public List<MethodParameterSpi> getParameters() {
    if (m_arguments == null) {
      Argument[] arguments = m_astNode.arguments;
      if (arguments == null || arguments.length < 1) {
        m_arguments = new ArrayList<>(0);
      }
      else {
        List<MethodParameterSpi> result = new ArrayList<>(arguments.length);
        for (int i = 0; i < arguments.length; i++) {
          result.add(m_env.createDeclarationMethodParameter(this, arguments[i], i));
        }
        m_arguments = result;
      }
    }
    return m_arguments;
  }

  @Override
  public DeclarationTypeWithJdt getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public String getElementName() {
    if (m_name == null) {
      if (m_astNode.selector != null) {
        m_name = new String(m_astNode.selector);
      }
      else if (m_astNode.binding.selector != null) {
        m_name = new String(m_astNode.binding.selector);
      }
      else {
        // m_md.binding is null for static constructors
        m_name = "<clinit>";
      }
    }
    return m_name;
  }

  @Override
  public List<TypeSpi> getExceptionTypes() {
    if (m_exceptions == null) {
      TypeReference[] exceptions = m_astNode.thrownExceptions;
      if (exceptions == null || exceptions.length < 1) {
        m_exceptions = new ArrayList<>(0);
      }
      else {
        List<TypeSpi> result = new ArrayList<>(exceptions.length);
        for (TypeReference r : exceptions) {
          if (r.resolvedType == null) {
            r.resolveType(SpiWithJdtUtils.classScopeOf(this));
          }
          TypeSpi t = SpiWithJdtUtils.bindingToType(m_env, r.resolvedType);
          if (t != null) {
            result.add(t);
          }
        }
        m_exceptions = result;
      }
    }
    return m_exceptions;
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    if (m_typeParameters == null) {
      TypeParameter[] typeParams = m_astNode.typeParameters();
      if (typeParams == null || typeParams.length < 1) {
        m_typeParameters = new ArrayList<>(0);
      }
      else {
        List<TypeParameterSpi> result = new ArrayList<>(typeParams.length);
        for (int i = 0; i < typeParams.length; i++) {
          result.add(m_env.createDeclarationTypeParameter(this, typeParams[i], i));
        }
        m_typeParameters = result;
      }
    }
    return m_typeParameters;
  }

  @Override
  public boolean hasTypeParameters() {
    TypeParameter[] typeParams = m_astNode.typeParameters();
    return typeParams != null && typeParams.length > 0;
  }

  @Override
  public DeclarationMethodWithJdt getOriginalMethod() {
    return this;
  }

  @Override
  public ISourceRange getSource() {
    CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
    AbstractMethodDeclaration decl = m_astNode;
    return m_env.getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
  }

  @Override
  public ISourceRange getSourceOfBody() {
    CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
    AbstractMethodDeclaration decl = m_astNode;
    return m_env.getSource(cu, decl.bodyStart, decl.bodyEnd);
  }

  @Override
  public ISourceRange getJavaDoc() {
    CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
    AbstractMethodDeclaration decl = m_astNode;
    Javadoc doc = decl.javadoc;
    if (doc != null) {
      return m_env.getSource(cu, doc.sourceStart, doc.sourceEnd);
    }
    return null;
  }

}
