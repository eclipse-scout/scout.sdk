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
package org.eclipse.scout.sdk.core.model.api.query;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link AnnotationQuery}</h3> Annotation query that by default returns all annotations directly defined on the
 * owner.
 *
 * @since 5.1.0
 */
public class AnnotationQuery<T> extends AbstractQuery<T> implements Predicate<IAnnotation> {

  private final IType m_containerType;
  private final Function<IType, Optional<? extends IAnnotatable>> m_ownerInLevelFinder;

  private boolean m_includeSuperClasses;
  private boolean m_includeSuperInterfaces;

  private String m_name;
  private Class<AbstractManagedAnnotation> m_managedWrapperType;

  public AnnotationQuery(IType containerType, JavaElementSpi owner) {
    m_containerType = Ensure.notNull(containerType);

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
      MethodParameterSpi param = (MethodParameterSpi) owner;
      m_ownerInLevelFinder = getMethodLookup(param.getDeclaringMethod())
          .andThen(method -> method
              .flatMap(m -> ((IMethod) m).parameters().item(param.getIndex())));
    }
    else {
      throw new IllegalArgumentException("Unsupported annotation container: " + owner.getClass().getName());
    }
  }

  protected static Function<IType, Optional<? extends IAnnotatable>> getMethodLookup(MethodSpi method) {
    String methodId = method.wrap().identifier();
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
  public AnnotationQuery<T> withName(String fullyQualifiedName) {
    m_name = fullyQualifiedName;
    return this;
  }

  protected String getName() {
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
    withName(AbstractManagedAnnotation.typeName(managedWrapperType));
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
    String name = getName();
    return name == null || name.equals(a.name());
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Stream<T> createStream() {
    Function<IAnnotatable, Spliterator<IAnnotation>> toAnnotations = o -> new WrappingSpliterator<>(o.unwrap().getAnnotations());
    Function<IType, Spliterator<IAnnotation>> levelSpliteratorProvider = lookupOwnerOnLevel().andThen(
        owner -> owner
            .map(toAnnotations)
            .orElse(null));
    Stream<IAnnotation> result = new HierarchicalStreamBuilder<IAnnotation>()
        .withSuperClasses(isIncludeSuperClasses())
        .withSuperInterfaces(isIncludeSuperInterfaces())
        .withStartType(true)
        .build(getType(), levelSpliteratorProvider)
        .filter(this);

    Class<AbstractManagedAnnotation> wrapperClass = getManagedWrapper();
    if (wrapperClass == null) {
      return (Stream<T>) result;
    }

    Stream<AbstractManagedAnnotation> converted = result.map(annot -> annot.wrap(wrapperClass));
    return (Stream<T>) converted;
  }
}