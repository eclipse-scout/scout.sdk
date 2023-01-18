/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator.method;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.java.model.api.Flags.isAbstract;
import static org.eclipse.scout.sdk.core.java.model.api.Flags.isDefaultMethod;
import static org.eclipse.scout.sdk.core.java.model.api.Flags.isInterface;
import static org.eclipse.scout.sdk.core.java.model.api.Flags.isVarargs;
import static org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer.transformMethodParameter;
import static org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer.transformTypeParameter;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContextFunction;
import org.eclipse.scout.sdk.core.java.builder.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.java.builder.body.MethodBodyBuilder;
import org.eclipse.scout.sdk.core.java.builder.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.java.builder.comment.JavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.java.builder.member.IMemberBuilder;
import org.eclipse.scout.sdk.core.java.builder.member.MemberBuilder;
import org.eclipse.scout.sdk.core.java.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.java.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.java.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.java.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.member.AbstractMemberGenerator;
import org.eclipse.scout.sdk.core.java.generator.methodparam.IMethodParameterGenerator;
import org.eclipse.scout.sdk.core.java.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.java.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.java.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.java.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link MethodGenerator}</h3>
 *
 * @since 6.1.0
 */
public class MethodGenerator<TYPE extends IMethodGenerator<TYPE, BODY>, BODY extends IMethodBodyBuilder<?>> extends AbstractMemberGenerator<TYPE> implements IMethodGenerator<TYPE, BODY> {

  protected final List<IMethodParameterGenerator<?>> m_parameters;
  protected final List<ITypeParameterGenerator<?>> m_typeParameters;
  protected final List<JavaBuilderContextFunction<ITypeNameSupplier>> m_throwables;

  private JavaBuilderContextFunction<String> m_returnType;
  private ISourceGenerator<BODY> m_body;
  private boolean m_withOverrideIfNecessary;
  private IType m_overrideIfNecessaryDeclaringType;

  protected MethodGenerator() {
    m_parameters = new ArrayList<>();
    m_typeParameters = new ArrayList<>();
    m_throwables = new ArrayList<>();
  }

  protected MethodGenerator(IMethod method, IWorkingCopyTransformer transformer) {
    super(method, transformer);

    m_returnType = method.returnType()
        .map(IType::reference)
        .map(JavaBuilderContextFunction::create)
        .orElse(null);

    m_typeParameters = method.typeParameters()
        .map(p -> transformTypeParameter(p, transformer))
        .flatMap(Optional::stream)
        .collect(toList());

    m_parameters = method.parameters().stream()
        .map(mp -> transformMethodParameter(mp, transformer))
        .flatMap(Optional::stream)
        .collect(toList());

    m_throwables = method.exceptionTypes()
        .map(IType::name)
        .map(ITypeNameSupplier::of)
        .map(JavaBuilderContextFunction::create)
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
   * @return A new empty {@link IMethodGenerator}.
   */
  public static IMethodGenerator<?, ?> create() {
    return new MethodGenerator<>();
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
  public static IMethodGenerator<?, ?> create(IMethod method, IWorkingCopyTransformer transformer) {
    return new MethodGenerator<>(method, transformer);
  }

  /**
   * Creates an {@link IMethodGenerator} that creates a getter for the specified {@link IFieldGenerator}.
   *
   * @param fieldGenerator
   *          The {@link IFieldGenerator} for which the getter should be created. Must not be {@code null}.
   * @return The new {@link IMethodGenerator}.
   */
  public static IMethodGenerator<?, ?> createGetter(IFieldGenerator<?> fieldGenerator) {
    var dataType = fieldGenerator.dataTypeFunc().orElseThrow(() -> newFail("Cannot create getter for field because it has no data type."));
    var fieldName = Ensure.notNull(fieldGenerator).elementName().orElseThrow(() -> newFail("Cannot create getter for field because it has no name."));
    return createGetterFunc(fieldName, dataType, Flags.AccPublic);
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
  public static IMethodGenerator<?, ?> createGetter(String fieldName, String dataType) {
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
  public static IMethodGenerator<?, ?> createGetter(String fieldName, String dataType, int flags) {
    return createGetterFunc(fieldName, JavaBuilderContextFunction.create(dataType), flags);
  }

  /**
   * Creates an {@link IMethodGenerator} that creates a getter for a field with given name and data type.
   * 
   * @param fieldName
   *          The name of the field. Must not be blank or {@code null}.
   * @param apiDefinition
   *          An optional api class from which the data type should be obtained. It may be {@code null} in case the
   *          given dataTypeFunc can handle a {@code null} input.
   * @param dataTypeFunction
   *          A function returning the data type of the field. Must not be {@code null}.
   * @param flags
   *          The flags of the getter. see {@link Flags}.
   * @return A new {@link IMethodGenerator} that creates a getter for the specified field.
   */
  public static <A extends IApiSpecification> IMethodGenerator<?, ?> createGetterFrom(String fieldName, Class<A> apiDefinition, Function<A, String> dataTypeFunction, int flags) {
    return createGetterFunc(fieldName, new ApiFunction<>(apiDefinition, dataTypeFunction), flags);
  }

  /**
   * Creates an {@link IMethodGenerator} that creates a getter for a field with given name and data type.
   * 
   * @param fieldName
   *          The name of the field. Must not be blank or {@code null}.
   * @param dataTypeFunction
   *          A function returning the data type of the field. Must not be {@code null}.
   * @param flags
   *          The flags of the getter. see {@link Flags}.
   * @return A new {@link IMethodGenerator} that creates a getter for the specified field.
   */
  public static IMethodGenerator<?, ?> createGetterFunc(String fieldName, Function<IJavaBuilderContext, String> dataTypeFunction, int flags) {
    var methodBaseName = getGetterSetterBaseName(fieldName);
    var getter = create()
        .withFlags(flags)
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withBody(b -> b.returnClause().append(fieldName).semicolon());
    var constDataType = JavaBuilderContextFunction.orNull(dataTypeFunction).apply().orElse(null);
    if (constDataType != null) {
      return getter
          .withElementName(PropertyBean.getterPrefixFor(constDataType) + methodBaseName)
          .withReturnType(constDataType);
    }
    return getter
        .withElementNameFunc(c -> PropertyBean.getterPrefixFor(dataTypeFunction.apply(c)) + methodBaseName)
        .withReturnTypeFunc(dataTypeFunction);
  }

  /**
   * Creates an {@link IMethodGenerator} that creates a setter for the specified {@link IFieldGenerator}.
   *
   * @param fieldGenerator
   *          The {@link IFieldGenerator} for which the setter should be created. Must not be {@code null}.
   * @return The new {@link IMethodGenerator}.
   */
  public static IMethodGenerator<?, ?> createSetter(IFieldGenerator<?> fieldGenerator) {
    var dataType = fieldGenerator.dataTypeFunc().orElseThrow(() -> newFail("Cannot create setter for field because it has no data type."));
    return createSetter(Ensure.notNull(fieldGenerator).elementName().orElseThrow(() -> newFail("Cannot create setter for field because it has no name.")),
        dataType, Flags.AccPublic, null);
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
  public static IMethodGenerator<?, ?> createSetter(String fieldName, String dataType) {
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
  public static IMethodGenerator<?, ?> createSetter(String fieldName, String dataType, int flags) {
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
  public static IMethodGenerator<?, ?> createSetter(String fieldName, String dataType, int flags, String paramNamePrefix) {
    return createSetter(fieldName, c -> dataType, flags, paramNamePrefix);
  }

  /**
   * Creates an {@link IMethodGenerator} that creates a setter for a field with specified name and type.
   * 
   * @param fieldName
   *          The name of the field the setter should change. Must not be blank or {@code null}.
   * @param apiDefinition
   *          An optional api class from which the data type should be obtained. It may be {@code null} in case the
   *          given dataTypeFunc can handle a {@code null} input.
   * @param dataTypeFunction
   *          A function returning the data type of the field. Must not be {@code null}.
   * @param flags
   *          The flags of the setter. see {@link Flags}.
   * @param paramNamePrefix
   *          An optional prefix that will be appended to the parameter name of the setter. May be {@code null}.
   * @return The new {@link IMethodGenerator}
   */
  public static <A extends IApiSpecification> IMethodGenerator<?, ?> createSetter(String fieldName, Class<A> apiDefinition, Function<A, String> dataTypeFunction, int flags, String paramNamePrefix) {
    return createSetter(fieldName, new ApiFunction<>(apiDefinition, dataTypeFunction), flags, paramNamePrefix);
  }

  /**
   * Creates an {@link IMethodGenerator} that creates a setter for a field with specified name and type.
   * 
   * @param fieldName
   *          The name of the field the setter should change. Must not be blank or {@code null}.
   * @param dataTypeFunction
   *          A function returning the data type of the field. Must not be {@code null}.
   * @param flags
   *          The flags of the setter. see {@link Flags}.
   * @param paramNamePrefix
   *          An optional prefix that will be appended to the parameter name of the setter. May be {@code null}.
   * @return The new {@link IMethodGenerator}
   */
  public static IMethodGenerator<?, ?> createSetter(String fieldName, Function<IJavaBuilderContext, String> dataTypeFunction, int flags, String paramNamePrefix) {
    var setterBaseName = getGetterSetterBaseName(fieldName); // starts with an uppercase char
    var parameterName = getPrefixedMethodParameterName(paramNamePrefix, setterBaseName);
    return create()
        .withElementName(PropertyBean.SETTER_PREFIX + setterBaseName)
        .withFlags(flags)
        .withReturnType(JavaTypes._void)
        .withParameter(
            MethodParameterGenerator.create()
                .withDataTypeFunc(dataTypeFunction)
                .withElementName(parameterName))
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withBody(b -> b.append(fieldName).equalSign().append(parameterName).semicolon());
  }

  protected static boolean canHaveBody(int methodFlags) {
    return (!isInterface(methodFlags) || isDefaultMethod(methodFlags)) && !isAbstract(methodFlags);
  }

  protected static String getGetterSetterBaseName(String fieldName) {
    var sb = new StringBuilder(Ensure.notBlank(fieldName));
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

    var paramNameBuilder = new StringBuilder(paramNamePrefix.length() + setterBaseName.length());
    paramNameBuilder.append(Introspector.decapitalize(paramNamePrefix));
    paramNameBuilder.append(setterBaseName);
    return paramNameBuilder.toString();
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    var overrideAnnotation = createOverrideAnnotationIfNecessary(builder.context());
    withAnnotation(overrideAnnotation); // temporary append
    try {
      super.build(builder);
      buildMethodSource(MemberBuilder.create(builder));
    }
    finally {
      if (overrideAnnotation != null) {
        withoutAnnotation(a -> a == overrideAnnotation); // remove again in case generator is executed again using a different Java environment
      }
    }
  }

  protected IAnnotationGenerator<?> createOverrideAnnotationIfNecessary(IJavaBuilderContext context) {
    if (!isWithOverrideIfNecessary()) {
      return null;
    }
    var overrideFqn = Override.class.getName();
    if (annotations().anyMatch(a -> overrideFqn.equals(a.elementName(context).orElse(null)))) {
      return null; // already exists
    }
    if (!existsMethodInSuperHierarchy(context)) {
      return null;
    }
    return AnnotationGenerator.createOverride();
  }

  protected boolean existsMethodInSuperHierarchy(IJavaBuilderContext context) {
    var methodId = identifier(context);
    var explicitDeclaringType = getOverrideIfNecessaryDeclaringType();
    if (explicitDeclaringType != null) {
      // explicit declaring type has been given.
      return explicitDeclaringType.directSuperTypes()
          .anyMatch(t -> existsMethodInSuperHierarchy(t, methodId));
    }

    //noinspection RedundantExplicitVariableType
    IJavaElementGenerator<?> declaringGenerator = declaringGenerator().orElse(null);
    if (declaringGenerator instanceof ITypeGenerator<?> declaringTypeGenerator) {
      // use super types from declaring (surrounding) type generator
      return existsMethodInSuperHierarchy(declaringTypeGenerator.getHierarchyType(context), methodId);
    }
    return false;
  }

  protected static boolean existsMethodInSuperHierarchy(IType t, String methodId) {
    return t.methods()
        .withSuperTypes(true)
        .withMethodIdentifier(methodId)
        .existsAny();
  }

  protected void buildMethodSource(IMemberBuilder<?> builder) {
    var flags = flags();
    var isVarargs = isVarargs(flags);
    flags &= ~Flags.AccVarargs; // remove varargs flag because it has the same value as transient which would be wrong in Flags.toString()
    if (isDefaultMethod(flags) || isInterface(flags)) {
      flags &= ~(Flags.AccPublic | Flags.AccAbstract); // for interface methods: remove implicit public and abstract flag
    }
    builder.appendFlags(flags);

    // type parameters
    builder.append(typeParameters(), "<", ", ", "> ");

    // return type
    returnTypeFunc().ifPresent(t -> builder.refFunc(t).space());

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

    // throwables
    builder.references(throwablesFunc()
        .map(af -> af.apply(builder.context()))
        .filter(Objects::nonNull)
        .map(ITypeNameSupplier::fqn)
        .filter(Strings::hasText)
        .distinct(), " throws ", ", ", null);

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
  protected BODY createMethodBodyBuilder(ISourceBuilder<?> inner, IMethodGenerator<?, ?> surroundingMethod) {
    return (BODY) MethodBodyBuilder.create(inner, surroundingMethod);
  }

  protected void buildBodySource(IMemberBuilder<?> builder) {
    builder.space().blockStart();

    var lineDelimiter = builder.context().lineDelimiter();
    var bodySrc = body()
        .map(g -> g.toSource(this::createMethodBodyBuilder, builder.context()))
        .orElse(new StringBuilder(lineDelimiter));

    var srcAvailable = bodySrc.length() > lineDelimiter.length();
    if (srcAvailable && !bodySrc.substring(0, lineDelimiter.length()).equals(lineDelimiter)) {
      // ensure the body starts on a new line
      builder.nl();
    }
    builder.append(bodySrc);

    var endsWithNl = srcAvailable && bodySrc.substring(bodySrc.length() - lineDelimiter.length(), bodySrc.length()).equals(lineDelimiter);
    if (!endsWithNl) {
      var lastNl = bodySrc.lastIndexOf(lineDelimiter);
      if (lastNl < 0 || Strings.hasText(bodySrc.substring(lastNl))) {
        // ensure the body ends with a line delimiter
        builder.append(lineDelimiter);
      }
    }
    builder.blockEnd();
  }

  @Override
  protected IJavaElementCommentBuilder<?> createCommentBuilder(ISourceBuilder<?> builder) {
    var javaSourceBuilder = (IJavaSourceBuilder<?>) builder;
    var context = javaSourceBuilder.context();
    if (PropertyBean.getterName(this, context).isPresent()) {
      return JavaElementCommentBuilder.createForMethodGetter(builder, this);
    }
    if (PropertyBean.setterName(this, context).isPresent()) {
      return JavaElementCommentBuilder.createForMethodSetter(builder, this);
    }
    return JavaElementCommentBuilder.createForMethod(builder, this);
  }

  @Override
  public String identifier(IJavaBuilderContext context, boolean includeTypeArguments) {
    var methodParamTypes = m_parameters.stream()
        .map(param -> param.reference(context, !includeTypeArguments))
        .collect(toList());
    return JavaTypes.createMethodIdentifier(elementName(context).orElseThrow(() -> newFail("Cannot calculate method identifier because the method name is missing.")),
        methodParamTypes);
  }

  @Override
  public String identifier(IJavaBuilderContext context) {
    return identifier(context, false);
  }

  @Override
  public boolean isConstructor() {
    return returnTypeFunc().isEmpty();
  }

  @Override
  public Optional<String> returnType() {
    return returnTypeFunc().flatMap(JavaBuilderContextFunction::apply);
  }

  @Override
  public Optional<String> returnType(IJavaBuilderContext context) {
    return returnTypeFunc()
        .map(f -> f.apply(context));
  }

  @Override
  public Optional<JavaBuilderContextFunction<String>> returnTypeFunc() {
    return Optional.ofNullable(m_returnType);
  }

  @Override
  public TYPE withReturnType(String returnType) {
    m_returnType = JavaBuilderContextFunction.orNull(returnType);
    return thisInstance();
  }

  @Override
  public <A extends IApiSpecification> TYPE withReturnTypeFrom(Class<A> apiDefinition, Function<A, String> returnTypeSupplier) {
    m_returnType = new ApiFunction<>(apiDefinition, returnTypeSupplier);
    return thisInstance();
  }

  @Override
  public TYPE withReturnTypeFunc(Function<IJavaBuilderContext, String> returnTypeSupplier) {
    m_returnType = JavaBuilderContextFunction.orNull(returnTypeSupplier);
    return thisInstance();
  }

  @Override
  public Stream<String> throwables() {
    return throwablesFunc()
        .map(JavaBuilderContextFunction::apply)
        .flatMap(Optional::stream)
        .map(ITypeNameSupplier::fqn);
  }

  @Override
  public Stream<JavaBuilderContextFunction<ITypeNameSupplier>> throwablesFunc() {
    return m_throwables.stream();
  }

  @Override
  public TYPE withThrowable(String throwableFqn) {
    if (Strings.hasText(throwableFqn)) {
      m_throwables.add(JavaBuilderContextFunction.create(ITypeNameSupplier.of(throwableFqn)));
    }
    return thisInstance();
  }

  @Override
  public <A extends IApiSpecification> TYPE withThrowableFrom(Class<A> apiDefinition, Function<A, ITypeNameSupplier> throwableSupplier) {
    if (throwableSupplier != null) {
      m_throwables.add(new ApiFunction<>(apiDefinition, throwableSupplier));
    }
    return thisInstance();
  }

  @Override
  public TYPE withThrowableFunc(Function<IJavaBuilderContext, ITypeNameSupplier> throwableSupplier) {
    if (throwableSupplier != null) {
      m_throwables.add(JavaBuilderContextFunction.create(throwableSupplier));
    }
    return thisInstance();
  }

  @Override
  public TYPE withoutThrowable(CharSequence fqn) {
    return withoutThrowable(ITypeNameSupplier.of(fqn));
  }

  @Override
  public TYPE withoutThrowable(ITypeNameSupplier cns) {
    var fqnToRemove = cns == null ? null : cns.fqn();
    return withoutThrowable(f -> f.apply()
        .map(ITypeNameSupplier::fqn)
        .filter(isEqual(fqnToRemove))
        .isPresent());
  }

  @Override
  public TYPE withoutThrowable(Predicate<JavaBuilderContextFunction<ITypeNameSupplier>> toRemoveFilter) {
    if (toRemoveFilter == null) {
      m_throwables.clear();
    }
    else {
      m_throwables.removeIf(toRemoveFilter);
    }
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
    for (var it = m_parameters.iterator(); it.hasNext();) {
      var parameter = it.next();
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
  public TYPE withOverrideIfNecessary() {
    return withOverrideIfNecessary(true, null);
  }

  @Override
  public TYPE withoutOverrideIfNecessary() {
    return withOverrideIfNecessary(false, null);
  }

  @Override
  public TYPE withOverrideIfNecessary(boolean withOverrideIfNecessary, IType declaringType) {
    m_withOverrideIfNecessary = withOverrideIfNecessary;
    if (withOverrideIfNecessary) {
      m_overrideIfNecessaryDeclaringType = declaringType;
    }
    else {
      m_overrideIfNecessaryDeclaringType = null;
    }
    return thisInstance();
  }

  @Override
  public boolean isWithOverrideIfNecessary() {
    return m_withOverrideIfNecessary;
  }

  protected IType getOverrideIfNecessaryDeclaringType() {
    return m_overrideIfNecessaryDeclaringType;
  }

  @Override
  public TYPE asAbstract() {
    return withFlags(Flags.AccAbstract);
  }

  @Override
  public TYPE asSynchronized() {
    return withFlags(Flags.AccSynchronized);
  }

  @Override
  public TYPE asDefaultMethod() {
    return withFlags(Flags.AccDefaultMethod);
  }
}
