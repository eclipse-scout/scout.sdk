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
package org.eclipse.scout.sdk.core.testing.context;

import static org.eclipse.scout.sdk.core.builder.java.comment.JavaElementCommentBuilder.getCommentGeneratorSpi;
import static org.eclipse.scout.sdk.core.builder.java.comment.JavaElementCommentBuilder.setCommentGeneratorSpi;

import org.eclipse.scout.sdk.core.builder.java.comment.IDefaultElementCommentGeneratorSpi;
import org.eclipse.scout.sdk.core.builder.java.comment.JavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.testing.TestingElementCommentGenerator;
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
