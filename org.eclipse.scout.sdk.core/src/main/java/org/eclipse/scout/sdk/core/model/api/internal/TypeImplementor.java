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
package org.eclipse.scout.sdk.core.model.api.internal;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.TypeNames;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IPackage;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.model.sugar.AnnotationQuery;
import org.eclipse.scout.sdk.core.model.sugar.FieldQuery;
import org.eclipse.scout.sdk.core.model.sugar.MethodQuery;
import org.eclipse.scout.sdk.core.model.sugar.SuperTypeQuery;
import org.eclipse.scout.sdk.core.model.sugar.TypeQuery;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;

public class TypeImplementor extends AbstractMemberImplementor<TypeSpi>implements IType {
  private String m_signature;

  public TypeImplementor(TypeSpi spi) {
    super(spi);
  }

  @Override
  public String getSimpleName() {
    return m_spi.getElementName();
  }

  @Override
  public boolean isArray() {
    return m_spi.isArray();
  }

  @Override
  public int getArrayDimension() {
    return m_spi.getArrayDimension();
  }

  @Override
  public IType getLeafComponentType() {
    return WrapperUtils.wrapType(m_spi.getLeafComponentType());
  }

  @Override
  public boolean isPrimitive() {
    return m_spi.isPrimitive();
  }

  @Override
  public boolean isAnonymous() {
    return m_spi.isAnonymous();
  }

  @Override
  public String getName() {
    return m_spi.getName();
  }

  @Override
  public IPackage getPackage() {
    return m_spi.getPackage().wrap();
  }

  @Override
  public List<IField> getFields() {
    return new WrappedList<>(m_spi.getFields());
  }

  @Override
  public List<IMethod> getMethods() {
    return new WrappedList<>(m_spi.getMethods());
  }

  @Override
  public List<IType> getTypes() {
    return new WrappedList<>(m_spi.getTypes());
  }

  @Override
  public IType getSuperClass() {
    return WrapperUtils.wrapType(m_spi.getSuperClass());
  }

  @Override
  public List<IType> getSuperInterfaces() {
    return new WrappedList<>(m_spi.getSuperInterfaces());
  }

  @Override
  public List<IType> getTypeArguments() {
    return new WrappedList<>(m_spi.getTypeArguments());
  }

  @Override
  public IType getOriginalType() {
    return m_spi.getOriginalType().wrap();
  }

  @Override
  public ICompilationUnit getCompilationUnit() {
    return m_spi.getCompilationUnit().wrap();
  }

  @Override
  public boolean isWildcardType() {
    return m_spi.isWildcardType();
  }

  @Override
  public ISourceRange getSourceOfStaticInitializer() {
    return m_spi.getSourceOfStaticInitializer();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    JavaModelPrinter.print(this, sb);
    return sb.toString();
  }

  @Override
  public void internalSetSpi(JavaElementSpi spi) {
    super.internalSetSpi(spi);
    m_signature = null;
  }

  //additional convenience methods

  @Override
  public boolean isVoid() {
    return getName().equals(TypeNames._void);
  }

  @Override
  public boolean isInterface() {
    return Flags.isInterface(getFlags());
  }

  @Override
  public String getSignature() {
    if (m_signature == null) {
      StringBuilder builder = new StringBuilder();
      buildSignatureRec(this, builder);
      int len = builder.length();
      char[] expr = new char[len];
      builder.getChars(0, len, expr, 0);
      m_signature = Signature.createTypeSignature(expr, true);
    }
    return m_signature;
  }

  protected static void buildSignatureRec(IType type, StringBuilder builder) {
    if (type.isArray()) {
      buildSignatureRec(type.getLeafComponentType(), builder);
      for (int i = 0; i < type.getArrayDimension(); i++) {
        builder.append(ISignatureConstants.C_ARRAY);
        builder.append(Signature.C_ARRAY_END);
      }
      return;
    }
    String name = type.getName();
    boolean isWildCardOnly = name == null; // name may be null for wildcard only types (<?>)
    if (type.isWildcardType()) {
      builder.append('?');
      if (isWildCardOnly) {
        return; // no further processing needed.
      }
      builder.append(" extends ");
    }
    if (!isWildCardOnly) {
      builder.append(name);
    }

    // generics
    List<IType> typeArgs = type.getTypeArguments();
    if (typeArgs.size() > 0) {
      builder.append(ISignatureConstants.C_GENERIC_START);
      buildSignatureRec(typeArgs.get(0), builder);
      for (int i = 1; i < typeArgs.size(); i++) {
        builder.append(Signature.C_COMMA);
        buildSignatureRec(typeArgs.get(i), builder);
      }
      builder.append(ISignatureConstants.C_GENERIC_END);
    }
  }

  @Override
  public AnnotationQuery<IAnnotation> annotations() {
    return new AnnotationQuery<>(this, this);
  }

  @Override
  public SuperTypeQuery superTypes() {
    return new SuperTypeQuery(this);
  }

  @Override
  public TypeQuery innerTypes() {
    return new TypeQuery(getTypes());
  }

  @Override
  public MethodQuery methods() {
    return new MethodQuery(this);
  }

  @Override
  public FieldQuery fields() {
    return new FieldQuery(this);
  }

  @Override
  public boolean isInstanceOf(String queryType) {
    return superTypes().withName(queryType).exists();
  }

  private static final Pattern PRIMITIVE_TYPE_ASSIGNABLE_PAT =
      Pattern.compile("("
          + "char=(char|Character)|byte=(byte|Byte)|short=(short|Short)|int=(int|Integer)|long=(long|Long)"
          + "|float=(float|Float)|double=(double|Double)|Character=(char|Character)|Byte=(byte|Byte)"
          + "|Short=(short|Short)|Integer=(int|Integer)|Long=(long|Long)|Float=(float|Float)|Double=(double|Double)"
          + ")");

  @Override
  public boolean isAssignableFrom(IType specificClass) {
    if ((this.isPrimitive() || specificClass.isPrimitive())) {
      return PRIMITIVE_TYPE_ASSIGNABLE_PAT.matcher(this.getSimpleName() + "=" + specificClass.getSimpleName()).matches();
    }
    if ((this.isArray() || specificClass.isArray())) {
      return this.getName().equals(specificClass.getName());
    }
    return specificClass.superTypes().withName(this.getName()).exists();
  }

  @Override
  public IType boxPrimitiveType() {
    if (!isPrimitive()) {
      return this;
    }
    IJavaEnvironment env = getJavaEnvironment();
    switch (getName()) {
      case TypeNames._boolean: {
        return env.findType(TypeNames.java_lang_Boolean);
      }
      case TypeNames._char: {
        return env.findType(TypeNames.java_lang_Character);
      }
      case TypeNames._byte: {
        return env.findType(TypeNames.java_lang_Byte);
      }
      case TypeNames._short: {
        return env.findType(TypeNames.java_lang_Short);
      }
      case TypeNames._int: {
        return env.findType(TypeNames.java_lang_Integer);
      }
      case TypeNames._long: {
        return env.findType(TypeNames.java_lang_Long);
      }
      case TypeNames._float: {
        return env.findType(TypeNames.java_lang_Float);
      }
      case TypeNames._double: {
        return env.findType(TypeNames.java_lang_Double);
      }
      case TypeNames._void: {
        return env.findType(TypeNames.java_lang_Void);
      }
      default:
        throw new IllegalStateException("unknown primitive type: " + getName());
    }
  }

  @Override
  public IType unboxPrimitiveType() {
    String s = getName();
    if (s.length() > 19) {
      return null;
    }
    IJavaEnvironment env = getJavaEnvironment();
    switch (s) {
      case TypeNames.java_lang_Boolean: {
        return env.findType(TypeNames._boolean);
      }
      case TypeNames.java_lang_Character: {
        return env.findType(TypeNames._char);
      }
      case TypeNames.java_lang_Byte: {
        return env.findType(TypeNames._byte);
      }
      case TypeNames.java_lang_Short: {
        return env.findType(TypeNames._short);
      }
      case TypeNames.java_lang_Integer: {
        return env.findType(TypeNames._int);
      }
      case TypeNames.java_lang_Long: {
        return env.findType(TypeNames._long);
      }
      case TypeNames.java_lang_Float: {
        return env.findType(TypeNames._float);
      }
      case TypeNames.java_lang_Double: {
        return env.findType(TypeNames._double);
      }
      case TypeNames.java_lang_Void: {
        return env.findType(TypeNames._void);
      }
      default:
        return null;
    }
  }

}
