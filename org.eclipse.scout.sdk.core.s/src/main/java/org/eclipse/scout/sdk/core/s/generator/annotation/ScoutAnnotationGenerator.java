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
package org.eclipse.scout.sdk.core.s.generator.annotation;

import static org.eclipse.scout.sdk.core.generator.ISourceGenerator.raw;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.DefaultSubtypeSdkCommand;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.SdkCommand;
import org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link ScoutAnnotationGenerator}</h3>
 *
 * @since 6.1.0
 */
public class ScoutAnnotationGenerator<TYPE extends ScoutAnnotationGenerator<TYPE>> extends AnnotationGenerator<TYPE> {

  public static IAnnotationGenerator<?> createOrder(double orderNr) {
    return create()
        .withElementNameFrom(IScoutApi.class, IScoutApi::Order)
        .withElementFrom(IScoutApi.class, api -> api.Order().valueElementName(), raw(OrderAnnotation.convertToJavaSource(orderNr)));
  }

  public static IAnnotationGenerator<?> createExtends(CharSequence extendedType) {
    return create()
        .withElementNameFrom(IScoutApi.class, IScoutApi::Extends)
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
        .withElementNameFrom(IScoutApi.class, IScoutApi::ClassId)
        .withElementFrom(IScoutApi.class, api -> api.ClassId().valueElementName(), b -> b.stringLiteral(classIdValue));
  }

  public static IAnnotationGenerator<?> createBefore() {
    return create()
        .withElementNameFrom(IScoutApi.class, IScoutApi::Before);
  }

  public static IAnnotationGenerator<?> createApplicationScoped() {
    return create()
        .withElementNameFrom(IScoutApi.class, IScoutApi::ApplicationScoped);
  }

  public static IAnnotationGenerator<?> createBeanMock() {
    return create()
        .withElementNameFrom(IScoutApi.class, IScoutApi::BeanMock);
  }

  public static IAnnotationGenerator<?> createAuthentication() {
    return create()
        .withElementNameFrom(IScoutApi.class, IScoutApi::Authentication)
        .withElementFrom(IScoutApi.class, api -> api.Authentication().methodElementName(), b -> createClazz(IScoutApi.class, IScoutApi::BasicAuthenticationMethod).generate(b))
        .withElementFrom(IScoutApi.class, api -> api.Authentication().verifierElementName(), b -> createClazz(IScoutApi.class, IScoutApi::BasicAuthenticationMethod).generate(b));
  }

  public static <API extends IApiSpecification> IAnnotationGenerator<?> createClazz(Class<API> apiClass, Function<API, IClassNameSupplier> referenceProvider) {
    return create()
        .withElementNameFrom(IScoutApi.class, IScoutApi::Clazz)
        .withElementFrom(IScoutApi.class, api -> api.Clazz().valueElementName(), b -> b.classLiteralFrom(apiClass, referenceProvider));
  }

  public static IAnnotationGenerator<?> createClazz(CharSequence clazzFqn) {
    var fqn = IClassNameSupplier.raw(clazzFqn);
    return createClazz(null, api -> fqn);
  }

  public static IAnnotationGenerator<?> createHandler(CharSequence handlerFqn) {
    var fqn = IClassNameSupplier.raw(handlerFqn);
    return createHandler(null, api -> fqn);
  }

  public static <API extends IApiSpecification> IAnnotationGenerator<?> createHandler(Class<API> apiClass, Function<API, IClassNameSupplier> referenceProvider) {
    return create()
        .withElementNameFrom(IScoutApi.class, IScoutApi::Handler)
        .withElementFrom(IScoutApi.class, api -> api.Handler().valueElementName(), b -> createClazz(apiClass, referenceProvider).generate(b));
  }

  public static IAnnotationGenerator<?> createTunnelToServer() {
    return create().withElementNameFrom(IScoutApi.class, IScoutApi::TunnelToServer);
  }

  public static IAnnotationGenerator<?> createTest() {
    return create().withElementNameFrom(IScoutApi.class, IScoutApi::Test);
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
        .withElementNameFrom(IScoutApi.class, IScoutApi::RunWithSubject)
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
    var session = IClassNameSupplier.raw(clientSession);
    return createRunWithClientSession(null, api -> session);
  }

  public static <API extends IApiSpecification> IAnnotationGenerator<?> createRunWithClientSession(ApiFunction<API, IClassNameSupplier> clientSessionProvider) {
    if (clientSessionProvider == null) {
      return createRunWithClientSession(null, null);
    }
    return createRunWithClientSession(clientSessionProvider.apiClass().orElse(null), clientSessionProvider.apiFunction());
  }

  public static <API extends IApiSpecification> IAnnotationGenerator<?> createRunWithClientSession(Class<API> apiSpec, Function<API, IClassNameSupplier> clientSessionProvider) {
    ApiFunction<API, IClassNameSupplier> sessionFunction;
    if (clientSessionProvider == null) {
      // use default session
      var session = IClassNameSupplier.raw(null);
      sessionFunction = new ApiFunction<>(null, api -> session);
    }
    else {
      sessionFunction = new ApiFunction<>(apiSpec, clientSessionProvider);
    }

    return create()
        .withElementNameFrom(IScoutApi.class, IScoutApi::RunWithClientSession)
        .withElementFrom(IScoutApi.class, api -> api.RunWithClientSession().valueElementName(),
            b -> b.classLiteral(
                sessionFunction.apply(b.context())
                    .map(IClassNameSupplier::fqn)
                    .filter(Strings::hasText)
                    .orElseGet(() -> b.context().requireApi(IScoutApi.class).TestEnvironmentClientSession().fqn())));
  }

  public static IAnnotationGenerator<?> createRunWithServerSession(CharSequence serverSession) {
    var session = IClassNameSupplier.raw(serverSession);
    return createRunWithServerSession(null, api -> session);
  }

  public static <API extends IApiSpecification> IAnnotationGenerator<?> createRunWithServerSession(ApiFunction<API, IClassNameSupplier> serverSessionProvider) {
    Ensure.notNull(serverSessionProvider);
    return createRunWithServerSession(serverSessionProvider.apiClass().orElse(null), serverSessionProvider.apiFunction());
  }

  public static <API extends IApiSpecification> IAnnotationGenerator<?> createRunWithServerSession(Class<API> apiSpec, Function<API, IClassNameSupplier> serverSessionProvider) {
    Ensure.notNull(serverSessionProvider);
    return create()
        .withElementNameFrom(IScoutApi.class, IScoutApi::RunWithServerSession)
        .withElementFrom(IScoutApi.class, api -> api.RunWithServerSession().valueElementName(), b -> b.classLiteralFrom(apiSpec, serverSessionProvider));
  }

  public static IAnnotationGenerator<?> createRunWith(CharSequence runner) {
    return create()
        .withElementNameFrom(IScoutApi.class, IScoutApi::RunWith)
        .withElementFrom(IScoutApi.class, api -> api.RunWith().valueElementName(), b -> b.classLiteral(Ensure.notBlank(runner)));
  }

  public static IAnnotationGenerator<?> createData(CharSequence pageDataType) {
    return create()
        .withElementNameFrom(IScoutApi.class, IScoutApi::Data)
        .withElementFrom(IScoutApi.class, api -> api.Data().valueElementName(), b -> b.classLiteral(Ensure.notBlank(pageDataType)));
  }

  public static IAnnotationGenerator<?> createFormData() {
    return createFormData(null, null, null);
  }

  public static IAnnotationGenerator<?> createFormData(CharSequence formDataClass, SdkCommand sdkCommand, DefaultSubtypeSdkCommand defaultSubtypeCommand) {
    var generator = create().withElementNameFrom(IScoutApi.class, IScoutApi::FormData);
    if (Strings.hasText(formDataClass)) {
      generator.withElementFrom(IScoutApi.class, api -> api.FormData().valueElementName(), b -> b.classLiteral(formDataClass));
    }
    if (sdkCommand != null && SdkCommand.DEFAULT != sdkCommand) {
      generator.withElementFrom(IScoutApi.class, api -> api.FormData().sdkCommandElementName(), b -> b.refFrom(IScoutApi.class, api -> api.FormData().fqn()).append(".SdkCommand.").append(sdkCommand.name()));
    }
    if (defaultSubtypeCommand != null && DefaultSubtypeSdkCommand.DEFAULT != defaultSubtypeCommand) {
      generator.withElementFrom(IScoutApi.class, api -> api.FormData().defaultSubtypeSdkCommandElementName(),
          b -> b.ref(generator.elementName().get()).append(".DefaultSubtypeSdkCommand.").append(defaultSubtypeCommand.name()));
    }
    return generator;
  }

  public static IAnnotationGenerator<?> createReplace() {
    return create().withElementNameFrom(IScoutApi.class, IScoutApi::Replace);
  }
}
