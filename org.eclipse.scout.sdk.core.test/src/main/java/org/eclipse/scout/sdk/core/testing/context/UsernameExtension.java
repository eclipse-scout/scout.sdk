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
package org.eclipse.scout.sdk.core.testing.context;

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

  public static final String USERNAME_KEY = "usrname";
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
