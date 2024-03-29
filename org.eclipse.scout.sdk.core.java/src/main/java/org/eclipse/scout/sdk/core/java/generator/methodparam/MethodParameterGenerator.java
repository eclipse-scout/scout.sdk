/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator.methodparam;

import static org.eclipse.scout.sdk.core.java.JavaTypes.arrayMarker;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.java.builder.member.IMemberBuilder;
import org.eclipse.scout.sdk.core.java.builder.member.MemberBuilder;
import org.eclipse.scout.sdk.core.java.generator.AbstractAnnotatableGenerator;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.java.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.java.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link MethodParameterGenerator}</h3>
 *
 * @since 6.1.0
 */
public class MethodParameterGenerator<TYPE extends IMethodParameterGenerator<TYPE>> extends AbstractAnnotatableGenerator<TYPE> implements IMethodParameterGenerator<TYPE> {

  private boolean m_isFinal;
  private boolean m_isVarargs;
  private JavaBuilderContextFunction<String> m_dataType;

  protected MethodParameterGenerator() {
  }

  protected MethodParameterGenerator(IMethodParameter parameter, IWorkingCopyTransformer transformer) {
    super(parameter, transformer);
    var declaringMethod = parameter.declaringMethod();
    var isVarargs = Flags.isVarargs(declaringMethod.flags()) && parameter.index() == declaringMethod.parameters().stream().count() - 1;
    var dataType = parameter.dataType().reference();
    if (isVarargs) {
      dataType = dataType.substring(0, dataType.length() - 2); // remove one array dimension because it is printed as varargs
    }

    asFinal(Flags.isFinal(parameter.flags()))
        .asVarargs(isVarargs)
        .withDataType(dataType);
  }

  /**
   * @return A new empty {@link IMethodParameterGenerator}.
   */
  public static IMethodParameterGenerator<?> create() {
    return new MethodParameterGenerator<>();
  }

  /**
   * Creates a new {@link IMethodParameterGenerator} based on the given {@link IMethodParameter}.
   * <p>
   * <b>Example:</b> See {@link IWorkingCopyTransformer}.
   *
   * @param parameter
   *          The {@link IMethodParameter} that should be converted to an {@link IMethodParameterGenerator}. Must not be
   *          {@code null}.
   * @param transformer
   *          An optional {@link IWorkingCopyTransformer} callback that is responsible for transforming the method
   *          parameter to a working copy. May be {@code null} if no custom transformation is required and the method
   *          parameter should be converted into a working copy without any modification.
   * @return A new {@link IMethodParameterGenerator} initialized to generate source that is structurally similar to the
   *         one from the given {@link IMethodParameter}.
   * @see DefaultWorkingCopyTransformer
   * @see SimpleWorkingCopyTransformerBuilder
   */
  public static IMethodParameterGenerator<?> create(IMethodParameter parameter, IWorkingCopyTransformer transformer) {
    return new MethodParameterGenerator<>(parameter, transformer);
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    super.build(builder);
    createParameterSource(MemberBuilder.create(builder));
  }

  protected void createParameterSource(IMemberBuilder<?> builder) {
    if (isFinal()) {
      builder.appendFlags(Flags.AccFinal);
    }

    var dataType = dataTypeFunc().orElseThrow(() -> newFail("Method parameter data type missing for generator {}", this))
        .apply(builder.context());
    builder.ref(dataType);

    if (isVarargs()) {
      builder.append("...");
    }
    var parameterName = ensureValidJavaName(elementName(builder.context()).orElseThrow(() -> newFail("Method parameter name missing for generator {}", this)));
    builder.space()
        .append(parameterName);
  }

  @Override
  protected String annotationDelimiter(ISourceBuilder<?> builder) {
    return String.valueOf(JavaTypes.C_SPACE);
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
  public boolean isFinal() {
    return m_isFinal;
  }

  @Override
  public TYPE asFinal() {
    return asFinal(true);
  }

  @Override
  public TYPE notFinal() {
    return asFinal(false);
  }

  @Override
  public TYPE asFinal(boolean newFinalValue) {
    m_isFinal = newFinalValue;
    return thisInstance();
  }

  @Override
  public boolean isVarargs() {
    return m_isVarargs;
  }

  @Override
  public TYPE asVarargs() {
    return asVarargs(true);
  }

  @Override
  public TYPE notVarargs() {
    return asVarargs(false);
  }

  @Override
  public TYPE asVarargs(boolean newVarargsValue) {
    m_isVarargs = newVarargsValue;
    return thisInstance();
  }

  @Override
  public String reference(IJavaBuilderContext context) {
    return reference(context, false);
  }

  @Override
  public String reference(IJavaBuilderContext context, boolean useErasureOnly) {
    var dataTypeFunc = dataTypeFunc().orElseThrow(() -> newFail("Cannot calculate the method parameter reference because the datatype is missing."));
    var typeString = Strings.notEmpty(dataTypeFunc.apply(context)).orElseThrow(() -> newFail("Cannot compute parameter data type of method parameter '{}'.", elementName(context).orElse(null)));
    if (useErasureOnly) {
      typeString = JavaTypes.erasure(typeString);
    }
    if (isVarargs()) {
      typeString += arrayMarker();
    }
    return typeString;
  }
}
