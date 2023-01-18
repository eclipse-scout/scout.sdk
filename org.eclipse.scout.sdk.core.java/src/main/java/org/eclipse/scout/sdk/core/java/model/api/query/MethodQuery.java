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

import static java.util.Collections.singletonList;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.spliterator.HierarchicalStreamBuilder;
import org.eclipse.scout.sdk.core.java.model.api.spliterator.WrappingSpliterator;

/**
 * <h3>{@link MethodQuery}</h3> Method query that by default returns all {@link IMethod}s in an {@link IType}.
 *
 * @since 5.1.0
 */
public class MethodQuery extends AbstractQuery<IMethod> implements Predicate<IMethod> {

  // used in the spliterator
  private final IType m_type;
  private boolean m_includeSuperClasses;
  private boolean m_includeSuperInterfaces;
  private String m_methodId;

  // used in this predicate
  private String m_name;
  private ApiFunction<?, ITypeNameSupplier> m_annotation;
  private int m_flags = -1;
  private Pattern m_methodNamePattern;

  public MethodQuery(IType type) {
    m_type = type;
  }

  protected static Spliterator<IMethod> findMatchingMethods(@SuppressWarnings("TypeMayBeWeakened") IType container, String id) {
    if (id == null) {
      return new WrappingSpliterator<>(container.unwrap().getMethods());
    }
    for (var m : container.unwrap().getMethods()) {
      var methodName = m.getElementName(); // performance improvement: only compute identifier for methods where at least the name matches
      if (id.startsWith(methodName) && id.equals(m.wrap().identifier())) {
        return new WrappingSpliterator<>(singletonList(m));
      }
    }
    return Spliterators.emptySpliterator();
  }

  protected IType getType() {
    return m_type;
  }

  /**
   * Include or exclude super types visiting when searching for {@link IMethod}s.
   *
   * @param b
   *          {@code true} if all super classes and super interfaces should be checked for {@link IMethod}s. Default is
   *          {@code false}.
   * @return this
   */
  public MethodQuery withSuperTypes(boolean b) {
    m_includeSuperClasses = b;
    m_includeSuperInterfaces = b;
    return this;
  }

  /**
   * Limit the {@link IMethod}s to the ones having an {@link IAnnotation} with the given fully qualified name.
   *
   * @param fqn
   *          The fully qualified name of the {@link IAnnotation} that must exist on the {@link IMethod}.
   * @return this
   */
  public MethodQuery withAnnotation(CharSequence fqn) {
    return withAnnotationFrom(null, api -> ITypeNameSupplier.of(fqn));
  }

  /**
   * Limit the {@link IMethod}s to the ones having an {@link IAnnotation} with the fully qualified name as specified by
   * the {@link ITypeNameSupplier} returned by the given nameFunction.<br>
   * <b>Example:</b> {@code type.methods().withAnnotationFrom(IJavaApi.class, IJavaApi::Deprecated)}.
   *
   * @param api
   *          The api type that defines the type. An instance of this API is passed to the nameFunction. May be
   *          {@code null} in case the given nameFunction can handle a {@code null} input.
   * @param nameFunction
   *          A {@link Function} to be called to obtain the fully qualified annotation name to search.
   * @param <API>
   *          The API type that contains the class name
   * @return this
   */
  public <API extends IApiSpecification> MethodQuery withAnnotationFrom(Class<API> api, Function<API, ITypeNameSupplier> nameFunction) {
    if (nameFunction == null) {
      m_annotation = null;
    }
    else {
      m_annotation = new ApiFunction<>(api, nameFunction);
    }
    return this;
  }

  protected ApiFunction<?, ITypeNameSupplier> getAnnotation() {
    return m_annotation;
  }

  /**
   * Limit the {@link IMethod}s to the ones having at least all the given flags.
   *
   * @param flags
   *          The flags that must exist on the {@link IMethod}
   * @return this
   * @see Flags
   */
  public MethodQuery withFlags(int flags) {
    m_flags = flags;
    return this;
  }

  protected int getFlags() {
    return m_flags;
  }

  /**
   * Include or exclude super class visiting when searching for {@link IMethod}s.
   *
   * @param b
   *          {@code true} if all super classes should be checked for {@link IMethod}s. Default is {@code false}.
   * @return this
   */
  public MethodQuery withSuperClasses(boolean b) {
    m_includeSuperClasses = b;
    return this;
  }

  protected boolean isIncludeSuperClasses() {
    return m_includeSuperClasses;
  }

  /**
   * Include or exclude super interfaces visiting when searching for {@link IMethod}s.
   *
   * @param b
   *          {@code true} if all super interfaces should be checked for {@link IMethod}s. Default is {@code false}.
   * @return this
   */
  public MethodQuery withSuperInterfaces(boolean b) {
    m_includeSuperInterfaces = b;
    return this;
  }

  protected boolean isIncludeSuperInterfaces() {
    return m_includeSuperInterfaces;
  }

  /**
   * Limit the {@link IMethod}s to the given name (see {@link IMethod#elementName()}).
   *
   * @param name
   *          The {@link IMethod} name. Default is no filtering.
   * @return this
   */
  public MethodQuery withName(String name) {
    m_name = name;
    return this;
  }

  protected String getName() {
    return m_name;
  }

  /**
   * Limit the {@link IMethod}s to the given method identifier (erasure only).
   *
   * @param id
   *          The id (with type erasure only) of the {@link IMethod}. <br>
   *          Use {@link IMethod#identifier()}, {@link IMethodGenerator#identifier(IJavaBuilderContext)} or
   *          {@link JavaTypes#createMethodIdentifier(CharSequence, java.util.Collection)} to create a method
   *          identifier.
   * @return this
   */
  public MethodQuery withMethodIdentifier(String id) {
    m_methodId = id;
    return this;
  }

  protected String getMethodIdentifier() {
    return m_methodId;
  }

  /**
   * Limit to the {@link IMethod}s whose name ({@link IMethod#elementName()}) matches the given regular expression
   * pattern.
   *
   * @param namePattern
   *          The regular expression the method name must match
   * @return this
   * @see Pattern
   */
  public MethodQuery withName(Pattern namePattern) {
    m_methodNamePattern = namePattern;
    return this;
  }

  protected Pattern getNamePattern() {
    return m_methodNamePattern;
  }

  /**
   * Tests if the given {@link IMethod} fulfills the filter criteria of this query.
   */
  @Override
  public boolean test(IMethod candidate) {
    var name = getName();
    if (name != null && !name.equals(candidate.elementName())) {
      return false;
    }

    var flags = getFlags();
    if (flags >= 0 && (candidate.flags() & flags) != flags) {
      return false;
    }

    var namePat = getNamePattern();
    if (namePat != null && !namePat.matcher(candidate.elementName()).matches()) {
      return false;
    }

    var annotation = getAnnotation();
    return annotation == null || annotation.apply(candidate.javaEnvironment())
        .map(ITypeNameSupplier::fqn)
        .map(annotationFqn -> candidate.annotations().withName(annotationFqn).existsAny())
        .orElse(false);
  }

  @Override
  protected Stream<IMethod> createStream() {
    return new HierarchicalStreamBuilder<IMethod>()
        .withSuperClasses(isIncludeSuperClasses())
        .withSuperInterfaces(isIncludeSuperInterfaces())
        .withStartType(true)
        .build(getType(), level -> findMatchingMethods(level, getMethodIdentifier()))
        .filter(this);
  }
}
