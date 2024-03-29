/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api.query;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.spliterator.HierarchicalStreamBuilder;
import org.eclipse.scout.sdk.core.java.model.api.spliterator.WrappingSpliterator;
import org.eclipse.scout.sdk.core.java.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.java.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.java.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.java.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.java.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.model.query.AbstractQuery;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link AnnotationQuery}</h3> Annotation query that by default returns all annotations directly defined on the
 * owner.
 *
 * @since 5.1.0
 */
public class AnnotationQuery<T> extends AbstractQuery<T> implements Predicate<IAnnotation> {

  private final IType m_containerType; // may be null
  private final Function<IType, Optional<? extends IAnnotatable>> m_ownerInLevelFinder;

  private boolean m_includeSuperClasses;
  private boolean m_includeSuperInterfaces;

  private ApiFunction<?, ITypeNameSupplier> m_name;
  private Class<AbstractManagedAnnotation> m_managedWrapperType;

  public AnnotationQuery(IType containerType, JavaElementSpi owner) {
    m_containerType = containerType;

    if (owner instanceof TypeSpi) {
      m_ownerInLevelFinder = Optional::of;
    }
    else if (owner instanceof MethodSpi) {
      m_ownerInLevelFinder = getMethodLookup((MethodSpi) owner);
    }
    else if (owner instanceof FieldSpi) {
      m_ownerInLevelFinder = level -> level.fields().withName(owner.getElementName()).first();
    }
    else if (owner instanceof MethodParameterSpi param) {
      m_ownerInLevelFinder = getMethodLookup(param.getDeclaringMethod())
          .andThen(method -> method
              .flatMap(m -> ((IMethod) m).parameters().item(param.getIndex())));
    }
    else if (owner instanceof PackageSpi) {
      m_ownerInLevelFinder = Optional::of;
    }
    else {
      throw new IllegalArgumentException("Unsupported annotation container: " + owner.getClass().getName());
    }
  }

  @SuppressWarnings("TypeMayBeWeakened")
  protected static Function<IType, Optional<? extends IAnnotatable>> getMethodLookup(MethodSpi method) {
    var m = method.wrap();
    var declaringType = m.declaringType().orElse(null);
    return level -> {
      if (declaringType == level) {
        // if the level is the class of the start method: directly return the method without computing an identifier (better performance for queries without super types) 
        return Optional.of(m);
      }
      return level.methods().withMethodIdentifier(m.identifier()).first();
    };
  }

  protected IType getType() {
    return m_containerType;
  }

  protected Function<IType, Optional<? extends IAnnotatable>> lookupOwnerOnLevel() {
    return m_ownerInLevelFinder;
  }

  /**
   * Include or exclude super types visiting when searching for annotations.
   *
   * @param b
   *          {@code true} if all super classes and super interfaces should be checked for annotations. Default is
   *          {@code false}.
   * @return this
   */
  public AnnotationQuery<T> withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * Include or exclude super class visiting when searching for annotations.
   *
   * @param b
   *          {@code true} if all super classes should be checked for annotations. Default is {@code false}.
   * @return this
   */
  public AnnotationQuery<T> withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  protected boolean isIncludeSuperClasses() {
    return m_includeSuperClasses;
  }

  /**
   * Include or exclude super interface visiting when searching for annotations.
   *
   * @param b
   *          {@code true} if all super interfaces should be checked for annotations. Default is {@code false} .
   * @return this
   */
  public AnnotationQuery<T> withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  protected boolean isIncludeSuperInterfaces() {
    return m_includeSuperInterfaces;
  }

  /**
   * Limit the annotations to the given fully qualified annotation type name (see {@link IAnnotation#type()}).
   *
   * @param fullyQualifiedName
   *          The fully qualified name. Default is no filtering.
   * @return this
   */
  public AnnotationQuery<T> withName(CharSequence fullyQualifiedName) {
    return withNameFrom(null, api -> ITypeNameSupplier.of(fullyQualifiedName));
  }

  /**
   * Limit the {@link IAnnotation}s to the {@link ITypeNameSupplier} returned by the given nameFunction.<br>
   * <b>Example:</b> {@code type.annotations().withNameFrom(IJavaApi.class, IJavaApi::Override)}.
   *
   * @param api
   *          The api type that defines the type. An instance of this API is passed to the nameFunction. May be
   *          {@code null} in case the given nameFunction can handle a {@code null} input.
   * @param nameFunction
   *          A {@link Function} to be called to obtain the fully qualified type name to search.
   * @param <API>
   *          The API type that contains the class name
   * @return this
   */
  public <API extends IApiSpecification> AnnotationQuery<T> withNameFrom(Class<API> api, Function<API, ITypeNameSupplier> nameFunction) {
    if (nameFunction == null) {
      m_name = null;
    }
    else {
      m_name = new ApiFunction<>(api, nameFunction);
    }
    return this;
  }

  protected ApiFunction<?, ITypeNameSupplier> getName() {
    return m_name;
  }

  /**
   * Limit the annotations to the given managed annotation type and convert the result into the narrowed managed type.
   *
   * @param managedWrapperType
   *          The managed annotation type class. Default no filtering.
   * @return this
   */
  @SuppressWarnings("unchecked")
  public <A extends AbstractManagedAnnotation> AnnotationQuery<A> withManagedWrapper(Class<A> managedWrapperType) {
    m_managedWrapperType = (Class<AbstractManagedAnnotation>) managedWrapperType;
    m_name = AbstractManagedAnnotation.typeName(managedWrapperType);
    return (AnnotationQuery<A>) this;
  }

  protected Class<AbstractManagedAnnotation> getManagedWrapper() {
    return m_managedWrapperType;
  }

  /**
   * Tests if the given {@link IAnnotation} fulfills the filter criteria of this query.
   */
  @Override
  public boolean test(IAnnotation a) {
    var name = getName();
    if (name == null) {
      return true; // not filtered by name
    }
    var fqn = name.apply(a.javaEnvironment()).map(ITypeNameSupplier::fqn);
    return fqn.isPresent() && fqn.orElseThrow().equals(a.name());
  }

  private static Spliterator<IAnnotation> getAnnotationsSpliterator(@SuppressWarnings("TypeMayBeWeakened") IAnnotatable o) {
    return new WrappingSpliterator<>(o.unwrap().getAnnotations());
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Stream<T> createStream() {
    var levelSpliteratorProvider = lookupOwnerOnLevel().andThen(
        owner -> owner
            .map(AnnotationQuery::getAnnotationsSpliterator)
            .orElse(null));
    var result = new HierarchicalStreamBuilder<IAnnotation>()
        .withSuperClasses(isIncludeSuperClasses())
        .withSuperInterfaces(isIncludeSuperInterfaces())
        .withStartType(true)
        .build(Ensure.notNull(getType()), levelSpliteratorProvider)
        .filter(this);

    var wrapperClass = getManagedWrapper();
    if (wrapperClass == null) {
      return (Stream<T>) result;
    }

    var converted = result.map(annotation -> annotation.wrap(wrapperClass));
    return (Stream<T>) converted;
  }
}
