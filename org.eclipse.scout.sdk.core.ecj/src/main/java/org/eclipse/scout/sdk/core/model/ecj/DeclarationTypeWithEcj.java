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
import static java.util.stream.Collectors.toList;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
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
import org.eclipse.scout.sdk.core.util.JavaTypes;

public class DeclarationTypeWithEcj extends AbstractTypeWithEcj {
  private final CompilationUnitSpi m_cu;
  private final DeclarationTypeWithEcj m_declaringType;
  private final TypeDeclaration m_astNode;
  private final List<TypeSpi> m_typeArguments;

  private final FinalValue<PackageSpi> m_package;
  private final FinalValue<String> m_fqn;
  private final FinalValue<String> m_elementName;
  private final FinalValue<TypeSpi> m_superType;
  private final FinalValue<List<TypeSpi>> m_memberTypes;
  private final FinalValue<List<TypeSpi>> m_superInterfaces;
  private final FinalValue<List<TypeParameterSpi>> m_typeParameters;
  private final FinalValue<List<DeclarationAnnotationWithEcj>> m_annotations;
  private final FinalValue<List<MethodSpi>> m_methods;
  private final FinalValue<List<FieldSpi>> m_fields;
  private final FinalValue<ISourceRange> m_source;
  private final FinalValue<ISourceRange> m_javaDocSource;
  private final FinalValue<ISourceRange> m_staticInitSource;
  private int m_flags;

  protected DeclarationTypeWithEcj(JavaEnvironmentWithEcj env, CompilationUnitSpi cu, DeclarationTypeWithEcj declaringType, TypeDeclaration astNode) {
    super(env);
    m_cu = Ensure.notNull(cu);
    m_declaringType = declaringType;
    m_astNode = Ensure.notNull(astNode);
    m_typeArguments = emptyList(); // no arguments for declarations
    m_flags = -1; // mark as uninitialized
    m_package = new FinalValue<>();
    m_fqn = new FinalValue<>();
    m_elementName = new FinalValue<>();
    m_superType = new FinalValue<>();
    m_memberTypes = new FinalValue<>();
    m_superInterfaces = new FinalValue<>();
    m_typeParameters = new FinalValue<>();
    m_annotations = new FinalValue<>();
    m_methods = new FinalValue<>();
    m_fields = new FinalValue<>();
    m_source = new FinalValue<>();
    m_javaDocSource = new FinalValue<>();
    m_staticInitSource = new FinalValue<>();
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    var newSpi = getJavaEnvironment().findType(getName());
    if (newSpi == null) {
      return null;
    }
    return newSpi.getCompilationUnit()
        .getTypes().stream()
        .filter(declType -> declType.getName().equals(getName()))
        .findFirst()
        .orElse(null);
  }

  @Override
  public TypeBinding getInternalBinding() {
    return null;
  }

  @Override
  protected IType internalCreateApi() {
    return new TypeImplementor(this);
  }

  public TypeDeclaration getInternalTypeDeclaration() {
    return m_astNode;
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
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public boolean isAnonymous() {
    return m_astNode.name == null || m_astNode.name.length < 1;
  }

  @Override
  public String getName() {
    return m_fqn.computeIfAbsentAndGet(() -> {
      var sb = new StringBuilder(128);

      var qualifiedPackageName = m_astNode.binding.qualifiedPackageName();
      if (qualifiedPackageName != null && qualifiedPackageName.length > 0) {
        sb.append(qualifiedPackageName);
        sb.append(JavaTypes.C_DOT);
      }

      // collect declaring types
      Deque<char[]> namesBottomUp = new ArrayDeque<>();
      var declaringType = m_astNode;
      while (declaringType != null) {
        namesBottomUp.add(declaringType.name);
        declaringType = declaringType.enclosingType;
      }

      var namesTopDown = namesBottomUp.descendingIterator();
      sb.append(namesTopDown.next()); // there must be at least one type name
      while (namesTopDown.hasNext()) {
        sb.append(JavaTypes.C_DOLLAR).append(namesTopDown.next());
      }
      return sb.toString();
    });
  }

  @Override
  public String getElementName() {
    return m_elementName.computeIfAbsentAndGet(() -> new String(m_astNode.name));
  }

  @Override
  public PackageSpi getPackage() {
    return m_package.computeIfAbsentAndGet(() -> {
      var qualifiedPackageName = m_astNode.binding.qualifiedPackageName();
      if (qualifiedPackageName != null && qualifiedPackageName.length > 0) {
        return javaEnvWithEcj().createPackage(new String(qualifiedPackageName));
      }
      return javaEnvWithEcj().createDefaultPackage();
    });
  }

  @Override
  public List<DeclarationAnnotationWithEcj> getAnnotations() {
    return m_annotations.computeIfAbsentAndGet(() -> SpiWithEcjUtils.createDeclarationAnnotations(javaEnvWithEcj(), this, m_astNode.annotations));
  }

  @Override
  public List<FieldSpi> getFields() {
    return m_fields.computeIfAbsentAndGet(() -> {
      var fields = m_astNode.fields;
      if (fields == null || fields.length < 1) {
        return emptyList();
      }
      return Arrays.stream(fields)
          .map(fd -> javaEnvWithEcj().createDeclarationField(this, fd))
          .collect(toList());
    });
  }

  @Override
  public List<MethodSpi> getMethods() {
    return m_methods.computeIfAbsentAndGet(() -> {
      var methods = m_astNode.methods;
      if (methods == null || methods.length < 1) {
        return emptyList();
      }
      return Arrays.stream(methods)
          .filter(a -> a.bodyStart > 0) // skip compiler generated methods
          .filter(a -> !Arrays.equals(TypeConstants.INIT, a.selector))
          .map(a -> javaEnvWithEcj().createDeclarationMethod(this, a))
          .collect(toList());
    });
  }

  @Override
  public List<TypeSpi> getTypes() {
    return m_memberTypes.computeIfAbsentAndGet(() -> {
      var memberTypes = m_astNode.memberTypes;
      if (memberTypes == null || memberTypes.length < 1) {
        return emptyList();
      }
      return Arrays.stream(memberTypes)
          .map(d -> new DeclarationTypeWithEcj(javaEnvWithEcj(), m_cu, this, d))
          .collect(toList());
    });
  }

  @Override
  public TypeSpi getSuperClass() {
    return m_superType.computeIfAbsentAndGet(() -> {
      if (m_astNode.superclass == null) {
        return null;
      }
      var tb = m_astNode.superclass.resolvedType;
      if (tb == null) {
        return null;
      }
      return SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), tb);
    });
  }

  @Override
  public List<TypeSpi> getSuperInterfaces() {
    return m_superInterfaces.computeIfAbsentAndGet(() -> {
      var refs = m_astNode.superInterfaces;
      if (refs == null || refs.length < 1) {
        return emptyList();
      }
      return Arrays.stream(refs)
          .map(r -> r.resolvedType)
          .filter(Objects::nonNull)
          .map(b -> SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), b))
          .filter(Objects::nonNull)
          .collect(toList());
    });
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = SpiWithEcjUtils.getTypeFlags(m_astNode.modifiers, m_astNode.allocation, SpiWithEcjUtils.hasDeprecatedAnnotation(getAnnotations()));
    }
    return m_flags;
  }

  @Override
  public List<TypeSpi> getTypeArguments() {
    return m_typeArguments;
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    return m_typeParameters.computeIfAbsentAndGet(() -> SpiWithEcjUtils.toTypeParameterSpi(m_astNode.typeParameters, this, javaEnvWithEcj()));
  }

  @Override
  public boolean hasTypeParameters() {
    var typeParams = m_astNode.typeParameters;
    return typeParams != null && typeParams.length > 0;
  }

  @Override
  public CompilationUnitSpi getCompilationUnit() {
    return m_cu;
  }

  @Override
  public TypeSpi getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> javaEnvWithEcj().getSource(m_cu, m_astNode.declarationSourceStart, m_astNode.declarationSourceEnd));
  }

  @Override
  public ISourceRange getSourceOfStaticInitializer() {
    return m_staticInitSource.computeIfAbsentAndGet(() -> {
      var fields = m_astNode.fields;
      if (fields == null) {
        return null;
      }
      return Arrays.stream(fields)
          .filter(fieldDecl -> fieldDecl.type == null && fieldDecl.name == null)
          .findFirst()
          .map(fieldDecl -> javaEnvWithEcj().getSource(m_cu, fieldDecl.declarationSourceStart, fieldDecl.declarationSourceEnd))
          .orElse(null);
    });
  }

  @Override
  public ISourceRange getJavaDoc() {
    return m_javaDocSource.computeIfAbsentAndGet(() -> {
      var doc = m_astNode.javadoc;
      if (doc != null) {
        return javaEnvWithEcj().getSource(m_cu, doc.sourceStart, doc.sourceEnd);
      }
      return null;
    });
  }

  @Override
  public boolean isWildcardType() {
    return false;
  }
}
