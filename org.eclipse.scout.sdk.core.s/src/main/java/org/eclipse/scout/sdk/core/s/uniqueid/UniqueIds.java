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
package org.eclipse.scout.sdk.core.s.uniqueid;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link UniqueIds}</h3>
 *
 * @since 7.0.0
 */
public final class UniqueIds {

  private static final List<Function<String, String>> STORE = new ArrayList<>();

  private UniqueIds() {
  }

  public static synchronized boolean registerIdProvider(Function<String, String> provider) {
    if (STORE.contains(Ensure.notNull(provider))) {
      return false;
    }
    STORE.add(0, provider); // insert at beginning
    return true;
  }

  public static synchronized boolean removeIdProvider(Function<String, String> provider) {
    return STORE.remove(provider);
  }

  public static synchronized List<Function<String, String>> providers() {
    return new ArrayList<>(STORE);
  }

  /**
   * Gets the next unique id from the first unique ID provider which provides a non-{@code null} value for the given
   * input.
   *
   * @param dataType
   *          Fully qualified {@link String} describing the requested data type.
   * @return The unique id or {@code null}.
   */
  public static String next(String dataType) {
    for (var p : providers()) {
      try {
        var value = p.apply(dataType);
        if (value != null) {
          return value;
        }
      }
      catch (RuntimeException e) {
        SdkLog.warning("Exception in UniqueIdProvider '{}'.", p.getClass().getName(), e);
      }
    }
    return null;
  }
}
