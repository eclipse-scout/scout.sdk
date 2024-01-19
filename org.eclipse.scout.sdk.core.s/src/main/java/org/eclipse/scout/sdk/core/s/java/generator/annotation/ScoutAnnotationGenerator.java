/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.generator.annotation;

import static org.eclipse.scout.sdk.core.generator.ISourceGenerator.raw;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.java.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.java.annotation.FormDataAnnotation.DefaultSubtypeSdkCommand;
import org.eclipse.scout.sdk.core.s.java.annotation.FormDataAnnotation.SdkCommand;
import org.eclipse.scout.sdk.core.s.java.annotation.OrderAnnotation;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link ScoutAnnotationGenerator}</h3>
 *
 * @since 6.1.0
 */
public class ScoutAnnotationGenerator<TYPE extends ScoutAnnotationGenerator<TYPE>> extends AnnotationGenerator<TYPE> {

  public static final String DO_CONVENIENCE_METHODS_GENERATED_COMMENT = "DoConvenienceMethodsGenerator";

  public static IAnnotationGenerator<?> createOrder(double orderNr) {
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::Order)
        .withElementFrom(IScoutApi.class, api -> api.Order().valueElementName(), raw(OrderAnnotation.convertToJavaSource(orderNr)));
  }

  public static IAnnotationGenerator<?> createExtends(CharSequence extendedType) {
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::Extends)
        .withElementFrom(IScoutApi.class, api -> api.Extends().valueElementName(), b -> b.classLiteral(extendedType));
  }

  /**
   * Creates a new ClassId annotation source builder
   *
   * @param classIdValue
   *          the class id value to use
   * @return the created source builder
   */
  public static IAnnotationGenerator<?> createClassId(CharSequence classIdValue) {
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::ClassId)
        .withElementFrom(IScoutApi.class, api -> api.ClassId().valueElementName(), b -> b.stringLiteral(classIdValue));
  }

  public static IAnnotationGenerator<?> createBefore() {
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::Before);
  }

  public static IAnnotationGenerator<?> createApplicationScoped() {
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::ApplicationScoped);
  }

  public static IAnnotationGenerator<?> createBeanMock() {
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::BeanMock);
  }

  public static IAnnotationGenerator<?> createDoConvenienceMethodsGenerated() {
    return createGenerated(DO_CONVENIENCE_METHODS_GENERATED_COMMENT, null);
  }

  public static IAnnotationGenerator<?> createAuthentication() {
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::Authentication)
        .withElementFrom(IScoutApi.class, api -> api.Authentication().methodElementName(), b -> createClazz(IScoutApi.class, IScoutApi::BasicAuthenticationMethod).generate(b))
        .withElementFrom(IScoutApi.class, api -> api.Authentication().verifierElementName(), b -> createClazz(IScoutApi.class, IScoutApi::BasicAuthenticationMethod).generate(b));
  }

  public static <API extends IApiSpecification> IAnnotationGenerator<?> createClazz(Class<API> apiClass, Function<API, ITypeNameSupplier> referenceProvider) {
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::Clazz)
        .withElementFrom(IScoutApi.class, api -> api.Clazz().valueElementName(), b -> b.classLiteralFrom(apiClass, referenceProvider));
  }

  public static IAnnotationGenerator<?> createClazz(CharSequence clazzFqn) {
    var fqn = ITypeNameSupplier.of(clazzFqn);
    return createClazz(null, api -> fqn);
  }

  public static IAnnotationGenerator<?> createHandler(CharSequence handlerFqn) {
    var fqn = ITypeNameSupplier.of(handlerFqn);
    return createHandler(null, api -> fqn);
  }

  public static <API extends IApiSpecification> IAnnotationGenerator<?> createHandler(Class<API> apiClass, Function<API, ITypeNameSupplier> referenceProvider) {
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::Handler)
        .withElementFrom(IScoutApi.class, api -> api.Handler().valueElementName(), b -> createClazz(apiClass, referenceProvider).generate(b));
  }

  public static IAnnotationGenerator<?> createTunnelToServer() {
    return create().withAnnotationNameFrom(IScoutApi.class, IScoutApi::TunnelToServer);
  }

  public static IAnnotationGenerator<?> createTest() {
    return create().withAnnotationNameFrom(IScoutApi.class, IScoutApi::Test);
  }

  /**
   * Creates a new {@link IAnnotationGenerator} creating an @RunWithSubject annotation.
   *
   * @param valueBuilder
   *          The {@link ISourceBuilder} creating the value part of the annotation. Can be {@code null} in which an
   *          anonymous subject is used.
   * @return The created {@link IAnnotationGenerator}.
   */
  public static IAnnotationGenerator<?> createRunWithSubject(ISourceGenerator<IExpressionBuilder<?>> valueBuilder) {
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::RunWithSubject)
        .withElementFrom(IScoutApi.class, api -> api.RunWithSubject().valueElementName(),
            Optional.ofNullable(valueBuilder).orElseGet(() -> b -> b.stringLiteral("anonymous")));
  }

  /**
   * Creates a new {@link IAnnotationGenerator} creating a {@code @RunWithClientSession} annotation using the given
   * client session.
   *
   * @param clientSession
   *          The client session fully qualified name to use or {@code null} if the default client session should be
   *          used.
   * @return The created {@link IAnnotationGenerator}.
   */
  public static IAnnotationGenerator<?> createRunWithClientSession(CharSequence clientSession) {
    var session = ITypeNameSupplier.of(clientSession);
    return createRunWithClientSessionFrom(null, api -> session);
  }

  public static <API extends IApiSpecification> IAnnotationGenerator<?> createRunWithClientSession(ApiFunction<API, ITypeNameSupplier> clientSessionProvider) {
    if (clientSessionProvider == null) {
      return createRunWithClientSessionFrom(null, null);
    }
    return createRunWithClientSessionFrom(clientSessionProvider.apiClass().orElse(null), clientSessionProvider.apiFunction());
  }

  public static <API extends IApiSpecification> IAnnotationGenerator<?> createRunWithClientSessionFrom(Class<API> apiSpec, Function<API, ITypeNameSupplier> clientSessionProvider) {
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::RunWithClientSession)
        .withElementFrom(IScoutApi.class, api -> api.RunWithClientSession().valueElementName(), b -> b.classLiteral(
            Optional.ofNullable(clientSessionProvider)
                .map(p -> new ApiFunction<>(apiSpec, p))
                .map(a -> a.apply(b.context()))
                .map(ITypeNameSupplier::fqn)
                .filter(Strings::hasText)
                .orElseGet(() -> b.context().requireApi(IScoutApi.class).TestEnvironmentClientSession().fqn()) // default
        ));
  }

  public static IAnnotationGenerator<?> createRunWithServerSession(CharSequence serverSession) {
    var session = ITypeNameSupplier.of(serverSession);
    return createRunWithServerSession(null, api -> session);
  }

  public static <API extends IApiSpecification> IAnnotationGenerator<?> createRunWithServerSession(ApiFunction<API, ITypeNameSupplier> serverSessionProvider) {
    Ensure.notNull(serverSessionProvider);
    return createRunWithServerSession(serverSessionProvider.apiClass().orElse(null), serverSessionProvider.apiFunction());
  }

  public static <API extends IApiSpecification> IAnnotationGenerator<?> createRunWithServerSession(Class<API> apiSpec, Function<API, ITypeNameSupplier> serverSessionProvider) {
    Ensure.notNull(serverSessionProvider);
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::RunWithServerSession)
        .withElementFrom(IScoutApi.class, api -> api.RunWithServerSession().valueElementName(), b -> b.classLiteralFrom(apiSpec, serverSessionProvider));
  }

  public static IAnnotationGenerator<?> createRunWith(CharSequence runner) {
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::RunWith)
        .withElementFrom(IScoutApi.class, api -> api.RunWith().valueElementName(), b -> b.classLiteral(Ensure.notBlank(runner)));
  }

  public static IAnnotationGenerator<?> createData(CharSequence pageDataType) {
    return create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::Data)
        .withElementFrom(IScoutApi.class, api -> api.Data().valueElementName(), b -> b.classLiteral(Ensure.notBlank(pageDataType)));
  }

  public static IAnnotationGenerator<?> createFormData() {
    return createFormData(null, null, null);
  }

  public static IAnnotationGenerator<?> createFormData(CharSequence formDataClass, SdkCommand sdkCommand, DefaultSubtypeSdkCommand defaultSubtypeCommand) {
    var generator = create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::FormData);
    if (Strings.hasText(formDataClass)) {
      generator.withElementFrom(IScoutApi.class, api -> api.FormData().valueElementName(), b -> b.classLiteral(formDataClass));
    }
    if (sdkCommand != null && SdkCommand.DEFAULT != sdkCommand) {
      generator.withElementFrom(IScoutApi.class, api -> api.FormData().sdkCommandElementName(), b -> b.refFrom(IScoutApi.class, api -> api.FormData().fqn()).append(".SdkCommand.").append(sdkCommand.name()));
    }
    if (defaultSubtypeCommand != null && DefaultSubtypeSdkCommand.DEFAULT != defaultSubtypeCommand) {
      generator.withElementFrom(IScoutApi.class, api -> api.FormData().defaultSubtypeSdkCommandElementName(),
          b -> b.ref(generator.elementName().orElseThrow()).append(".DefaultSubtypeSdkCommand.").append(defaultSubtypeCommand.name()));
    }
    return generator;
  }

  public static IAnnotationGenerator<?> createReplace() {
    return create().withAnnotationNameFrom(IScoutApi.class, IScoutApi::Replace);
  }

  /**
   * @param typeThatGeneratedTheCode
   *          The name of the class the generated (derived) element is based on. Must not be blank.
   * @return A new {@code Generated} {@link IAnnotationGenerator} with the specified value and a default comment.
   */
  public static IAnnotationGenerator<?> createGenerated(CharSequence typeThatGeneratedTheCode) {
    return createGenerated(typeThatGeneratedTheCode, "This class is auto generated. No manual modifications recommended.");
  }

  /**
   * @param typeThatGeneratedTheCode
   *          The name of the class the generated (derived) element is based on. Must not be blank.
   * @param comments
   *          The comment value of the {@code Generated} annotation. May be {@code null}.
   * @return A new {@code Generated} {@link IAnnotationGenerator} with the specified value and comment.
   */
  public static IAnnotationGenerator<?> createGenerated(CharSequence typeThatGeneratedTheCode, CharSequence comments) {
    var generator = create()
        .withAnnotationNameFrom(IScoutApi.class, IScoutApi::Generated)
        .withElementFrom(IScoutApi.class, api -> api.Generated().valueElementName(), b -> b.stringLiteral(Ensure.notBlank(typeThatGeneratedTheCode)));
    Strings.notBlank(comments).ifPresent(c -> generator.withElementFrom(IScoutApi.class, api -> api.Generated().commentsElementName(), b -> b.stringLiteral(c)));
    return generator;
  }
}
