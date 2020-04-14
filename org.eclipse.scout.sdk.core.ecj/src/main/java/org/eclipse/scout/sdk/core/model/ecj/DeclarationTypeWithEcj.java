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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
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
    TypeSpi newSpi = getJavaEnvironment().findType(getName());
    if (newSpi == null) {
      return null;
    }
    for (TypeSpi declType : newSpi.getCompilationUnit().getTypes()) {
      if (declType.getName().equals(getName())) {
        return declType;
      }
    }
    return null;
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
      StringBuilder sb = new StringBuilder(128);

      char[] qualifiedPackageName = m_astNode.binding.qualifiedPackageName();
      if (qualifiedPackageName != null && qualifiedPackageName.length > 0) {
        sb.append(qualifiedPackageName);
        sb.append(JavaTypes.C_DOT);
      }

      // collect declaring types
      Deque<char[]> namesBottomUp = new ArrayDeque<>();
      TypeDeclaration declaringType = m_astNode;
      while (declaringType != null) {
        namesBottomUp.add(declaringType.name);
        declaringType = declaringType.enclosingType;
      }

      Iterator<char[]> namesTopDown = namesBottomUp.descendingIterator();
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
      char[] qualifiedPackageName = m_astNode.binding.qualifiedPackageName();
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
      FieldDeclaration[] fields = m_astNode.fields;
      if (fields == null || fields.length < 1) {
        return emptyList();
      }

      List<FieldSpi> result = new ArrayList<>(fields.length);
      for (FieldDeclaration fd : fields) {
        result.add(javaEnvWithEcj().createDeclarationField(this, fd));
      }
      return result;
    });
  }

  @Override
  public List<MethodSpi> getMethods() {
    return m_methods.computeIfAbsentAndGet(() -> {
      AbstractMethodDeclaration[] methods = m_astNode.methods;
      if (methods == null || methods.length < 1) {
        return emptyList();
      }

      List<MethodSpi> result = new ArrayList<>(methods.length);
      for (AbstractMethodDeclaration a : methods) {
        if (a.bodyStart <= 0) { // skip compiler generated methods
          continue;
        }
        if (Arrays.equals(TypeConstants.INIT, a.selector)) {
          continue;
        }
        result.add(javaEnvWithEcj().createDeclarationMethod(this, a));
      }
      return result;
    });
  }

  @Override
  public List<TypeSpi> getTypes() {
    return m_memberTypes.computeIfAbsentAndGet(() -> {
      TypeDeclaration[] memberTypes = m_astNode.memberTypes;
      if (memberTypes == null || memberTypes.length < 1) {
        return emptyList();
      }
      List<TypeSpi> result = new ArrayList<>(memberTypes.length);
      for (TypeDeclaration d : memberTypes) {
        result.add(new DeclarationTypeWithEcj(javaEnvWithEcj(), m_cu, this, d));
      }
      return result;
    });
  }

  @Override
  public TypeSpi getSuperClass() {
    return m_superType.computeIfAbsentAndGet(() -> {
      if (m_astNode.superclass == null) {
        return null;
      }
      TypeBinding tb = m_astNode.superclass.resolvedType;
      if (tb == null) {
        return null;
      }
      return SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), tb);
    });
  }

  @Override
  public List<TypeSpi> getSuperInterfaces() {
    return m_superInterfaces.computeIfAbsentAndGet(() -> {
      TypeReference[] refs = m_astNode.superInterfaces;
      if (refs == null || refs.length < 1) {
        return emptyList();
      }
      List<TypeSpi> result = new ArrayList<>(refs.length);
      for (TypeReference r : refs) {
        TypeBinding b = r.resolvedType;
        if (b != null) {
          TypeSpi t = SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), b);
          if (t != null) {
            result.add(t);
          }
        }
      }
      return result;
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
    TypeParameter[] typeParams = m_astNode.typeParameters;
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
      FieldDeclaration[] fields = m_astNode.fields;
      if (fields != null) {
        for (FieldDeclaration fieldDecl : fields) {
          if (fieldDecl.type == null && fieldDecl.name == null) {
            return javaEnvWithEcj().getSource(m_cu, fieldDecl.declarationSourceStart, fieldDecl.declarationSourceEnd);
          }
        }
      }
      return null;
    });
  }

  @Override
  public ISourceRange getJavaDoc() {
    return m_javaDocSource.computeIfAbsentAndGet(() -> {
      Javadoc doc = m_astNode.javadoc;
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
