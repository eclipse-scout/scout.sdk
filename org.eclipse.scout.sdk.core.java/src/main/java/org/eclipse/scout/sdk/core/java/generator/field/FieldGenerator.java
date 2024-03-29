/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator.field;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.java.builder.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.java.builder.comment.JavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.java.builder.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.builder.member.IMemberBuilder;
import org.eclipse.scout.sdk.core.java.builder.member.MemberBuilder;
import org.eclipse.scout.sdk.core.java.generator.SimpleGenerators;
import org.eclipse.scout.sdk.core.java.generator.member.AbstractMemberGenerator;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IField;
import org.eclipse.scout.sdk.core.java.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.java.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.util.SourceRange;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link FieldGenerator}</h3>
 *
 * @since 6.1.0
 */
public class FieldGenerator<TYPE extends IFieldGenerator<TYPE>> extends AbstractMemberGenerator<TYPE> implements IFieldGenerator<TYPE> {

  public static final String SERIAL_VERSION_UID = "serialVersionUID";
  private JavaBuilderContextFunction<String> m_dataType;
  private ISourceGenerator<IExpressionBuilder<?>> m_valueGenerator;

  protected FieldGenerator() {
  }

  protected FieldGenerator(IField field, IWorkingCopyTransformer transformer) {
    super(field, transformer);
    withDataType(field.dataType().reference())
        .withValue(field.sourceOfInitializer()
            .map(SourceRange::asCharSequence)
            .<ISourceGenerator<IExpressionBuilder<?>>> map(ISourceGenerator::raw)
            .orElseGet(() -> field.constantValue()
                .map(mv -> SimpleGenerators.createMetaValueGenerator(mv, transformer))
                .orElse(null)));
  }

  /**
   * @return A new empty {@link IFieldGenerator}.
   */
  public static IFieldGenerator<?> create() {
    return new FieldGenerator<>();
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
   * @return An {@link IFieldGenerator} initialized to create a {@code serialVersionUID} field with the value {@code 1}.
   */
  public static IFieldGenerator<?> createSerialVersionUid() {
    return createSerialVersionUid(1);
  }

  public static IFieldGenerator<?> createSerialVersionUid(long value) {
    return create()
        .withElementName(SERIAL_VERSION_UID)
        .withDataType(JavaTypes._long)
        .asPrivate()
        .asStatic()
        .asFinal()
        .withValue(ISourceGenerator.raw(value + "L"));
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    super.build(builder);
    buildFieldSource(MemberBuilder.create(builder));
  }

  protected void buildFieldSource(IMemberBuilder<?> builder) {
    var elementName = elementName(builder.context()).filter(Strings::hasText);
    if (elementName.isPresent()) {
      builder
          .appendFlags(flags())
          .refFunc(dataTypeFunc().orElseThrow(() -> newFail("Field data type missing for builder {}", this)))
          .space()
          .append(ensureValidJavaName(elementName.orElseThrow()));
      value().ifPresent(v -> {
        builder.equalSign();
        buildFieldValue(ExpressionBuilder.create(builder), v);
      });
      builder.semicolon();
    }
    else if (value().isPresent()) {
      // static constructors
      buildFieldValue(ExpressionBuilder.create(builder), value().orElseThrow());
    }
  }

  protected static void buildFieldValue(IExpressionBuilder<?> builder, ISourceGenerator<IExpressionBuilder<?>> valueGenerator) {
    valueGenerator.generate(builder);
  }

  @Override
  public Optional<String> dataType() {
    return dataTypeFunc().flatMap(JavaBuilderContextFunction::apply);
  }

  @Override
  public Optional<String> dataType(IJavaBuilderContext context) {
    return dataTypeFunc().map(f -> f.apply(context));
  }

  @Override
  public Optional<JavaBuilderContextFunction<String>> dataTypeFunc() {
    return Optional.ofNullable(m_dataType);
  }

  @Override
  public TYPE withDataType(String dataType) {
    m_dataType = JavaBuilderContextFunction.orNull(dataType);
    return thisInstance();
  }

  @Override
  public <A extends IApiSpecification> TYPE withDataTypeFrom(Class<A> apiDefinition, Function<A, String> dataTypeSupplier) {
    m_dataType = new ApiFunction<>(apiDefinition, dataTypeSupplier);
    return thisInstance();
  }

  @Override
  public TYPE withDataTypeFunc(Function<IJavaBuilderContext, String> dataTypeSupplier) {
    m_dataType = JavaBuilderContextFunction.orNull(dataTypeSupplier);
    return thisInstance();
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
    return thisInstance();
  }
}
