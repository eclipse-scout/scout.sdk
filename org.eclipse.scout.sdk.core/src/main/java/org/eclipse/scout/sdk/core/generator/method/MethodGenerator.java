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
package org.eclipse.scout.sdk.core.generator.method;

import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.model.api.Flags.isAbstract;
import static org.eclipse.scout.sdk.core.model.api.Flags.isDefaultMethod;
import static org.eclipse.scout.sdk.core.model.api.Flags.isInterface;
import static org.eclipse.scout.sdk.core.model.api.Flags.isVarargs;
import static org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.transformMethodParameter;
import static org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.transformTypeParameter;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.builder.java.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.body.MethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.JavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.builder.java.member.IMemberBuilder;
import org.eclipse.scout.sdk.core.builder.java.member.MemberBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.member.AbstractMemberGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.util.apidef.IApiSpecification;

/**
 * <h3>{@link MethodGenerator}</h3>
 *
 * @since 6.1.0
 */
public class MethodGenerator<TYPE extends IMethodGenerator<TYPE, BODY>, BODY extends IMethodBodyBuilder<?>> extends AbstractMemberGenerator<TYPE> implements IMethodGenerator<TYPE, BODY> {

  protected final List<IMethodParameterGenerator<?>> m_parameters;
  protected final List<ITypeParameterGenerator<?>> m_typeParameters;
  protected final List<ApiFunction<?, String>> m_exceptions;

  private ApiFunction<?, String> m_returnType;
  private ISourceGenerator<BODY> m_body;
  private ApiFunction<?, String> m_methodName;

  protected MethodGenerator() {
    m_parameters = new ArrayList<>();
    m_typeParameters = new ArrayList<>();
    m_exceptions = new ArrayList<>();
  }

  protected MethodGenerator(IMethod method, IWorkingCopyTransformer transformer) {
    super(method, transformer);

    m_returnType = method.returnType()
        .map(IType::reference)
        .map(ApiFunction::new)
        .orElse(null);

    m_typeParameters = method.typeParameters()
        .map(p -> transformTypeParameter(p, transformer))
        .flatMap(Optional::stream)
        .collect(toList());

    m_parameters = method.parameters().stream()
        .map(mp -> transformMethodParameter(mp, transformer))
        .flatMap(Optional::stream)
        .collect(toList());

    m_exceptions = method.exceptionTypes()
        .map(IType::reference)
        .map(ApiFunction::new)
        .collect(toList());

    if (canHaveBody(method.flags())) {
      m_body = method.sourceOfBody()
          .map(ISourceRange::asCharSequence)
          .<ISourceGenerator<BODY>> map(ISourceGenerator::raw)
          .orElse(null);
    }

    // add interface flag on method if declaring type is an interface
    if (method.requireDeclaringType().isInterface()) {
      withFlags(flags() | Flags.AccInterface);
    }
  }

  /**
   * Creates a new {@link IMethodGenerator} based on the given {@link IMethod}.
   * <p>
   * <b>Example:</b> See {@link IWorkingCopyTransformer}.
   *
   * @param method
   *          The {@link IMethod} that should be converted to an {@link IMethodGenerator}. Must not be {@code null}.
   * @param transformer
   *          An optional {@link IWorkingCopyTransformer} callback that is responsible for transforming the method to a
   *          working copy. May be {@code null} if no custom transformation is required and the method should be
   *          converted into a working copy without any modification.
   * @return A new {@link IMethodGenerator} initialized to generate source that is structurally similar to the one from
   *         the given {@link IMethod}.
   * @see DefaultWorkingCopyTransformer
   * @see SimpleWorkingCopyTransformerBuilder
   */
  public static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> create(IMethod method, IWorkingCopyTransformer transformer) {
    return new MethodGenerator<>(method, transformer);
  }

  /**
   * @return A new empty {@link IMethodGenerator}.
   */
  public static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> create() {
    return new MethodGenerator<>();
  }

  /**
   * Creates an {@link IMethodGenerator} that creates a getter for the specified {@link IFieldGenerator}.
   *
   * @param fieldGenerator
   *          The {@link IFieldGenerator} for which the getter should be created. Must not be {@code null}.
   * @return The new {@link IMethodGenerator}.
   */
  @SuppressWarnings("unchecked")
  public static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createGetter(IFieldGenerator<?> fieldGenerator) {
    ApiFunction<IApiSpecification, String> dataType = (ApiFunction<IApiSpecification, String>) fieldGenerator.dataType()
        .orElseThrow(() -> newFail("Cannot create getter for field because it has no data type."));
    String fieldName = Ensure.notNull(fieldGenerator).elementName().orElseThrow(() -> newFail("Cannot create getter for field because it has no name."));
    return createGetter(fieldName, dataType.apiClass().orElse(null), dataType.apiFunction(), Flags.AccPublic);
  }

  /**
   * Creates an {@link IMethodGenerator} that creates a public getter for a field with specified name and type.
   *
   * @param fieldName
   *          The name of the field the getter should return. Must not be blank.
   * @param dataType
   *          The data type of the specified field.
   * @return A new {@link IMethodGenerator} that creates a public getter for the specified field.
   */
  public static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createGetter(String fieldName, String dataType) {
    return createGetter(fieldName, dataType, Flags.AccPublic);
  }

  /**
   * Creates an {@link IMethodGenerator} that creates a getter for a field with specified name and type.
   *
   * @param fieldName
   *          The name of the field the getter should return. Must not be blank.
   * @param dataType
   *          The data type of the specified field.
   * @param flags
   *          The flags of the getter. see {@link Flags}.
   * @return A new {@link IMethodGenerator} that creates a getter for the specified field.
   */
  public static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createGetter(String fieldName, String dataType, int flags) {
    return createGetter(fieldName, null, api -> dataType, flags);
  }

  public static <A extends IApiSpecification> IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createGetter(String fieldName, Class<A> apiDefinition, Function<A, String> dataTypeFunction, int flags) {
    String methodBaseName = getGetterSetterBaseName(fieldName);
    return create()
        .withElementNameFrom(apiDefinition, api -> PropertyBean.getterPrefixFor(dataTypeFunction.apply(api)) + methodBaseName)
        .withFlags(flags)
        .withReturnTypeFrom(apiDefinition, dataTypeFunction)
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withBody(b -> b.returnClause().append(fieldName).semicolon());
  }

  /**
   * Creates an {@link IMethodGenerator} that creates a setter for the specified {@link IFieldGenerator}.
   *
   * @param fieldGenerator
   *          The {@link IFieldGenerator} for which the setter should be created. Must not be {@code null}.
   * @return The new {@link IMethodGenerator}.
   */
  @SuppressWarnings("unchecked")
  public static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createSetter(IFieldGenerator<?> fieldGenerator) {
    ApiFunction<IApiSpecification, String> dataType = (ApiFunction<IApiSpecification, String>) fieldGenerator.dataType()
        .orElseThrow(() -> newFail("Cannot create setter for field because it has no data type."));
    return createSetter(Ensure.notNull(fieldGenerator).elementName().orElseThrow(() -> newFail("Cannot create setter for field because it has no name.")),
        dataType.apiClass().orElse(null), dataType.apiFunction(), Flags.AccPublic, null);
  }

  /**
   * Creates an {@link IMethodGenerator} that creates a public setter for a field with specified name and type.
   *
   * @param fieldName
   *          The name of the field the setter should change. Must not be blank.
   * @param dataType
   *          The data type of the specified field.
   * @return The new {@link IMethodGenerator}
   */
  public static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createSetter(String fieldName, String dataType) {
    return createSetter(fieldName, dataType, Flags.AccPublic);
  }

  /**
   * Creates an {@link IMethodGenerator} that creates a setter for a field with specified name and type.
   *
   * @param fieldName
   *          The name of the field the setter should change. Must not be blank.
   * @param dataType
   *          The data type of the specified field.
   * @param flags
   *          The flags of the setter. see {@link Flags}.
   * @return The new {@link IMethodGenerator}
   */
  public static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createSetter(String fieldName, String dataType, int flags) {
    return createSetter(fieldName, dataType, flags, null);
  }

  /**
   * Creates an {@link IMethodGenerator} that creates a setter for a field with specified name and type.
   *
   * @param fieldName
   *          The name of the field the setter should change. Must not be blank.
   * @param dataType
   *          The data type of the specified field.
   * @param flags
   *          The flags of the setter. see {@link Flags}.
   * @param paramNamePrefix
   *          An optional prefix that will be appended to the parameter name of the setter. May be {@code null}.
   * @return The new {@link IMethodGenerator}
   */
  public static IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createSetter(String fieldName, String dataType, int flags, String paramNamePrefix) {
    return createSetter(fieldName, null, api -> dataType, flags, paramNamePrefix);
  }

  public static <A extends IApiSpecification> IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createSetter(String fieldName,
      Class<A> apiDefinition, Function<A, String> dataTypeFunction, int flags, String paramNamePrefix) {
    String setterBaseName = getGetterSetterBaseName(fieldName); // starts with an uppercase char
    String parameterName = getPrefixedMethodParameterName(paramNamePrefix, setterBaseName);
    return create()
        .withElementName(PropertyBean.SETTER_PREFIX + setterBaseName)
        .withFlags(flags)
        .withReturnType(JavaTypes._void)
        .withParameter(
            MethodParameterGenerator.create()
                .withDataTypeFrom(apiDefinition, dataTypeFunction)
                .withElementName(parameterName))
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withBody(b -> b.append(fieldName).equalSign().append(parameterName).semicolon());
  }

  protected static boolean canHaveBody(int methodFlags) {
    return (!isInterface(methodFlags) || isDefaultMethod(methodFlags)) && !isAbstract(methodFlags);
  }

  protected static String getGetterSetterBaseName(String fieldName) {
    StringBuilder sb = new StringBuilder(Ensure.notBlank(fieldName));
    if (sb.length() > 1 && sb.charAt(0) == 'm' && sb.charAt(1) == '_') {
      sb.delete(0, 2);
    }
    if (sb.length() > 0) {
      // ensure start with upper case
      sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
    }
    return sb.toString();
  }

  protected static String getPrefixedMethodParameterName(String paramNamePrefix, String setterBaseName) {
    if (Strings.isBlank(paramNamePrefix)) {
      return Introspector.decapitalize(setterBaseName);
    }

    StringBuilder paramNameBuilder = new StringBuilder(paramNamePrefix.length() + setterBaseName.length());
    paramNameBuilder.append(Introspector.decapitalize(paramNamePrefix));
    paramNameBuilder.append(setterBaseName);
    return paramNameBuilder.toString();
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    super.build(builder);
    buildMethodSource(MemberBuilder.create(builder));
  }

  protected void buildMethodSource(IMemberBuilder<?> builder) {
    int flags = flags();
    boolean isVarargs = isVarargs(flags);
    flags &= ~Flags.AccVarargs; // remove varargs flag because it has the same value as transient which would be wrong in Flags.toString()
    if (isDefaultMethod(flags) || isInterface(flags)) {
      flags &= ~(Flags.AccPublic | Flags.AccAbstract); // for interface methods: remove implicit public and abstract flag
    }
    builder.appendFlags(flags);

    // type parameters
    builder.append(typeParameters(), "<", ", ", "> ");

    // return type
    returnType().ifPresent(t -> builder.refFrom(t).space());

    // method name
    builder.append(ensureValidJavaName(elementName(builder.context()).orElseThrow(() -> newFail("Method must have a name."))));

    // parameters
    if (isVarargs && !m_parameters.isEmpty()) {
      // if method is varargs the last parameter must be the varargs
      m_parameters.get(m_parameters.size() - 1).asVarargs();
    }
    builder.parenthesisOpen();
    builder.append(parameters(), null, ", ", null);
    builder.parenthesisClose();

    // exceptions
    builder.appendFrom(exceptions(), " throws ", ", ", null);

    if (canHaveBody(flags)) {
      buildBodySource(builder);
    }
    else {
      builder.semicolon();
    }
  }

  protected BODY createMethodBodyBuilder(ISourceBuilder<?> inner) {
    return createMethodBodyBuilder(inner, this);
  }

  @SuppressWarnings("unchecked")
  protected BODY createMethodBodyBuilder(ISourceBuilder<?> inner, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> surroundingMethod) {
    return (BODY) MethodBodyBuilder.create(inner, surroundingMethod);
  }

  protected void buildBodySource(IMemberBuilder<?> builder) {
    builder.space().blockStart();

    String lineDelimiter = builder.context().lineDelimiter();
    StringBuilder bodySrc = body()
        .map(g -> g.toSource(this::createMethodBodyBuilder, builder.context()))
        .orElse(new StringBuilder(lineDelimiter));

    boolean srcAvailable = bodySrc.length() > lineDelimiter.length();
    if (srcAvailable && !bodySrc.substring(0, lineDelimiter.length()).equals(lineDelimiter)) {
      // ensure the body starts on a new line
      builder.nl();
    }
    builder.append(bodySrc);

    boolean endsWithNl = srcAvailable && bodySrc.substring(bodySrc.length() - lineDelimiter.length(), bodySrc.length()).equals(lineDelimiter);
    if (!endsWithNl) {
      int lastNl = bodySrc.lastIndexOf(lineDelimiter);
      if (lastNl < 0 || Strings.hasText(bodySrc.substring(lastNl))) {
        // ensure the body ends with a line delimiter
        builder.append(lineDelimiter);
      }
    }
    builder.blockEnd();
  }

  @Override
  protected IJavaElementCommentBuilder<?> createCommentBuilder(ISourceBuilder<?> builder) {
    //noinspection TypeMayBeWeakened
    IJavaSourceBuilder<?> javaSourceBuilder = (IJavaSourceBuilder<?>) builder;
    IJavaEnvironment context = javaSourceBuilder.context().environment().orElse(null);
    if (PropertyBean.getterName(this, context).isPresent()) {
      return JavaElementCommentBuilder.createForMethodGetter(builder, this);
    }
    if (PropertyBean.setterName(this, context).isPresent()) {
      return JavaElementCommentBuilder.createForMethodSetter(builder, this);
    }
    return JavaElementCommentBuilder.createForMethod(builder, this);
  }

  @Override
  public String identifier(IJavaEnvironment context, boolean useErasureOnly) {
    List<String> methodParamTypes = m_parameters.stream()
        .map(IMethodParameterGenerator::dataType)
        .map(d -> d.orElseThrow(() -> newFail("Cannot calculate the method identifier because the datatype is missing.")))
        .map(af -> af.apply(context).orElseThrow(() -> newFail("Cannot compute parameter data type of method '{}'.", elementName().orElse(null))))
        .map(d -> typeErasureIfNecessary(d, useErasureOnly))
        .collect(toList());

    return JavaTypes.createMethodIdentifier(elementName().orElseThrow(() -> newFail("Cannot calculate method identifier because the method name is missing.")),
        methodParamTypes);
  }

  protected static String typeErasureIfNecessary(String name, boolean useErasureOnly) {
    if (useErasureOnly) {
      return JavaTypes.erasure(name);
    }
    return name;
  }

  @Override
  public String identifier(IJavaEnvironment context) {
    return identifier(context, false);
  }

  @Override
  public Optional<String> elementName() {
    return elementName((IJavaEnvironment) null);
  }

  @Override
  public Optional<String> elementName(IJavaBuilderContext context) {
    return elementNameFunction().flatMap(func -> func.apply(context));
  }

  @Override
  public Optional<String> elementName(IJavaEnvironment context) {
    return elementNameFunction().flatMap(func -> func.apply(context));
  }

  public Optional<ApiFunction<?, String>> elementNameFunction() {
    return Optional.ofNullable(m_methodName);
  }

  @Override
  public TYPE withElementName(String newName) {
    return withElementNameFrom(null, api -> newName);
  }

  @Override
  public <A extends IApiSpecification> TYPE withElementNameFrom(Class<A> apiDefinition, Function<A, String> nameSupplier) {
    if (nameSupplier == null) {
      m_methodName = null;
    }
    else {
      m_methodName = new ApiFunction<>(apiDefinition, nameSupplier);
    }
    return thisInstance();
  }

  @Override
  public Optional<ApiFunction<?, String>> returnType() {
    return Optional.ofNullable(m_returnType);
  }

  @Override
  public TYPE withReturnType(String returnType) {
    return withReturnTypeFrom(null, api -> returnType);
  }

  @Override
  public <A extends IApiSpecification> TYPE withReturnTypeFrom(Class<A> apiDefinition, Function<A, String> returnTypeSupplier) {
    if (returnTypeSupplier == null) {
      m_returnType = null;
    }
    else {
      m_returnType = new ApiFunction<>(apiDefinition, returnTypeSupplier);
    }
    return thisInstance();
  }

  @Override
  public Stream<ApiFunction<?, String>> exceptions() {
    return m_exceptions.stream();
  }

  @Override
  public TYPE withException(String exceptionReference) {
    return withExceptionFrom(null, api -> exceptionReference);
  }

  @Override
  public <A extends IApiSpecification> TYPE withExceptionFrom(Class<A> apiDefinition, Function<A, String> exceptionSupplier) {
    m_exceptions.add(new ApiFunction<>(apiDefinition, exceptionSupplier));
    return thisInstance();
  }

  @Override
  public TYPE withoutException(Predicate<ApiFunction<?, String>> toRemove) {
    m_exceptions.removeIf(toRemove);
    return thisInstance();
  }

  @Override
  public Optional<ISourceGenerator<BODY>> body() {
    return Optional.ofNullable(m_body);
  }

  @Override
  public TYPE withBody(ISourceGenerator<BODY> body) {
    m_body = body;
    return thisInstance();
  }

  @Override
  public Stream<IMethodParameterGenerator<?>> parameters() {
    return m_parameters.stream();
  }

  @Override
  public TYPE withParameter(IMethodParameterGenerator<?> parameter) {
    if (parameter != null) {
      m_parameters.add(parameter);
    }
    return thisInstance();
  }

  @Override
  public TYPE withoutParameter(String parameterName) {
    Ensure.notBlank(parameterName);
    for (Iterator<IMethodParameterGenerator<?>> it = m_parameters.iterator(); it.hasNext();) {
      IMethodParameterGenerator<?> parameter = it.next();
      if (parameterName.equals(parameter.elementName().orElse(null))) {
        it.remove();
        return thisInstance();
      }
    }
    return thisInstance();
  }

  @Override
  public TYPE withTypeParameter(ITypeParameterGenerator<?> typeParameter) {
    if (typeParameter != null) {
      m_typeParameters.add(typeParameter);
    }
    return thisInstance();
  }

  @Override
  public Stream<ITypeParameterGenerator<?>> typeParameters() {
    return m_typeParameters.stream();
  }

  @Override
  public TYPE withoutTypeParameter(String elementName) {
    Ensure.notNull(elementName);
    m_typeParameters.removeIf(generator -> elementName.equals(generator.elementName().orElse(null)));
    return thisInstance();
  }

  @Override
  public TYPE asAbstract() {
    return withFlags(Flags.AccAbstract);
  }

  @Override
  public TYPE asSynchronized() {
    return withFlags(Flags.AccSynchronized);
  }
}
