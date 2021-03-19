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
package org.eclipse.scout.sdk.core.model.api.query;

import static java.util.Collections.singletonList;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.spliterator.HierarchicalStreamBuilder;
import org.eclipse.scout.sdk.core.model.api.spliterator.WrappingSpliterator;
import org.eclipse.scout.sdk.core.util.JavaTypes;

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
  private String m_annotationFqn;
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
  public MethodQuery withAnnotation(String fqn) {
    m_annotationFqn = fqn;
    return this;
  }

  protected String getAnnotationFqn() {
    return m_annotationFqn;
  }

  /**
   * Limit the {@link IMethod}s to the ones having at least all of the given flags.
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
   *          Use {@link IMethod#identifier()}, {@link IMethodGenerator#identifier(IJavaEnvironment)} or
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
  public boolean test(IMethod f) {
    var name = getName();
    if (name != null && !name.equals(f.elementName())) {
      return false;
    }

    var flags = getFlags();
    if (flags >= 0 && (f.flags() & flags) != flags) {
      return false;
    }

    var namePat = getNamePattern();
    if (namePat != null && !namePat.matcher(f.elementName()).matches()) {
      return false;
    }

    var annotFqn = getAnnotationFqn();
    return annotFqn == null || f.annotations().withName(annotFqn).existsAny();
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
