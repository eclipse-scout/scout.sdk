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
package org.eclipse.scout.sdk.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;

/**
 *
 */
public class BindingType implements IType {

  private final ReferenceBinding m_tb;
  private final char[] m_id;
  private final int m_hash;
  private final int m_arrayDimension;
  private final boolean m_isWildcard;
  private final ILookupEnvironment m_env;

  private IPackage m_package;
  private IType m_declaringType;
  private IType m_superClass;
  private String m_simpleName;
  private String m_name;
  private List<IType> m_memberTypes;
  private List<IType> m_superInterfaces;
  private List<ITypeParameter> m_typeParameters;
  private List<IType> m_typeArguments;
  private int m_flags;
  private List<IAnnotation> m_annotations;
  private List<IMethod> m_methods;
  private List<IField> m_fields;
  private boolean m_sourceTypeInitialized;
  private ICompilationUnit m_unit;

  public BindingType(ReferenceBinding tb, IType declaringType, int arrayDim, boolean isWildcard, ILookupEnvironment lookupEnvironment) {
    if (tb instanceof MissingTypeBinding || tb instanceof ProblemReferenceBinding) {
      // type not found -> parsing error. do not silently continue
      throw new IllegalArgumentException(tb.toString());
    }
    m_env = lookupEnvironment;
    m_tb = Validate.notNull(tb);
    m_isWildcard = isWildcard;
    m_arrayDimension = arrayDim;
    m_declaringType = declaringType;
    m_id = CharOperation.concatWith(m_tb.compoundName, getArrUniqueKey(arrayDim), '.');
    m_hash = Arrays.hashCode(m_id);
    m_flags = -1; // mark as uninitialized;
    m_sourceTypeInitialized = false;
  }

  private static char[] getArrUniqueKey(int arrayDim) {
    char[] arr = new char[arrayDim];
    Arrays.fill(arr, ISignatureConstants.C_ARRAY);
    return arr;
  }

  @Override
  public boolean isArray() {
    return m_arrayDimension > 0;
  }

  @Override
  public int getArrayDimension() {
    return m_arrayDimension;
  }

  @Override
  public ICompilationUnit getCompilationUnit() {
    if (m_unit == null && m_tb instanceof SourceTypeBinding) { // no compilation unit for binary bindings
      CompilationUnitScope icuScope = ((SourceTypeBinding) m_tb).scope.compilationUnitScope();
      if (icuScope != null) {
        m_unit = new CompilationUnit(icuScope.referenceContext, getLookupEnvironment());
      }
    }
    return m_unit;
  }

  @Override
  public String getSimpleName() {
    if (m_simpleName == null) {
      m_simpleName = new String(m_tb.sourceName);
    }
    return m_simpleName;
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public IPackage getPackage() {
    if (m_package == null) {
      char[] qualifiedPackageName = m_tb.qualifiedPackageName();
      if (qualifiedPackageName == null || qualifiedPackageName.length < 1) {
        m_package = IPackage.DEFAULT_PACKAGE;
      }
      else {
        m_package = new Package(new String(qualifiedPackageName));
      }
    }
    return m_package;
  }

  @Override
  public String getName() {
    if (m_name == null) {
      if (m_tb.compoundName == null) {
        // for typevariable bindings
        m_name = new String(m_tb.sourceName);
      }
      else {
        m_name = CharOperation.toString(m_tb.compoundName);
      }
    }
    return m_name;
  }

  @Override
  public List<IMethod> getMethods() {
    if (m_methods == null) {
      ensureTypeBuild();
      ReferenceBinding actualType = m_tb.actualType();
      MethodBinding[] methods = null;
      if (actualType != null) {
        methods = actualType.methods();
      }
      else {
        methods = m_tb.methods();
      }
      if (methods == null || methods.length < 1) {
        m_methods = new ArrayList<>(0);
      }
      else {
        List<IMethod> result = new ArrayList<>(methods.length);
        for (MethodBinding a : methods) {
          if (!a.isBridge() && !a.isSynthetic() && !a.isDefaultAbstract()) {
            result.add(new BindingMethod(a, this));
          }
        }
        m_methods = result;
      }
    }
    return m_methods;
  }

  @Override
  public IType getDeclaringType() {
    if (m_declaringType == null) {
      ReferenceBinding enclosingType = null;
      if (m_tb.actualType() == null) {
        enclosingType = m_tb.enclosingType();
      }
      else {
        enclosingType = m_tb.actualType().enclosingType();
      }
      if (enclosingType != null) {
        m_declaringType = JavaModelUtils.bindingToType(enclosingType, m_env);
      }
    }
    return m_declaringType;
  }

  @Override
  public IType getSuperClass() {
    if (m_superClass == null) {
      ensureTypeBuild();
      ReferenceBinding superclass = m_tb.superclass();
      if (superclass != null) {
        m_superClass = JavaModelUtils.bindingToType(superclass, m_env);
      }
    }
    return m_superClass;
  }

  @Override
  public List<IField> getFields() {
    if (m_fields == null) {
      ensureTypeBuild();
      FieldBinding[] fields = m_tb.fields();
      if (fields == null || fields.length < 1) {
        m_fields = new ArrayList<>(0);
      }
      else {
        List<IField> result = new ArrayList<>(fields.length);
        for (FieldBinding fd : fields) {
          if (!fd.isSynthetic()) {
            result.add(new BindingField(fd, this));
          }
        }
        m_fields = result;
      }
    }
    return m_fields;
  }

  @Override
  public List<IType> getTypes() {
    if (m_memberTypes == null) {
      ensureTypeBuild();
      ReferenceBinding[] memberTypes = m_tb.memberTypes();
      if (memberTypes == null || memberTypes.length < 1) {
        m_memberTypes = new ArrayList<>(0);
      }
      else {
        List<IType> result = new ArrayList<>(memberTypes.length);
        for (ReferenceBinding d : memberTypes) {
          IType t = JavaModelUtils.bindingToType(d, m_env, this);
          if (t != null) {
            result.add(t);
          }
        }
        m_memberTypes = result;
      }
    }
    return m_memberTypes;
  }

  @Override
  public List<IType> getSuperInterfaces() {
    if (m_superInterfaces == null) {
      ensureTypeBuild();
      ReferenceBinding[] superInterfaces = m_tb.superInterfaces();
      if (superInterfaces == null || superInterfaces.length < 1) {
        m_superInterfaces = new ArrayList<>(0);
      }
      else {
        List<IType> result = new ArrayList<>(superInterfaces.length);
        for (ReferenceBinding b : superInterfaces) {
          IType t = JavaModelUtils.bindingToType(b, m_env);
          if (t != null) {
            result.add(t);
          }
        }
        m_superInterfaces = result;
      }
    }
    return m_superInterfaces;
  }

  @Override
  public boolean isWildcardType() {
    return m_isWildcard;
  }

  @Override
  public List<IType> getTypeArguments() {
    if (m_typeArguments == null) {
      ensureTypeBuild();
      if (m_tb instanceof ParameterizedTypeBinding) {
        ParameterizedTypeBinding ptb = (ParameterizedTypeBinding) m_tb;
        TypeBinding[] arguments = ptb.arguments;
        if (arguments != null && arguments.length > 0) {
          List<IType> result = new ArrayList<>(arguments.length);
          for (TypeBinding b : arguments) {
            IType type = JavaModelUtils.bindingToType(b, m_env);
            if (type != null) {
              result.add(type);
            }
          }
          m_typeArguments = result;
        }
      }

      if (m_typeArguments == null) {
        m_typeArguments = new ArrayList<>(0);
      }
    }
    return m_typeArguments;
  }

  @Override
  public List<ITypeParameter> getTypeParameters() {
    if (m_typeParameters == null) {
      ensureTypeBuild();
      if (hasTypeParameters()) {
        TypeVariableBinding[] typeParams = getTypeVariables();

        List<ITypeParameter> result = new ArrayList<>(typeParams.length);
        for (TypeVariableBinding param : typeParams) {
          result.add(new BindingTypeParameter(param, this));
        }
        m_typeParameters = result;
      }
      else {
        m_typeParameters = new ArrayList<>(0);
      }
    }
    return m_typeParameters;
  }

  @Override
  public List<IAnnotation> getAnnotations() {
    if (m_annotations == null) {
      ensureTypeBuild();
      ReferenceBinding actualType = m_tb.actualType();
      if (actualType == null) {
        m_annotations = JavaModelUtils.annotationBindingsToIAnnotations(m_tb.getAnnotations(), this, m_env);
      }
      else {
        m_annotations = JavaModelUtils.annotationBindingsToIAnnotations(actualType.getAnnotations(), this, m_env);
      }
    }
    return m_annotations;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = JavaModelUtils.getTypeFlags(m_tb.modifiers, null, JavaModelUtils.hasDeprecatedAnnotation(m_tb.getAnnotations()));
    }
    return m_flags;
  }

  protected TypeVariableBinding[] getTypeVariables() {
    if (m_tb.actualType() == null) {
      return m_tb.typeVariables();
    }
    return m_tb.actualType().typeVariables();
  }

  protected void ensureTypeBuild() {
    if (m_sourceTypeInitialized) {
      return;
    }

    if (m_tb instanceof SourceTypeBinding) {
      // source bindings are not automatically fully built. lazily complete all bindings when needed.
      SourceTypeBinding stb = (SourceTypeBinding) m_tb;
      CompilationUnitScope scope = stb.scope.compilationUnitScope();
      if (scope != null) {
        scope.environment.completeTypeBindings(scope.referenceContext, true);
      }
    }
    m_sourceTypeInitialized = true;
  }

  @Override
  public boolean hasTypeParameters() {
    ensureTypeBuild();
    TypeVariableBinding[] typeVariables = getTypeVariables();
    return typeVariables != null && typeVariables.length > 0;
  }

  @Override
  public boolean isAnonymous() {
    return m_tb.compoundName == null || m_tb.compoundName.length < 1;
  }

  @Override
  public ILookupEnvironment getLookupEnvironment() {
    return m_env;
  }

  @Override
  public int hashCode() {
    return m_hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BindingType)) {
      return false;
    }
    BindingType other = (BindingType) obj;
    if (!Arrays.equals(m_id, other.m_id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    ModelPrinter.print(this, sb);
    return sb.toString();
  }
}
