/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.testing;

import org.eclipse.scout.sdk.core.builder.IBuilderContext;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.comment.ICommentBuilder;
import org.eclipse.scout.sdk.core.java.builder.comment.IDefaultElementCommentGeneratorSpi;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;

/**
 * <h3>{@link TestingElementCommentGenerator}</h3>
 *
 * @since 6.1.0
 */
@SuppressWarnings("HardcodedLineSeparator")
public class TestingElementCommentGenerator implements IDefaultElementCommentGeneratorSpi {

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createCompilationUnitComment(ICompilationUnitGenerator<?> target) {
    return b -> b.appendJavaDocComment("Default Testing Comment for\nCompilationUnit " + target.fileName().orElse(""));
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createTypeComment(ITypeGenerator<?> target) {
    return b -> b.appendJavaDocComment("Default Testing Comment for\nType " + target.elementName(toJavaBuilderContext(b.context())).orElse(""));
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createMethodComment(IMethodGenerator<?, ?> target) {
    return b -> b.appendJavaDocComment("Default Testing Comment for\nMethod " + target.elementName(toJavaBuilderContext(b.context())).orElse(""));
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createGetterMethodComment(IMethodGenerator<?, ?> target) {
    return b -> b.appendJavaDocComment("Default Testing Comment for\nGetter " + target.elementName(toJavaBuilderContext(b.context())).orElse(""));
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createSetterMethodComment(IMethodGenerator<?, ?> target) {
    return b -> b.appendJavaDocComment("Default Testing Comment for\nSetter " + target.elementName(toJavaBuilderContext(b.context())).orElse(""));
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createFieldComment(IFieldGenerator<?> target) {
    return b -> b.appendJavaDocComment("Default Testing Comment for\nField " + target.elementName(toJavaBuilderContext(b.context())).orElse(""));
  }

  protected static IJavaBuilderContext toJavaBuilderContext(IBuilderContext context) {
    if (context instanceof IJavaBuilderContext) {
      return (IJavaBuilderContext) context;
    }
    return null;
  }
}
