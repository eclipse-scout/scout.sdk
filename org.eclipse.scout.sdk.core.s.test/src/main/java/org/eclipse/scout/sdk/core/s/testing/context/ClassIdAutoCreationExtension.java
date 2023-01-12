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

import static org.eclipse.scout.sdk.core.s.classid.ClassIds.isAutomaticallyCreateClassIdAnnotation;
import static org.eclipse.scout.sdk.core.s.classid.ClassIds.setAutomaticallyCreateClassIdAnnotation;

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

/**
 * <h3>{@link ClassIdAutoCreationExtension}</h3>
 * <p>
 * Unit test extension that activates the automatic ClassId annotation creation.
 *
 * @since 7.1.0
 */
public class ClassIdAutoCreationExtension implements AfterAllCallback, BeforeAllCallback {

  public static final String AUTOMATICALLY_CREATE_CLASS_ID = "autoCreateClassId";

  @Override
  public void beforeAll(ExtensionContext context) {
    getStore(context).put(AUTOMATICALLY_CREATE_CLASS_ID, isAutomaticallyCreateClassIdAnnotation());
    setAutomaticallyCreateClassIdAnnotation(true);
  }

  @Override
  public void afterAll(ExtensionContext context) {
    setAutomaticallyCreateClassIdAnnotation(
        Optional.ofNullable(getStore(context).remove(AUTOMATICALLY_CREATE_CLASS_ID, Boolean.class))
            .orElse(Boolean.FALSE));
  }

  protected Store getStore(ExtensionContext context) {
    return context.getStore(Namespace.create(getClass(), context));
  }
}
