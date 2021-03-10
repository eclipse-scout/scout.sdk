/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api.query;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.spliterator.HierarchicalStreamBuilder;
import org.eclipse.scout.sdk.core.model.api.spliterator.WrappingSpliterator;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
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

  private ApiFunction<?, IClassNameSupplier> m_name;
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
    else if (owner instanceof MethodParameterSpi) {
      var param = (MethodParameterSpi) owner;
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
    var methodId = method.wrap().identifier();
    return level -> level.methods().withMethodIdentifier(methodId).first();
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
    return withNameFrom(null, api -> IClassNameSupplier.raw(fullyQualifiedName));
  }

  /**
   * Limit the {@link IAnnotation}s to the {@link IClassNameSupplier} returned by the given nameFunction.<br>
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
  public <API extends IApiSpecification> AnnotationQuery<T> withNameFrom(Class<API> api, Function<API, IClassNameSupplier> nameFunction) {
    if (nameFunction == null) {
      m_name = null;
    }
    else {
      m_name = new ApiFunction<>(api, nameFunction);
    }
    return this;
  }

  protected ApiFunction<?, IClassNameSupplier> getName() {
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
    var fqn = name.apply(a.javaEnvironment()).map(IClassNameSupplier::fqn);
    return fqn.isPresent() && fqn.get().equals(a.name());
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Stream<T> createStream() {
    Function<IAnnotatable, Spliterator<IAnnotation>> toAnnotations = o -> new WrappingSpliterator<>(o.unwrap().getAnnotations());
    var levelSpliteratorProvider = lookupOwnerOnLevel().andThen(
        owner -> owner
            .map(toAnnotations)
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
