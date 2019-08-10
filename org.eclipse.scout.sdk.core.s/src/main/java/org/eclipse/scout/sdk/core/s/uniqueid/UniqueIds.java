/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.uniqueid;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.log.SdkLog;

/**
 * <h3>{@link UniqueIds}</h3>
 *
 * @since 7.0.0
 */
public final class UniqueIds {

  public static final List<Function<String, String>> STORE = new CopyOnWriteArrayList<>();

  private UniqueIds() {
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
    for (Function<String, String> p : STORE) {
      try {
        String value = p.apply(dataType);
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
