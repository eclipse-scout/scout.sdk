/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.util;

import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.OptApiFunction;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.Strings;

public interface ITier<A extends IApiSpecification> extends Predicate<IJavaElement> {

  /**
   * The name of the tier. Used to convert e.g. package names from on tier to another (see
   * {@link #convert(ITier, String)}).
   */
  String tierName();

  /**
   * Get a fully qualified name of an element for a given API to test whether an object is a tier or not (see
   * {@link #of(IJavaElement)}, {@link #of(IJavaEnvironment)}, {@link #of(Predicate, OptApiFunction)}).
   */
  String getLookupFqn(A api);

  /**
   * Get the {@link Class} corresponding to {@link A}.
   */
  Class<A> getApiClass();

  /**
   * Get a fully qualified name of an element for a strategy to resolve an API to test whether an object is a tier or
   * not (see {@link #of(IJavaElement)}, {@link #of(IJavaEnvironment)}, {@link #of(Predicate, OptApiFunction)}).
   */
  default String getLookupFqn(OptApiFunction optApiFunction) {
    return Optional.ofNullable(optApiFunction)
        .flatMap(oaf -> oaf.apply(getApiClass()))
        .map(this::getLookupFqn)
        .orElse(null);
  }

  /**
   * Tests if the tier of this instance is included in the tier of the given {@link IJavaElement}.
   *
   * @return {@code true} if the tier of the given {@link IJavaElement} includes all elements of this tier.
   *         {@code false} otherwise or if the given {@link IJavaElement} does not belong to a tier.
   * @see #isIncludedIn(ITier)
   */
  @Override
  default boolean test(IJavaElement element) {
    return of(element)
        .filter(this::isIncludedIn)
        .isPresent();
  }

  /**
   * Tests if the tier of this instance is included in the given tier.<br>
   * <br>
   * <b>Examples:</b><br>
   * {@code ScoutTier.Shared.isIncludedIn(ScoutTier.Client) == true}<br>
   * {@code ScoutTier.Client.isIncludedIn(ScoutTier.HtmlUi) == true}<br>
   * {@code ScoutTier.Server.isIncludedIn(ScoutTier.Server) == true}<br>
   * {@code ScoutTier.Server.isIncludedIn(ScoutTier.Shared) == false}<br>
   * {@code ScoutTier.HtmlUi.isIncludedIn(ScoutTier.Client) == false}<br>
   * {@code ScoutTier.Shared.isIncludedIn(null) == false}
   *
   * @param tier
   *          The other tier to test against or {@code null}.
   * @return {@code true} if the given tier includes all elements of this tier. {@code false} otherwise or if the given
   *         tier is {@code null}.
   */
  default boolean isIncludedIn(ITier<?> tier) {
    return TierTree.isAvailable(tier, this);
  }

  /**
   * Get the corresponding tier for the given {@link IJavaElement}.
   */
  static Optional<ITier<?>> of(IJavaElement element) {
    return TierTree.tierOf(element);
  }

  /**
   * Get the corresponding tier for the given {@link IJavaEnvironment}.
   */
  static Optional<ITier<?>> of(IJavaEnvironment env) {
    return TierTree.tierOf(env);
  }

  /**
   * Get the corresponding tier for the given strategies.
   * 
   * @param typeLookupStrategy
   *          a tier will use this {@link Predicate} to check if its lookupFqn (see
   *          {@link #getLookupFqn(OptApiFunction)}) is available
   * @param optApiFunction
   *          a function that provides an implementation of a given API class (see {@link #getApiClass()})
   */
  static Optional<ITier<?>> of(Predicate<String> typeLookupStrategy, OptApiFunction optApiFunction) {
    return TierTree.tierOf(typeLookupStrategy, optApiFunction);
  }

  /**
   * Converts the given {@link String} from the {@link #tierName()} of this {@link ITier} to the {@link #tierName()} of
   * the given {@link ITier}.<br>
   * <br>
   * <b>Example :</b><br>
   * {@code ScoutTier.Client.convert(ScoutTier.Server, "org.eclipse.scout.client.test.app")} ->
   * {@code "org.eclipse.scout.server.test.app"}
   *
   * @param to
   *          The {@link ITier} to which the given name should be converted.
   * @param name
   *          The name to convert.
   * @return The converted {@link String}.
   */
  default String convert(ITier<?> to, String name) {
    if (Strings.isBlank(name) || to == null) {
      return name;
    }
    if (to == this) {
      return name;
    }
    return Strings.replace(name, '.' + tierName(), '.' + to.tierName()).toString();
  }
}
