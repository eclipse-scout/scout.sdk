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
package org.eclipse.scout.sdk.core.generator.methodparam;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.member.IMemberBuilder;
import org.eclipse.scout.sdk.core.builder.java.member.MemberBuilder;
import org.eclipse.scout.sdk.core.generator.AbstractAnnotatableGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link MethodParameterGenerator}</h3>
 *
 * @since 6.1.0
 */
public class MethodParameterGenerator<TYPE extends IMethodParameterGenerator<TYPE>> extends AbstractAnnotatableGenerator<TYPE> implements IMethodParameterGenerator<TYPE> {

  private boolean m_isFinal;
  private boolean m_isVarargs;
  private ApiFunction<?, String> m_dataType;

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

  protected MethodParameterGenerator() {
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

  /**
   * @return A new empty {@link IMethodParameterGenerator}.
   */
  public static IMethodParameterGenerator<?> create() {
    return new MethodParameterGenerator<>();
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

    var dataType = dataType().orElseThrow(() -> newFail("Method parameter data type missing for generator {}", this))
        .apply(builder.context())
        .filter(Strings::hasText)
        .orElseThrow(() -> newFail("Unable to get method parameter data type for generator {}", this));
    builder.ref(dataType);

    if (isVarargs()) {
      builder.append("...");
    }
    var parameterName = ensureValidJavaName(elementName().orElseThrow(() -> newFail("Method parameter name missing for generator {}", this)));
    builder.space()
        .append(parameterName);
  }

  @Override
  protected String annotationDelimiter(ISourceBuilder<?> builder) {
    return String.valueOf(JavaTypes.C_SPACE);
  }

  @Override
  public Optional<ApiFunction<?, String>> dataType() {
    return Optional.ofNullable(m_dataType);
  }

  @Override
  public TYPE withDataType(String dataType) {
    return withDataTypeFrom(null, api -> dataType);
  }

  @Override
  public <A extends IApiSpecification> TYPE withDataTypeFrom(Class<A> apiDefinition, Function<A, String> dataTypeSupplier) {
    if (dataTypeSupplier == null) {
      m_dataType = null;
    }
    else {
      m_dataType = new ApiFunction<>(apiDefinition, dataTypeSupplier);
    }
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
}
