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

import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer.transformType;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IPackage;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.query.AnnotationQuery;
import org.eclipse.scout.sdk.core.model.api.query.FieldQuery;
import org.eclipse.scout.sdk.core.model.api.query.HierarchyInnerTypeQuery;
import org.eclipse.scout.sdk.core.model.api.query.MethodQuery;
import org.eclipse.scout.sdk.core.model.api.query.SuperTypeQuery;
import org.eclipse.scout.sdk.core.model.api.spliterator.WrappingSpliterator;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.JavaTypes;

public class TypeImplementor extends AbstractMemberImplementor<TypeSpi> implements IType {
  private static final Pattern PRIMITIVE_TYPE_ASSIGNABLE_PAT =
      Pattern.compile("(?:"
          + "char=(?:char|Character)|byte=(?:byte|Byte)|short=(?:short|Short)|int=(?:int|Integer)|long=(?:long|Long)"
          + "|float=(?:float|Float)|double=(?:double|Double)|Character=(?:char|Character)|Byte=(?:byte|Byte)"
          + "|Short=(?:short|Short)|Integer=(?:int|Integer)|Long=(?:long|Long)|Float=(?:float|Float)|Double=(?:double|Double)"
          + ')');
  private String m_reference;
  private String m_referenceErasureOnly;

  public TypeImplementor(TypeSpi spi) {
    super(spi);
  }

  @Override
  public Optional<IType> declaringType() {
    return Optional.ofNullable(m_spi.getDeclaringType())
        .map(TypeSpi::wrap);
  }

  @Override
  public boolean isArray() {
    return m_spi.getArrayDimension() > 0;
  }

  @Override
  public int arrayDimension() {
    return m_spi.getArrayDimension();
  }

  @Override
  public Optional<IType> leafComponentType() {
    return Optional.ofNullable(m_spi.getLeafComponentType())
        .map(TypeSpi::wrap);
  }

  @Override
  public Stream<? extends IJavaElement> children() {
    Comparator<IJavaElement> c = Comparator.comparing(e -> e.source().map(ISourceRange::start).orElse(0));
    Stream<? extends IJavaElement> fieldsAndMethos = Stream.concat(fields().stream(), methods().stream());
    Stream<? extends IJavaElement> innerTypesAndTypeParams = Stream.concat(innerTypes().stream(), typeParameters());
    return Stream.concat(Stream.concat(innerTypesAndTypeParams, fieldsAndMethos), annotations().stream())
        .sorted(c);
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
  public Optional<IType> superClass() {
    return Optional.ofNullable(m_spi.getSuperClass())
        .map(TypeSpi::wrap);
  }

  @Override
  public IType requireSuperClass() {
    return superClass()
        .orElseThrow(() -> newFail("Type '{}' has no super class.", name()));
  }

  @Override
  public Stream<IType> superInterfaces() {
    return WrappingSpliterator.stream(m_spi.getSuperInterfaces());
  }

  @Override
  public Stream<IType> typeArguments() {
    return WrappingSpliterator.stream(m_spi.getTypeArguments());
  }

  @Override
  public Optional<ICompilationUnit> compilationUnit() {
    return Optional.ofNullable(m_spi.getCompilationUnit())
        .map(CompilationUnitSpi::wrap);
  }

  @Override
  public ICompilationUnit requireCompilationUnit() {
    return compilationUnit()
        .orElseThrow(() -> newFail("Type '{}' does not have a compilation unit.", name()));
  }

  @Override
  public boolean isWildcardType() {
    return m_spi.isWildcardType();
  }

  @Override
  public Optional<ISourceRange> sourceOfStaticInitializer() {
    return Optional.ofNullable(m_spi.getSourceOfStaticInitializer());
  }

  @Override
  public String qualifier() {
    return declaringType()
        .map(IType::name)
        .orElseGet(() -> compilationUnit()
            .map(ICompilationUnit::containingPackage)
            .map(IPackage::elementName)
            .orElse(""));
  }

  @Override
  public void internalSetSpi(TypeSpi spi) {
    super.internalSetSpi(spi);
    m_reference = null;
    m_referenceErasureOnly = null;
  }

  @Override
  public boolean isVoid() {
    return JavaTypes._void.equals(name());
  }

  @Override
  public boolean isInterface() {
    return Flags.isInterface(flags());
  }

  @Override
  public String reference() {
    return reference(false);
  }

  @Override
  public String reference(boolean erasureOnly) {
    return erasureOnly ? referenceErasureOnly() : referenceWithTypeArgs();
  }

  protected String referenceWithTypeArgs() {
    if (m_reference == null) {
      m_reference = buildReference(this, false);
    }
    return m_reference;
  }

  protected String referenceErasureOnly() {
    if (m_referenceErasureOnly == null) {
      m_referenceErasureOnly = buildReference(this, true);
    }
    return m_referenceErasureOnly;
  }

  protected static String buildReference(IType type, boolean erasureOnly) {
    StringBuilder builder = new StringBuilder(128);
    buildReferenceRec(type, erasureOnly, builder);
    return builder.toString();
  }

  protected static void buildReferenceRec(IType type, boolean erasureOnly, StringBuilder builder) {
    if (type.isArray()) {
      buildReferenceRec(type.leafComponentType().get(), erasureOnly, builder);
      for (int i = 0; i < type.arrayDimension(); i++) {
        builder.append("[]");
      }
      return;
    }

    String name = type.name();
    boolean isWildCardOnly = name == null; // name may be null for wildcard only types (<?>)
    if (type.isWildcardType()) {
      builder.append(JavaTypes.C_QUESTION_MARK);
      if (isWildCardOnly) {
        return; // no further processing needed.
      }
      builder.append(" extends ");
    }
    if (!isWildCardOnly) {
      builder.append(name);
    }

    if (erasureOnly) {
      return;
    }

    // type arguments
    List<IType> typeArgs = type.typeArguments().collect(toList());
    if (!typeArgs.isEmpty()) {
      builder.append(JavaTypes.C_GENERIC_START);
      buildReferenceRec(typeArgs.get(0), false, builder);
      for (int i = 1; i < typeArgs.size(); i++) {
        builder.append(JavaTypes.C_COMMA);
        buildReferenceRec(typeArgs.get(i), false, builder);
      }
      builder.append(JavaTypes.C_GENERIC_END);
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
  public HierarchyInnerTypeQuery innerTypes() {
    return new HierarchyInnerTypeQuery(this);
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
  public Optional<Stream<IType>> resolveTypeParamValue(int typeParamIndex, String levelFqn) {
    return superTypes()
        .withName(levelFqn)
        .first()
        .flatMap(levelType -> levelType.resolveTypeParamValue(typeParamIndex));
  }

  @Override
  public Optional<Stream<IType>> resolveTypeParamValue(int typeParamIndex) {
    return typeArguments().skip(typeParamIndex).findAny()
        .map(t -> t.isParameterType() ? Stream.concat(
            t.superClass().map(Stream::of).orElseGet(Stream::empty),
            t.superInterfaces())
            : Stream.of(t));
  }

  @Override
  public boolean isInstanceOf(String queryType) {
    return superTypes().withName(queryType).existsAny();
  }

  @Override
  public boolean isAssignableFrom(IType specificClass) {
    if (isPrimitive() || specificClass.isPrimitive()) {
      return PRIMITIVE_TYPE_ASSIGNABLE_PAT.matcher(elementName() + '=' + specificClass.elementName()).matches();
    }
    if (isArray() || specificClass.isArray()) {
      return name().equals(specificClass.name());
    }
    return specificClass.superTypes().withName(name()).existsAny();
  }

  @Override
  public IType boxPrimitiveType() {
    if (!isPrimitive()) {
      return this;
    }
    String boxedFqn = JavaTypes.boxPrimitive(name());
    if (boxedFqn == null) {
      return this;
    }
    return javaEnvironment().requireType(boxedFqn);
  }

  @Override
  public IType unboxToPrimitive() {
    if (isPrimitive()) {
      return this;
    }
    String myName = name();
    String unboxed = JavaTypes.unboxToPrimitive(myName);
    if (unboxed == myName) {
      return this; // there is no primitive type for this type
    }
    return javaEnvironment().requireType(unboxed);
  }

  @Override
  public IType primary() {
    IType result = null;
    Optional<IType> tmp = Optional.of(this);
    while (tmp.isPresent()) {
      result = tmp.get();
      tmp = result.declaringType();
    }
    return result;
  }

  @Override
  public ITypeGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return transformType(this, transformer);
  }

  @Override
  public ITypeGenerator<?> toWorkingCopy() {
    return toWorkingCopy(null);
  }
}
