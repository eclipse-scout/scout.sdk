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

import static org.eclipse.scout.sdk.core.java.builder.comment.JavaElementCommentBuilder.getCommentGeneratorSpi;
import static org.eclipse.scout.sdk.core.java.builder.comment.JavaElementCommentBuilder.setCommentGeneratorSpi;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.builder.comment.IDefaultElementCommentGeneratorSpi;
import org.eclipse.scout.sdk.core.java.builder.comment.JavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.java.testing.TestingElementCommentGenerator;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

/**
 * <h3>{@link DefaultCommentGeneratorExtension}</h3>
 * <p>
 * JUnit extension that sets the {@link TestingElementCommentGenerator} as {@link IDefaultElementCommentGeneratorSpi} to
 * be used by {@link ISourceGenerator}s.
 *
 * @since 7.1.0
 * @see JavaElementCommentBuilder#setCommentGeneratorSpi(IDefaultElementCommentGeneratorSpi)
 */
public class DefaultCommentGeneratorExtension implements BeforeAllCallback, AfterAllCallback {

  public static final String COMMENT_GENERATOR_SPI = "commentGeneratorSpi";

  @Override
  public void afterAll(ExtensionContext context) {
    setCommentGeneratorSpi(
        getStore(context)
            .remove(COMMENT_GENERATOR_SPI, IDefaultElementCommentGeneratorSpi.class));
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    getStore(context).put(COMMENT_GENERATOR_SPI, getCommentGeneratorSpi());
    setCommentGeneratorSpi(new TestingElementCommentGenerator());
  }

  protected Store getStore(ExtensionContext context) {
    return context.getStore(Namespace.create(getClass(), context));
  }
}
