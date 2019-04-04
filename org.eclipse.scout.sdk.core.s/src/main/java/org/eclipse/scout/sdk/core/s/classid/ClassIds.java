/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.classid;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.log.SdkLog;

/**
 * <h3>{@link ClassIds}</h3>
 *
 * @since 7.0.0
 */
public final class ClassIds {

  public static final List<Function<String, String>> STORE = new CopyOnWriteArrayList<>();
  static {
    STORE.add(context -> UUID.randomUUID().toString());
  }
  private static volatile boolean automaticallyCreateClassIdAnnotation;

  private ClassIds() {
  }

  /**
   * Gets a new class id for the given type. All class id providers are considered until the first provides a value.
   *
   * @param ownerTypeFqn
   *          The fully qualified name of the type for which the class id should be generated.
   * @return The new id or {@code null} if no provider returned an id.
   */
  public static String next(String ownerTypeFqn) {
    for (Function<String, String> gen : STORE) {
      try {
        String newId = gen.apply(ownerTypeFqn);
        if (newId != null) {
          return newId;
        }
      }
      catch (RuntimeException e) {
        SdkLog.warning("Exception in ClassIdProvider '{}'.", gen.getClass().getName(), e);
      }
    }
    return null;
  }

  /**
   * Gets a new class id for the given type context. If the automatic class id generation is disabled this method
   * returns {@code null}.
   *
   * @param context
   *          The context for which the new id should be generated.
   * @return The new id or {@code null}.
   */
  public static String nextIfEnabled(String context) {
    if (isAutomaticallyCreateClassIdAnnotation()) {
      return next(context);
    }
    return null;
  }

  /**
   * @return true if the {@code ClassId} annotation should be generated automatically, false otherwise.
   */
  public static boolean isAutomaticallyCreateClassIdAnnotation() {
    return automaticallyCreateClassIdAnnotation;
  }

  /**
   * Sets if the {@code ClassId} annotation should automatically be created.
   *
   * @param newValue
   *          true if it should be created automatically, false otherwise.
   */
  public static void setAutomaticallyCreateClassIdAnnotation(boolean newValue) {
    automaticallyCreateClassIdAnnotation = newValue;
  }
}
