/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.testing.context;

import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

/**
 * <h3>{@link UsernameExtension}</h3>
 * <p>
 * JUnit extension that sets {@value #USERNAME} as username for the current thread.
 *
 * @since 7.1.0
 * @see CoreUtils#setUsernameForThread(String)
 */
public class UsernameExtension implements BeforeAllCallback, AfterAllCallback {

  public static final String USERNAME_KEY = "scout_username";
  public static final String USERNAME = "anonymous";

  @Override
  public void afterAll(ExtensionContext context) {
    CoreUtils.setUsernameForThread(
        getStore(context)
            .remove(USERNAME_KEY, String.class));
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    getStore(context).put(USERNAME_KEY, CoreUtils.getUsername());
    CoreUtils.setUsernameForThread(USERNAME);
  }

  protected Store getStore(ExtensionContext context) {
    return context.getStore(Namespace.create(getClass(), context));
  }
}
