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
package org.eclipse.scout.sdk.core.s.dataobject;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScout22DoApi;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.util.CompositeObject;

/**
 * Class to obtain {@link DoContext} instances and to register {@link IDoContextResolver} strategies.
 */
public final class DoContextResolvers {

  private static volatile IDoContextResolver resolver;

  private DoContextResolvers() {
  }

  /**
   * Gets the {@link DoContext} information for the package within the given {@link IJavaEnvironment}.
   * 
   * @param targetPackage
   *          The package for which the {@link DoContext} should be computed. Must not be {@code null}.
   * @param environment
   *          The {@link IJavaEnvironment} defining the classpath for which the context should be created. Must not be
   *          {@code null}.
   * @return The computed {@link DoContext}. Never returns {@code null}. But the resulting context may contain no
   *         information if nothing could be found.
   */
  public static DoContext resolve(CharSequence targetPackage, IJavaEnvironment environment) {
    return get()
        .map(r -> resolve(r, targetPackage, environment))
        .orElseGet(DoContext::new);
  }

  static DoContext resolve(IDoContextResolver resolver, CharSequence targetPackage, IJavaEnvironment environment) {
    var namespaceCandidates = resolver.resolveNamespaceCandidates(environment);
    var namespace = selectNamespace(targetPackage, namespaceCandidates);

    var typeVersionCandidates = Optional.ofNullable(namespace)
        .flatMap(n -> n.javaEnvironment().requireApi(IScoutApi.class).api(IScout22DoApi.class))
        .map(doApi -> doApi.ITypeVersion().fqn())
        .map(iTypeVersionFqn -> resolver.resolvePrimaryTypesInPackageOf(namespace)
            .flatMap(sibling -> resolveTypeVersionCandidates(iTypeVersionFqn, sibling))
            .collect(toList()))
        .orElseGet(Collections::emptyList);
    var typeVersion = selectNewestTypeVersion(typeVersionCandidates);

    return new DoContext(namespace, typeVersion);
  }

  static IType selectNamespace(CharSequence refPackage, Stream<IType> candidates) {
    var bestMatch = -1;
    IType result = null;
    var ref = ISdkConstants.REGEX_DOT.split(refPackage);
    var namespaces = candidates.collect(toList());
    for (var candidate : namespaces) {
      var curMatch = numSegmentsEquals(ref, ISdkConstants.REGEX_DOT.split(candidate.containingPackage().elementName()));
      if (curMatch > bestMatch) {
        bestMatch = curMatch;
        result = candidate;
      }
    }
    return result;
  }

  static int numSegmentsEquals(String[] a, String[] b) {
    var limit = Math.min(a.length, b.length);
    var num = 0;
    for (var i = 0; i < limit; i++) {
      if (Objects.equals(a[i], b[i])) {
        num++;
      }
      else {
        return num;
      }
    }
    return num;
  }

  static Stream<IType> resolveTypeVersionCandidates(String iTypeVersionFqn, IType sibling) {
    var self = Stream.of(sibling).filter(s -> s.isInstanceOf(iTypeVersionFqn));
    var inner = sibling.innerTypes()
        .withRecursiveInnerTypes(true)
        .withInstanceOf(iTypeVersionFqn)
        .withFlags(Flags.AccPublic)
        .stream();
    return Stream.concat(self, inner);
  }

  static IType selectNewestTypeVersion(Iterable<IType> typeVersionCandidates) {
    var pat = Pattern.compile("(\\w+?)_(\\d+(?:_\\d+)*)"); // must be the same as in org.eclipse.scout.rt.dataobject.AbstractTypeVersion
    var curVersion = new CompositeObject(-1);
    IType result = null;
    for (var candidate : typeVersionCandidates) {
      var matcher = pat.matcher(candidate.elementName());
      if (matcher.matches()) {
        var versionSegments = Arrays.stream(matcher.group(2).split("_"))
            .map(Integer::parseInt)
            .toArray();
        var co = new CompositeObject(versionSegments);
        if (co.compareTo(curVersion) > 0) {
          curVersion = co;
          result = candidate;
        }
      }
    }
    return result;
  }

  /**
   * Sets the {@link IDoContextResolver} to use.
   *
   * @param newResolver
   *          The new resolver or {@code null}.
   * @return The previous {@link IDoContextResolver} or {@code null} if there was none.
   */
  public static IDoContextResolver set(IDoContextResolver newResolver) {
    var previous = resolver;
    resolver = newResolver;
    return previous;
  }

  /**
   * @return The {@link IDoContextResolver} currently in use.
   */
  public static Optional<IDoContextResolver> get() {
    return Optional.ofNullable(resolver);
  }

  /**
   * Strategy to resolve DoContext base information
   */
  public interface IDoContextResolver {
    /**
     * Resolves all source INamespace classes available on the classpath given.
     * 
     * @param environment
     *          The classpath in which the INamespace classes should be searched.
     * @return All INamespace classes found in the classpath.
     */
    Stream<IType> resolveNamespaceCandidates(IJavaEnvironment environment);

    /**
     * Gets all classes in the same package as the namespace class given.
     * 
     * @param namespace
     *          The namespace class.
     * @return All types that exist in the exact same package as the class given.
     */
    Stream<IType> resolvePrimaryTypesInPackageOf(IType namespace);
  }
}
