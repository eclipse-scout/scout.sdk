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
package org.eclipse.scout.sdk.core.builder.java.comment;

import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.imports.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.JavaTypes.ReferenceParser;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link JavaElementCommentBuilder}</h3>
 *
 * @since 6.1.0
 */
public class JavaElementCommentBuilder<TYPE extends IJavaElementCommentBuilder<TYPE>> extends CommentBuilder<TYPE> implements IJavaElementCommentBuilder<TYPE> {

  private static volatile IDefaultElementCommentGeneratorSpi commentGeneratorSpi;
  public static final String LINK_PREFIX = "{@link ";
  public static final char LINK_SUFFIX = '}';
  public static final char LINK_MEMBER_DELIMITER = '#';
  private static final Pattern LINK_REFERENCE_PATTERN = Pattern.compile("([\\w.\\[\\]]+)?(?:" + LINK_MEMBER_DELIMITER + "([\\w]+)\\(([\\w\\s,.\\[\\]]*)\\))?");

  private final Supplier<ISourceGenerator<ICommentBuilder<?>>> m_defaultCommentGeneratorSupplier;
  private final FinalValue<ISourceGenerator<ICommentBuilder<?>>> m_defaultElementCommentGenerator;
  private final IJavaBuilderContext m_context;

  protected JavaElementCommentBuilder(ISourceBuilder<?> inner, Supplier<ISourceGenerator<ICommentBuilder<?>>> defaultCommentGeneratorSupplier) {
    super(inner);
    m_defaultElementCommentGenerator = new FinalValue<>();
    m_defaultCommentGeneratorSupplier = Ensure.notNull(defaultCommentGeneratorSupplier);
    var context = inner.context();
    if (context instanceof IJavaBuilderContext) {
      m_context = (IJavaBuilderContext) context;
    }
    else {
      m_context = null;
    }
  }

  public static IDefaultElementCommentGeneratorSpi getCommentGeneratorSpi() {
    return commentGeneratorSpi;
  }

  public static void setCommentGeneratorSpi(IDefaultElementCommentGeneratorSpi newCommentGeneratorSpi) {
    commentGeneratorSpi = newCommentGeneratorSpi;
  }

  static JavaElementCommentBuilder<?> newJavaElementCommentBuilder(ISourceBuilder<?> inner, Function<IDefaultElementCommentGeneratorSpi, ISourceGenerator<ICommentBuilder<?>>> mapper) {
    return new JavaElementCommentBuilder<>(inner, () -> Optional.ofNullable(getCommentGeneratorSpi())
        .map(mapper)
        .orElse(ISourceGenerator.empty()));
  }

  /**
   * @param inner
   *          The comment will be appended to this inner builder.
   * @return A new JavaElementCommentBuilder that creates an empty default comment.
   */
  public static IJavaElementCommentBuilder<?> create(ISourceBuilder<?> inner) {
    return new JavaElementCommentBuilder<>(inner, ISourceGenerator::empty);
  }

  /**
   * @param inner
   *          The comment will be appended to this inner builder.
   * @param target
   *          The {@link ICompilationUnitGenerator} for which the default IJavaElementCommentBuilder should be created.
   * @return A new {@link IJavaElementCommentBuilder} that creates default comments for the given
   *         {@link ICompilationUnitGenerator}.
   */
  public static IJavaElementCommentBuilder<?> createForCompilationUnit(ISourceBuilder<?> inner, ICompilationUnitGenerator<?> target) {
    return newJavaElementCommentBuilder(inner, spi -> spi.createCompilationUnitComment(target));
  }

  /**
   * @param inner
   *          The comment will be appended to this inner builder.
   * @param target
   *          The {@link ITypeGenerator} for which the default IJavaElementCommentBuilder should be created.
   * @return A new {@link IJavaElementCommentBuilder} that creates default comments for the given
   *         {@link ITypeGenerator}.
   */
  public static IJavaElementCommentBuilder<?> createForType(ISourceBuilder<?> inner, ITypeGenerator<?> target) {
    return newJavaElementCommentBuilder(inner, spi -> spi.createTypeComment(target));
  }

  /**
   * @param inner
   *          The comment will be appended to this inner builder.
   * @param target
   *          The {@link IMethodGenerator} for which the default IJavaElementCommentBuilder should be created.
   * @return A new {@link IJavaElementCommentBuilder} that creates default comments for the given
   *         {@link IMethodGenerator}.
   */
  public static IJavaElementCommentBuilder<?> createForMethod(ISourceBuilder<?> inner, IMethodGenerator<?, ?> target) {
    return newJavaElementCommentBuilder(inner, spi -> spi.createMethodComment(target));
  }

  /**
   * @param inner
   *          The comment will be appended to this inner builder.
   * @param target
   *          The getter {@link IMethodGenerator} for which the default IJavaElementCommentBuilder should be created.
   * @return A new {@link IJavaElementCommentBuilder} that creates default comments for the given getter
   *         {@link IMethodGenerator}.
   */
  public static IJavaElementCommentBuilder<?> createForMethodGetter(ISourceBuilder<?> inner, IMethodGenerator<?, ?> target) {
    return newJavaElementCommentBuilder(inner, spi -> spi.createGetterMethodComment(target));
  }

  /**
   * @param inner
   *          The comment will be appended to this inner builder.
   * @param target
   *          The setter {@link IMethodGenerator} for which the default IJavaElementCommentBuilder should be created.
   * @return A new {@link IJavaElementCommentBuilder} that creates default comments for the given setter
   *         {@link IMethodGenerator}.
   */
  public static IJavaElementCommentBuilder<?> createForMethodSetter(ISourceBuilder<?> inner, IMethodGenerator<?, ?> target) {
    return newJavaElementCommentBuilder(inner, spi -> spi.createSetterMethodComment(target));
  }

  /**
   * @param inner
   *          The comment will be appended to this inner builder.
   * @param target
   *          The {@link IFieldGenerator} for which the default IJavaElementCommentBuilder should be created.
   * @return A new {@link IJavaElementCommentBuilder} that creates default comments for the given
   *         {@link IFieldGenerator}.
   */
  public static IJavaElementCommentBuilder<?> createForField(ISourceBuilder<?> inner, IFieldGenerator<?> target) {
    return newJavaElementCommentBuilder(inner, spi -> spi.createFieldComment(target));
  }

  @Override
  public TYPE appendDefaultElementComment() {
    return append(defaultElementComment().generalize(CommentBuilder::create));
  }

  public ISourceGenerator<ICommentBuilder<?>> defaultElementComment() {
    return m_defaultElementCommentGenerator.computeIfAbsentAndGet(m_defaultCommentGeneratorSupplier);
  }

  @Override
  public TYPE appendLink(IType ref) {
    return appendLink(ref, null);
  }

  @Override
  public TYPE appendLink(IType ref, CharSequence label) {
    if (ref == null) {
      return thisInstance();
    }
    return appendLink(ref.reference(), label);
  }

  @Override
  public TYPE appendLink(CharSequence ref) {
    return appendLink(ref, null);
  }

  @Override
  public TYPE appendLink(CharSequence ref, CharSequence label) {
    if (!Strings.hasText(ref)) {
      return thisInstance();
    }

    if (Strings.indexOf(LINK_MEMBER_DELIMITER, ref) < 0 && Strings.lastIndexOf(JavaTypes.C_GENERIC_END, ref) > 0) {
      // it is a type reference containing arguments. Create links for each type used
      // limitations: - only supports links to types (no members)
      //              - label is ignored (not clear to which link it belongs).
      var p = new ReferenceParser((fqn, typeArgDepth) -> createTypeLink(fqn));
      var refWithLinks = p.useReference(ref);
      return append(refWithLinks);
    }

    var useLink = isLinkPossible(ref);
    if (useLink) {
      append(LINK_PREFIX);
    }
    if (m_context != null) {
      appendLinkReference(m_context.validator(), ref);
    }
    else {
      append(ref);
    }
    if (useLink) {
      if (Strings.hasText(label)) {
        if (!Character.isWhitespace(label.charAt(0))) {
          append(' ');
        }
        append(label);
      }
      append(LINK_SUFFIX);
    }
    return thisInstance();
  }

  protected static boolean isLinkPossible(CharSequence ref) {
    return !JavaTypes.isPrimitive(ref) && !JavaTypes.isArray(ref) && !JavaTypes.isWildcard(ref);
  }

  protected void appendLinkReference(IImportValidator validator, CharSequence ref) {
    var m = LINK_REFERENCE_PATTERN.matcher(JavaTypes.erasure(ref));
    if (!m.matches()) {
      append(ref); // cannot parse as reference
      return;
    }
    var className = m.group(1);
    var member = m.group(2);
    var args = m.group(3);
    if (Strings.hasText(className)) {
      append(validator.useReference(className));
    }
    if (Strings.hasText(member)) {
      append(LINK_MEMBER_DELIMITER).append(member).append('(');
      appendArgumentNames(validator, args);
      append(')');
    }
  }

  protected CharSequence createTypeLink(CharSequence fqn) {
    var ref = m_context != null ? m_context.validator().useReference(fqn) : fqn;
    return LINK_PREFIX + ref + LINK_SUFFIX;
  }

  protected void appendArgumentNames(IImportValidator validator, String args) {
    if (!Strings.hasText(args)) {
      return;
    }
    var arguments = new StringTokenizer(args, String.valueOf(JavaTypes.C_COMMA));
    var arg = Strings.trim(arguments.nextToken());
    append(validator.useReference(arg));
    while (arguments.hasMoreTokens()) {
      arg = Strings.trim(arguments.nextToken());
      append(", ").append(validator.useReference(arg));
    }
  }
}
