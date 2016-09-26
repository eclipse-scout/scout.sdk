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
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
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

public class DeclarationTypeWithJdt extends AbstractTypeWithJdt {
  private final CompilationUnitSpi m_cu;
  private final DeclarationTypeWithJdt m_declaringType;
  private final TypeDeclaration m_astNode;
  private final String m_fqn;
  private final List<TypeSpi> m_typeArguments;

  private PackageSpi m_package;
  private String m_elementName;
  private TypeSpi m_superType;
  private List<TypeSpi> m_memberTypes;
  private List<TypeSpi> m_superInterfaces;
  private List<TypeParameterSpi> m_typeParameters;
  private List<DeclarationAnnotationWithJdt> m_annotations;
  private List<MethodSpi> m_methods;
  private List<FieldSpi> m_fields;
  private int m_flags;
  private ISourceRange m_source;
  private ISourceRange m_javaDocSource;
  private ISourceRange m_staticInitSource;

  DeclarationTypeWithJdt(JavaEnvironmentWithJdt env, CompilationUnitSpi cu, DeclarationTypeWithJdt declaringType, TypeDeclaration astNode) {
    super(env);
    m_cu = Validate.notNull(cu);
    m_declaringType = declaringType;
    m_astNode = Validate.notNull(astNode);
    m_typeArguments = Collections.emptyList(); // no arguments for declarations
    m_fqn = calcFullyQualifiedName(astNode);
    m_flags = -1; // mark as uninitialized
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    BindingTypeWithJdt newSpi = (BindingTypeWithJdt) newEnv.findType(getName());
    if (newSpi != null) {
      for (TypeSpi declType : newSpi.getCompilationUnit().getTypes()) {
        if (declType.getName().equals(this.getName())) {
          return declType;
        }
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
  public boolean isArray() {
    return false;
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
    return m_fqn;
  }

  @Override
  public String getElementName() {
    if (m_elementName == null) {
      m_elementName = new String(m_astNode.name);
    }
    return m_elementName;
  }

  @Override
  public PackageSpi getPackage() {
    if (m_package == null) {
      char[] qualifiedPackageName = m_astNode.binding.qualifiedPackageName();
      if (qualifiedPackageName != null && qualifiedPackageName.length > 0) {
        m_package = m_env.createPackage(new String(qualifiedPackageName));
      }
      else {
        m_package = m_env.createDefaultPackage();
      }
    }
    return m_package;
  }

  @Override
  public List<DeclarationAnnotationWithJdt> getAnnotations() {
    if (m_annotations != null) {
      return m_annotations;
    }
    m_annotations = SpiWithJdtUtils.createDeclarationAnnotations(m_env, this, m_astNode.annotations);
    return m_annotations;
  }

  @Override
  public List<FieldSpi> getFields() {
    if (m_fields != null) {
      return m_fields;
    }

    FieldDeclaration[] fields = m_astNode.fields;
    if (fields == null || fields.length < 1) {
      m_fields = Collections.emptyList();
    }
    else {
      List<FieldSpi> result = new ArrayList<>(fields.length);
      for (FieldDeclaration fd : fields) {
        result.add(m_env.createDeclarationField(this, fd));
      }
      m_fields = Collections.unmodifiableList(result);
    }
    return m_fields;
  }

  @Override
  public List<MethodSpi> getMethods() {
    if (m_methods != null) {
      return m_methods;
    }

    AbstractMethodDeclaration[] methods = m_astNode.methods;
    if (methods == null || methods.length < 1) {
      m_methods = Collections.emptyList();
    }
    else {
      List<MethodSpi> result = new ArrayList<>(methods.length);
      for (AbstractMethodDeclaration a : methods) {
        if (a.bodyStart <= 0) { // skip compiler generated methods
          continue;
        }
        if ("<init>".equals(new String(a.selector))) {
          continue;
        }
        result.add(m_env.createDeclarationMethod(this, a));
      }
      m_methods = Collections.unmodifiableList(result);
    }
    return m_methods;
  }

  @Override
  public List<TypeSpi> getTypes() {
    if (m_memberTypes != null) {
      return m_memberTypes;
    }

    TypeDeclaration[] memberTypes = m_astNode.memberTypes;
    if (memberTypes == null || memberTypes.length < 1) {
      m_memberTypes = Collections.emptyList();
    }
    else {
      List<TypeSpi> result = new ArrayList<>(memberTypes.length);
      for (TypeDeclaration d : memberTypes) {
        result.add(new DeclarationTypeWithJdt(m_env, m_cu, this, d));
      }
      m_memberTypes = Collections.unmodifiableList(result);
    }
    return m_memberTypes;
  }

  @Override
  public TypeSpi getSuperClass() {
    if (m_superType == null) {
      if (m_astNode.superclass == null) {
        m_superType = null;
      }
      else {
        TypeBinding tb = m_astNode.superclass.resolvedType;
        if (tb == null) {
          m_superType = null;
        }
        else {
          m_superType = SpiWithJdtUtils.bindingToType(m_env, tb);
        }
      }
    }
    return m_superType;
  }

  @Override
  public List<TypeSpi> getSuperInterfaces() {
    if (m_superInterfaces != null) {
      return m_superInterfaces;
    }

    TypeReference[] refs = m_astNode.superInterfaces;
    if (refs == null || refs.length < 1) {
      m_superInterfaces = Collections.emptyList();
    }
    else {
      List<TypeSpi> result = new ArrayList<>(refs.length);
      for (TypeReference r : refs) {
        TypeBinding b = r.resolvedType;
        if (b != null) {
          TypeSpi t = SpiWithJdtUtils.bindingToType(m_env, b);
          if (t != null) {
            result.add(t);
          }
        }
      }
      m_superInterfaces = Collections.unmodifiableList(result);
    }
    return m_superInterfaces;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = SpiWithJdtUtils.getTypeFlags(m_astNode.modifiers, m_astNode.allocation, SpiWithJdtUtils.hasDeprecatedAnnotation(m_astNode.annotations));
    }
    return m_flags;
  }

  @Override
  public List<TypeSpi> getTypeArguments() {
    return m_typeArguments;
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    if (m_typeParameters != null) {
      return m_typeParameters;
    }

    TypeParameter[] typeParams = m_astNode.typeParameters;
    if (typeParams == null || typeParams.length < 1) {
      m_typeParameters = Collections.emptyList();
    }
    else {
      List<TypeParameterSpi> result = new ArrayList<>(typeParams.length);
      for (int i = 0; i < typeParams.length; i++) {
        result.add(m_env.createDeclarationTypeParameter(this, typeParams[i], i));
      }
      m_typeParameters = Collections.unmodifiableList(result);
    }
    return m_typeParameters;
  }

  @Override
  public boolean hasTypeParameters() {
    TypeParameter[] typeParams = m_astNode.typeParameters;
    return typeParams != null && typeParams.length > 0;
  }

  @Override
  public DeclarationTypeWithJdt getOriginalType() {
    return this;
  }

  @Override
  public CompilationUnitSpi getCompilationUnit() {
    return m_cu;
  }

  @Override
  public TypeSpi getDeclaringType() {
    return m_declaringType;
  }

  private static String calcFullyQualifiedName(TypeDeclaration td) {
    StringBuilder sb = new StringBuilder();

    char[] qualifiedPackageName = td.binding.qualifiedPackageName();
    if (qualifiedPackageName != null && qualifiedPackageName.length > 0) {
      sb.append(qualifiedPackageName);
      sb.append('.');
    }

    // collect declaring types
    Deque<char[]> namesBottomUp = new LinkedList<>();
    TypeDeclaration declaringType = td;
    while (declaringType != null) {
      namesBottomUp.add(declaringType.name);
      declaringType = declaringType.enclosingType;
    }

    Iterator<char[]> namesTopDown = namesBottomUp.descendingIterator();
    sb.append(namesTopDown.next()); // there must be at least one type name
    while (namesTopDown.hasNext()) {
      sb.append('$').append(namesTopDown.next());
    }
    return sb.toString();
  }

  @Override
  public ISourceRange getSource() {
    if (m_source == null) {
      TypeDeclaration decl = m_astNode;
      m_source = m_env.getSource(m_cu, decl.declarationSourceStart, decl.declarationSourceEnd);
    }
    return m_source;
  }

  @Override
  public ISourceRange getSourceOfStaticInitializer() {
    if (m_staticInitSource == null) {
      TypeDeclaration decl = m_astNode;
      for (FieldDeclaration fieldDecl : decl.fields) {
        if (fieldDecl.type == null && fieldDecl.name == null) {
          m_staticInitSource = m_env.getSource(m_cu, fieldDecl.declarationSourceStart, fieldDecl.declarationSourceEnd);
          break;
        }
      }
      if (m_staticInitSource == null) {
        m_staticInitSource = ISourceRange.NO_SOURCE;
      }
    }
    return m_staticInitSource;
  }

  @Override
  public ISourceRange getJavaDoc() {
    if (m_javaDocSource == null) {
      TypeDeclaration decl = m_astNode;
      Javadoc doc = decl.javadoc;
      if (doc != null) {
        m_javaDocSource = m_env.getSource(m_cu, doc.sourceStart, doc.sourceEnd);
      }
      else {
        m_javaDocSource = ISourceRange.NO_SOURCE;
      }
    }
    return m_javaDocSource;
  }

  @Override
  public boolean isWildcardType() {
    return false;
  }
}
