/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.generator.field;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.JavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.builder.java.member.IMemberBuilder;
import org.eclipse.scout.sdk.core.builder.java.member.MemberBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.member.AbstractMemberGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.generator.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link FieldGenerator}</h3>
 *
 * @since 6.1.0
 */
public class FieldGenerator<TYPE extends IFieldGenerator<TYPE>> extends AbstractMemberGenerator<TYPE> implements IFieldGenerator<TYPE> {

  private String m_dataType;
  private ISourceGenerator<IExpressionBuilder<?>> m_valueGenerator;

  protected FieldGenerator() {
  }

  protected FieldGenerator(IField field, IWorkingCopyTransformer transformer) {
    super(field, transformer);
    withDataType(field.dataType().reference())
        .withValue(field.sourceOfInitializer()
            .map(ISourceRange::asCharSequence)
            .<ISourceGenerator<IExpressionBuilder<?>>> map(ISourceGenerator::raw)
            .orElseGet(() -> field.constantValue()
                .map(mv -> mv.toWorkingCopy(transformer))
                .orElse(null)));
  }

  /**
   * Creates a new {@link IFieldGenerator} based on the given {@link IField}.
   * <p>
   * <b>Example:</b> See {@link IWorkingCopyTransformer}.
   *
   * @param field
   *          The {@link IField} that should be converted to an {@link IFieldGenerator}. Must not be {@code null}.
   * @param transformer
   *          An optional {@link IWorkingCopyTransformer} callback that is responsible for transforming the field to a
   *          working copy. May be {@code null} if no custom transformation is required and the field should be
   *          converted into a working copy without any modification.
   * @return A new {@link IFieldGenerator} initialized to generate source that is structurally similar to the one from
   *         the given {@link IField}.
   * @see DefaultWorkingCopyTransformer
   * @see SimpleWorkingCopyTransformerBuilder
   */
  public static IFieldGenerator<?> create(IField field, IWorkingCopyTransformer transformer) {
    return new FieldGenerator<>(field, transformer);
  }

  /**
   * @return A new empty {@link IFieldGenerator}.
   */
  public static IFieldGenerator<?> create() {
    return new FieldGenerator<>();
  }

  /**
   * @return An {@link IFieldGenerator} initialized to create a {@code serialVersionUID} field.
   */
  public static IFieldGenerator<?> createSerialVersionUid() {
    return create()
        .withElementName("serialVersionUID")
        .withDataType(JavaTypes._long)
        .withFlags(Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal)
        .withValue(ISourceGenerator.raw("1L"));
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    super.build(builder);
    buildFieldSource(MemberBuilder.create(builder));
  }

  protected void buildFieldSource(IMemberBuilder<?> builder) {
    if (elementName().isPresent()) {
      builder
          .appendFlags(flags())
          .ref(dataType().orElseThrow(() -> newFail("Field data type missing for builder {}", this)))
          .space()
          .append(ensureValidJavaName(elementName().get()));
      if (value().isPresent()) {
        builder.equalSign();
        buildFieldValue(ExpressionBuilder.create(builder), value().get());
      }
      builder.semicolon();
    }
    else if (value().isPresent()) {
      // static constructors
      buildFieldValue(ExpressionBuilder.create(builder), value().get());
    }
  }

  protected static void buildFieldValue(IExpressionBuilder<?> builder, ISourceGenerator<IExpressionBuilder<?>> valueGenerator) {
    valueGenerator.generate(builder);
  }

  @Override
  public Optional<String> dataType() {
    return Optional.ofNullable(m_dataType);
  }

  @Override
  public TYPE withDataType(String reference) {
    m_dataType = reference;
    return currentInstance();
  }

  @Override
  public Optional<ISourceGenerator<IExpressionBuilder<?>>> value() {
    return Optional.ofNullable(m_valueGenerator);
  }

  @Override
  public TYPE asVolatile() {
    return withFlags(Flags.AccVolatile);
  }

  @Override
  public TYPE asTransient() {
    return withFlags(Flags.AccTransient);
  }

  @Override
  protected IJavaElementCommentBuilder<?> createCommentBuilder(ISourceBuilder<?> builder) {
    return JavaElementCommentBuilder.createForField(builder, this);
  }

  @Override
  public TYPE withValue(ISourceGenerator<IExpressionBuilder<?>> valueGenerator) {
    m_valueGenerator = valueGenerator;
    return currentInstance();
  }
}
