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
package org.eclipse.scout.sdk.core.generator;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.JavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.JavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link AbstractJavaElementGenerator}</h3>
 *
 * @since 6.1.0
 */
public abstract class AbstractJavaElementGenerator<TYPE extends IJavaElementGenerator<TYPE>> implements IJavaElementGenerator<TYPE> {

  private String m_elementName;
  private ISourceGenerator<IJavaElementCommentBuilder<?>> m_comment;

  protected AbstractJavaElementGenerator() {
  }

  protected AbstractJavaElementGenerator(IJavaElement element) {
    withElementName(Ensure.notNull(element).elementName());
  }

  @SuppressWarnings("unchecked")
  protected TYPE thisInstance() {
    return (TYPE) this;
  }

  @Override
  public TYPE withElementName(String newName) {
    m_elementName = newName;
    return thisInstance();
  }

  @Override
  public Optional<String> elementName() {
    return Strings.notBlank(m_elementName);
  }

  protected void build(IJavaSourceBuilder<?> builder) {
    createComment(builder);
  }

  protected void createComment(ISourceBuilder<?> builder) {
    comment()
        .map(b -> b.generalize(this::createCommentBuilder))
        .ifPresent(builder::append);
  }

  protected IJavaElementCommentBuilder<?> createCommentBuilder(ISourceBuilder<?> builder) {
    return JavaElementCommentBuilder.create(builder);
  }

  public static IJavaSourceBuilder<?> ensureJavaSourceBuilder(ISourceBuilder<?> inner) {
    if (inner instanceof IJavaSourceBuilder<?>) {
      return (IJavaSourceBuilder<?>) inner;
    }
    return JavaSourceBuilder.create(inner);
  }

  /**
   * If the given name is a reserved java keyword a prefix is added to ensure it is a valid name to use e.g. for
   * variables or parameters.
   *
   * @param parameterName
   *          The original name.
   * @return The new value which probably has a prefix appended.
   */
  public static String ensureValidJavaName(String parameterName) {
    if (JavaTypes.isReservedJavaKeyword(parameterName)) {
      return parameterName + '_';
    }
    return parameterName;
  }

  @Override
  public final void generate(ISourceBuilder<?> builder) {
    build(ensureJavaSourceBuilder(builder));
  }

  @Override
  public TYPE withComment(ISourceGenerator<IJavaElementCommentBuilder<?>> commentBuilder) {
    m_comment = commentBuilder;
    return thisInstance();
  }

  @Override
  public Optional<ISourceGenerator<IJavaElementCommentBuilder<?>>> comment() {
    return Optional.ofNullable(m_comment);
  }
}
