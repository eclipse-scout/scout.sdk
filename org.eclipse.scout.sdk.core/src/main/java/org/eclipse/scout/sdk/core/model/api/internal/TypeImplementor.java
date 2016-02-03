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

import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
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
import org.eclipse.scout.sdk.core.util.CoreUtils;

public class TypeImplementor extends AbstractMemberImplementor<TypeSpi> implements IType {
  private String m_signature;

  public TypeImplementor(TypeSpi spi) {
    super(spi);
  }

  @Override
  public boolean isArray() {
    return m_spi.isArray();
  }

  @Override
  public int arrayDimension() {
    return m_spi.getArrayDimension();
  }

  @Override
  public IType leafComponentType() {
    return JavaEnvironmentImplementor.wrapType(m_spi.getLeafComponentType());
  }

  @Override
  public boolean isPrimitive() {
    return m_spi.isPrimitive();
  }

  @Override
  public boolean isParameterType() {
    return m_spi.isAnonymous();
  }

  @Override
  public String name() {
    return m_spi.getName();
  }

  @Override
  public IPackage containingPackage() {
    return m_spi.getPackage().wrap();
  }

  @Override
  public IType superClass() {
    return JavaEnvironmentImplementor.wrapType(m_spi.getSuperClass());
  }

  @Override
  public List<IType> superInterfaces() {
    return new WrappedList<>(m_spi.getSuperInterfaces());
  }

  @Override
  public List<IType> typeArguments() {
    return new WrappedList<>(m_spi.getTypeArguments());
  }

  @Override
  public IType originalType() {
    return m_spi.getOriginalType().wrap();
  }

  @Override
  public ICompilationUnit compilationUnit() {
    return m_spi.getCompilationUnit().wrap();
  }

  @Override
  public boolean isWildcardType() {
    return m_spi.isWildcardType();
  }

  @Override
  public ISourceRange sourceOfStaticInitializer() {
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
    return name().equals(IJavaRuntimeTypes._void);
  }

  @Override
  public boolean isInterface() {
    return Flags.isInterface(flags());
  }

  @Override
  public String signature() {
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
      buildSignatureRec(type.leafComponentType(), builder);
      for (int i = 0; i < type.arrayDimension(); i++) {
        builder.append(ISignatureConstants.C_ARRAY);
        builder.append(Signature.C_ARRAY_END);
      }
      return;
    }
    String name = type.name();
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
    List<IType> typeArgs = type.typeArguments();
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
    return new AnnotationQuery<>(this, m_spi);
  }

  @Override
  public SuperTypeQuery superTypes() {
    return new SuperTypeQuery(this);
  }

  @Override
  public TypeQuery innerTypes() {
    return new TypeQuery(new WrappedList<IType>(m_spi.getTypes()));
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
    return superTypes().withName(queryType).existsAny();
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
      return PRIMITIVE_TYPE_ASSIGNABLE_PAT.matcher(this.elementName() + '=' + specificClass.elementName()).matches();
    }
    if ((this.isArray() || specificClass.isArray())) {
      return this.name().equals(specificClass.name());
    }
    return specificClass.superTypes().withName(this.name()).existsAny();
  }

  @Override
  public IType boxPrimitiveType() {
    if (!isPrimitive()) {
      return this;
    }
    String boxedFqn = CoreUtils.boxPrimitive(name());
    if (boxedFqn == null) {
      return this;
    }
    return javaEnvironment().findType(boxedFqn);
  }

  @Override
  public IType unboxPrimitiveType() {
    String s = name();
    if (s.length() > 19) {
      return null;
    }
    String unboxed = CoreUtils.unboxToPrimitive(s);
    if (unboxed == null) {
      return null;
    }
    return javaEnvironment().findType(unboxed);
  }

}
