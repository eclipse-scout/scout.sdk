/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.testing.context;

import java.util.function.Function;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.s.uniqueid.UniqueIds;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * <h3>{@link UniqueIdExtension}</h3>
 * <p>
 * Unit test extension that adds a primary unique id provider for {@link Long} types. It always returns
 * {@value #UNIQUE_ID} as id.
 *
 * @since 7.1.0
 * @see UniqueIds
 */
public class UniqueIdExtension implements BeforeAllCallback, AfterAllCallback {

  public static final String UNIQUE_ID_PROVIDER = "uniqueIdProvider";
  public static final long UNIQUE_ID = 12345L;
  public static final Function<String, String> TESTING_ID_PROVIDER = type -> {
    if (JavaTypes.Long.equals(type)) {
      return UNIQUE_ID + "L";
    }
    return null;
  };

  @Override
  public void afterAll(ExtensionContext context) {
    UniqueIds.removeIdProvider(TESTING_ID_PROVIDER);
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    UniqueIds.registerIdProvider(TESTING_ID_PROVIDER);
  }
}
