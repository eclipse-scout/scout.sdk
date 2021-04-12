/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api.internal;

import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.eclipse.scout.sdk.core.util.JavaTypes.arrayMarker;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IImport;
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
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

@SuppressWarnings("squid:S2160")
public class TypeImplementor extends AbstractMemberImplementor<TypeSpi> implements IType {
  private static final Pattern PRIMITIVE_TYPE_ASSIGNABLE_PAT =
      Pattern.compile(
          "char=(?:char|Character)|byte=(?:byte|Byte)|short=(?:short|Short)|int=(?:int|Integer)|long=(?:long|Long)|" +
              "float=(?:float|Float)|double=(?:double|Double)|Character=(?:char|Character)|Byte=(?:byte|Byte)|" +
              "Short=(?:short|Short)|Integer=(?:int|Integer)|Long=(?:long|Long)|Float=(?:float|Float)|Double=(?:double|Double)");
  private String m_reference;
  private String m_referenceErasureOnly;

  public TypeImplementor(TypeSpi spi) {
    super(spi);
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
    return Stream.of(fields().stream(), methods().stream(), innerTypes().stream(), typeParameters(), annotations().stream())
        .flatMap(f -> f)
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
  public Stream<IType> directSuperTypes() {
    return Stream.concat(superClass().stream(), superInterfaces());
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
    var builder = new StringBuilder(128);
    buildReferenceRec(type, erasureOnly, builder);
    return builder.toString();
  }

  protected static void buildReferenceRec(IType type, boolean erasureOnly, StringBuilder builder) {
    if (type.isArray()) {
      buildReferenceRec(type.leafComponentType().get(), erasureOnly, builder);
      builder.append(arrayMarker(type.arrayDimension()));
      return;
    }

    var name = type.name();
    var isWildCardOnly = name == null; // name may be null for wildcard only types (<?>)
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
    var typeArgs = type.typeArguments().collect(toList());
    if (!typeArgs.isEmpty()) {
      builder.append(JavaTypes.C_GENERIC_START);
      buildReferenceRec(typeArgs.get(0), false, builder);
      for (var i = 1; i < typeArgs.size(); i++) {
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
        .map(t -> t.isParameterType() ? t.directSuperTypes() : Stream.of(t));
  }

  @Override
  public Optional<IType> resolveSimpleName(String simpleName) {
    if (Strings.isBlank(simpleName)) {
      return Optional.empty();
    }
    var optCu = compilationUnit();
    if (optCu.isEmpty()) {
      return Optional.empty(); // simple types cannot resolve anything
    }
    var env = javaEnvironment();
    var compilationUnit = optCu.get();

    // 1. step: try to resolve the name using the declared imports
    var fromImports = resolveNameUsingImports(compilationUnit, simpleName);
    if (fromImports.isPresent()) {
      return fromImports;
    }

    // 2. step: search in own package
    var pck = Strings.notBlank(compilationUnit.containingPackage().elementName())
        .map(p -> p + JavaTypes.C_DOT)
        .orElse("");
    var fromPackage = env.findType(pck + simpleName);
    if (fromPackage.isPresent()) {
      return fromPackage;
    }

    // 3. step: name is already fully qualified
    var fromFqn = env.findType(simpleName);
    if (fromFqn.isPresent()) {
      return fromFqn;
    }

    // 4. step: in java.lang package
    return env.findType("java.lang." + simpleName);
  }

  protected Optional<IType> resolveNameUsingImports(ICompilationUnit compilationUnit, String simpleName) {
    var env = javaEnvironment();
    return compilationUnit
        .imports() // imports are empty on synthetic type. Can only be parsed if source is available
        .map(imp -> toImportName(imp, simpleName))
        .filter(Objects::nonNull)
        .map(env::findType)
        .flatMap(Optional::stream)
        .findAny();
  }

  private static String toImportName(IImport imp, String simpleNameToResolve) {
    var impName = imp.name();
    if (impName.endsWith(JavaTypes.C_DOT + simpleNameToResolve)) {
      // explicit import
      return impName;
    }
    var wildcardSuffix = ".*";
    if (impName.endsWith(wildcardSuffix)) {
      // wildcard import
      return impName.substring(0, impName.length() - 1 /* only remove the star, keep the dot */) + simpleNameToResolve;
    }
    return null;
  }

  @Override
  public boolean isInstanceOf(IClassNameSupplier typeName) {
    return isInstanceOf(typeName.fqn());
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
    var boxedFqn = JavaTypes.boxPrimitive(name());
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
    var myName = name();
    var unboxed = JavaTypes.unboxToPrimitive(myName);
    //noinspection StringEquality
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
    return TypeGenerator.create(this, transformer);
  }

  @Override
  public ITypeGenerator<?> toWorkingCopy() {
    return toWorkingCopy(null);
  }

  @Override
  public String toString() {
    return name();
  }
}
