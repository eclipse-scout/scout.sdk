/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.bindingToType;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.bindingsToTypes;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.createDeclarationAnnotations;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.createSourceRange;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.getTypeFlags;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.hasDeprecatedAnnotation;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.qualifiedNameOf;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.toTypeParameterSpi;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.internal.TypeImplementor;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

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

  protected DeclarationTypeWithEcj(AbstractJavaEnvironment env, CompilationUnitSpi cu, DeclarationTypeWithEcj declaringType, TypeDeclaration astNode) {
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
  public TypeSpi internalFindNewElement() {
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
    return m_fqn.computeIfAbsentAndGet(() -> qualifiedNameOf(isAnonymous() ? CharOperation.NO_CHAR : m_astNode.binding.qualifiedPackageName(), m_astNode.binding.qualifiedSourceName()));
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
    return m_annotations.computeIfAbsentAndGet(() -> createDeclarationAnnotations(javaEnvWithEcj(), this, m_astNode.annotations));
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
          .map(d -> javaEnvWithEcj().createDeclarationType(m_cu, this, d))
          .collect(toList());
    });
  }

  protected static TypeBinding getSuperClassBinding(DeclarationTypeWithEcj decl) {
    if (decl.m_astNode.superclass == null) {
      return null;
    }
    return decl.m_astNode.superclass.resolvedType;
  }

  @Override
  public TypeSpi getSuperClass() {
    return m_superType.computeIfAbsentAndGet(() -> bindingToType(javaEnvWithEcj(), getSuperClassBinding(this), () -> withNewElement(DeclarationTypeWithEcj::getSuperClassBinding)));
  }

  protected static TypeBinding[] getSuperInterfaceBindings(DeclarationTypeWithEcj d) {
    var interfaces = d.m_astNode.superInterfaces;
    if (interfaces == null) {
      return Binding.NO_TYPES;
    }
    return Arrays.stream(interfaces)
        .map(i -> i.resolvedType)
        .filter(Objects::nonNull)
        .toArray(TypeBinding[]::new);
  }

  @Override
  public List<TypeSpi> getSuperInterfaces() {
    return m_superInterfaces.computeIfAbsentAndGet(() -> bindingsToTypes(javaEnvWithEcj(), getSuperInterfaceBindings(this), () -> withNewElement(DeclarationTypeWithEcj::getSuperInterfaceBindings)));
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = getTypeFlags(m_astNode.modifiers, m_astNode.allocation, hasDeprecatedAnnotation(getAnnotations()));
    }
    return m_flags;
  }

  @Override
  public List<TypeSpi> getTypeArguments() {
    return m_typeArguments;
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    return m_typeParameters.computeIfAbsentAndGet(() -> toTypeParameterSpi(m_astNode.typeParameters, this, javaEnvWithEcj()));
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
          .findAny()
          .map(fieldDecl -> javaEnvWithEcj().getSource(m_cu, fieldDecl.declarationSourceStart, fieldDecl.declarationSourceEnd))
          .orElse(null);
    });
  }

  @Override
  public ISourceRange getJavaDoc() {
    return m_javaDocSource.computeIfAbsentAndGet(() -> createSourceRange(m_astNode.javadoc, m_cu, javaEnvWithEcj()));
  }

  @Override
  public boolean isWildcardType() {
    return false;
  }
}
