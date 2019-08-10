/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.util;

import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link ScoutTier}</h3> Helper class to detect and convert scout tiers.
 *
 * @since 5.2.0
 */
@SuppressWarnings("squid:S00115")
public enum ScoutTier implements Predicate<IJavaElement> {

  /**
   * Scout Client Tier
   */
  Client,

  /**
   * Scout Shared Tier
   */
  Shared,

  /**
   * Scout Server Tier
   */
  Server,

  /**
   * Scout HTML UI Tier
   */
  HtmlUi;

  /**
   * Tests if the tier of this instance is included in the tier of the given {@link IJavaElement}.
   *
   * @return {@code true} if the tier of the given {@link IJavaElement} includes all elements of this tier.
   *         {@code false} otherwise or if the given {@link IJavaElement} does not belong to a Scout tier.
   * @see #isIncludedIn(ScoutTier)
   */
  @Override
  public boolean test(IJavaElement element) {
    return valueOf(element)
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
   * @param tierOfOtherElement
   *          The other tier to test against or {@code null}.
   * @return {@code true} if the given tier includes all elements of this tier. {@code false} otherwise or if the given
   *         tier is {@code null}.
   */
  public boolean isIncludedIn(ScoutTier tierOfOtherElement) {
    if (tierOfOtherElement == null) {
      return false;
    }
    if (Shared == this) {
      return true; // shared is always available if the other is not null
    }
    if (Client == this) {
      return Client == tierOfOtherElement || HtmlUi == tierOfOtherElement;
    }
    return equals(tierOfOtherElement);
  }

  /**
   * Gets the {@link ScoutTier} the given {@link IJavaElement} belongs to.
   *
   * @param element
   *          The {@link IJavaElement} for which the {@link ScoutTier} should be calculated.
   * @return The {@link ScoutTier} the given element belongs to or {@code null} if it does not belong to a
   *         {@link ScoutTier}.
   */
  public static Optional<ScoutTier> valueOf(IJavaElement element) {
    return Optional.ofNullable(element)
        .map(IJavaElement::javaEnvironment)
        .flatMap(ScoutTier::valueOf);
  }

  public static Optional<ScoutTier> valueOf(IJavaEnvironment env) {
    if (env == null) {
      return Optional.empty();
    }
    return valueOf(env::exists);
  }

  public static Optional<ScoutTier> valueOf(Predicate<String> typeLookupStrategy) {
    if (typeLookupStrategy == null) {
      return Optional.empty();
    }

    boolean uiAvailable = typeLookupStrategy.test(IScoutRuntimeTypes.UiServlet);
    if (uiAvailable) {
      return Optional.of(HtmlUi);
    }

    boolean clientAvailable = typeLookupStrategy.test(IScoutRuntimeTypes.IClientSession);
    if (clientAvailable) {
      return Optional.of(Client);
    }

    boolean serverAvailable = typeLookupStrategy.test(IScoutRuntimeTypes.IServerSession);
    if (serverAvailable) {
      return Optional.of(Server);
    }

    boolean sharedAvailable = typeLookupStrategy.test(IScoutRuntimeTypes.ISession);
    if (sharedAvailable) {
      return Optional.of(Shared);
    }

    return Optional.empty();
  }

  /**
   * Gets the name of this {@link ScoutTier}.
   *
   * @return E.g. "client" or "ui.html"
   */
  public String tierName() {
    switch (this) {
      case Client:
        return "client";
      case Shared:
        return "shared";
      case HtmlUi:
        return "ui.html";
      default:
        return "server";
    }
  }

  /**
   * Converts the given {@link String} from the {@link #tierName()} of this {@link ScoutTier} to the {@link #tierName()}
   * of the given {@link ScoutTier}.<br>
   * <br>
   * <b>Example :</b><br>
   * {@code ScoutTier.Client.convert(ScoutTier.Server, "org.eclipse.scout.client.test.app")} ->
   * {@code "org.eclipse.scout.server.test.app"}
   *
   * @param to
   *          The {@link ScoutTier} to which the given name should be converted.
   * @param name
   *          The name to convert.
   * @return The converted {@link String}.
   */
  public String convert(ScoutTier to, String name) {
    if (Strings.isBlank(name) || to == null) {
      return name;
    }
    if (to == this) {
      return name;
    }
    return Strings.replace(name, '.' + tierName(), '.' + to.tierName());
  }

}
