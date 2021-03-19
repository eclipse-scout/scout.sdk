/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.testing;

import org.eclipse.scout.sdk.core.builder.java.comment.ICommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IDefaultElementCommentGeneratorSpi;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;

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
    return b -> b.appendJavaDocComment("Default Testing Comment for\nType " + target.elementName().orElse(""));
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createMethodComment(IMethodGenerator<?, ?> target) {
    return b -> b.appendJavaDocComment("Default Testing Comment for\nMethod " + target.elementName().orElse(""));
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createGetterMethodComment(IMethodGenerator<?, ?> target) {
    return b -> b.appendJavaDocComment("Default Testing Comment for\nGetter " + target.elementName().orElse(""));
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createSetterMethodComment(IMethodGenerator<?, ?> target) {
    return b -> b.appendJavaDocComment("Default Testing Comment for\nSetter " + target.elementName().orElse(""));
  }

  @Override
  public ISourceGenerator<ICommentBuilder<?>> createFieldComment(IFieldGenerator<?> target) {
    return b -> b.appendJavaDocComment("Default Testing Comment for\nField " + target.elementName().orElse(""));
  }

}
