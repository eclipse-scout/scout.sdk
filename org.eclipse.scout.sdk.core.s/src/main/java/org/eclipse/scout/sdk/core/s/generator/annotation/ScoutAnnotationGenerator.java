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

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.annotation.IAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.annotation.ClassIdAnnotation;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotation;
import org.eclipse.scout.sdk.core.s.annotation.ExtendsAnnotation;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.DefaultSubtypeSdkCommand;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotation.SdkCommand;
import org.eclipse.scout.sdk.core.s.annotation.OrderAnnotation;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link ScoutAnnotationGenerator}</h3>
 *
 * @since 6.1.0
 */
public class ScoutAnnotationGenerator<TYPE extends ScoutAnnotationGenerator<TYPE>> extends AnnotationGenerator<TYPE> {

  public static IAnnotationGenerator<?> createOrder(double orderNr) {
    return create()
        .withElementName(IScoutRuntimeTypes.Order)
        .withElement(OrderAnnotation.VALUE_ELEMENT_NAME, OrderAnnotation.convertToJavaSource(orderNr));
  }

  public static IAnnotationGenerator<?> createExtends(CharSequence extendedType) {
    return create()
        .withElementName(IScoutRuntimeTypes.Extends)
        .withElement(ExtendsAnnotation.VALUE_ELEMENT_NAME, b -> b.classLiteral(extendedType));
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
        .withElementName(IScoutRuntimeTypes.ClassId)
        .withElement(ClassIdAnnotation.VALUE_ELEMENT_NAME, b -> b.stringLiteral(classIdValue));
  }

  public static IAnnotationGenerator<?> createBefore() {
    return create()
        .withElementName(IScoutRuntimeTypes.Before);
  }

  public static IAnnotationGenerator<?> createApplicationScoped() {
    return create()
        .withElementName(IScoutRuntimeTypes.ApplicationScoped);
  }

  public static IAnnotationGenerator<?> createBeanMock() {
    return create()
        .withElementName(IScoutRuntimeTypes.BeanMock);
  }

  public static IAnnotationGenerator<?> createAuthentication() {
    return create()
        .withElementName(IScoutRuntimeTypes.Authentication)
        .withElement("method", b -> createClazz(IScoutRuntimeTypes.BasicAuthenticationMethod).generate(b))
        .withElement("verifier", b -> createClazz(IScoutRuntimeTypes.ConfigFileCredentialVerifier).generate(b));
  }

  public static IAnnotationGenerator<?> createClazz(CharSequence clazzFqn) {
    return create()
        .withElementName(IScoutRuntimeTypes.Clazz)
        .withElement("value", b -> b.classLiteral(clazzFqn));
  }

  public static IAnnotationGenerator<?> createHandler(CharSequence handlerFqn) {
    return create()
        .withElementName(IScoutRuntimeTypes.Handler)
        .withElement("value", b -> createClazz(handlerFqn).generate(b));
  }

  public static IAnnotationGenerator<?> createTunnelToServer() {
    return create()
        .withElementName(IScoutRuntimeTypes.TunnelToServer);
  }

  public static IAnnotationGenerator<?> createTest() {
    return create()
        .withElementName(IScoutRuntimeTypes.Test);
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
        .withElementName(IScoutRuntimeTypes.RunWithSubject)
        .withElement("value", Optional.ofNullable(valueBuilder)
            .orElseGet(() -> b -> b.stringLiteral("anonymous")));
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
  public static IAnnotationGenerator<?> createRunWithClientSession(String clientSession) {
    return create()
        .withElementName(IScoutRuntimeTypes.RunWithClientSession)
        .withElement("value", b -> b.classLiteral(Optional.ofNullable(clientSession)
            .orElse(IScoutRuntimeTypes.TestEnvironmentClientSession)));
  }

  public static IAnnotationGenerator<?> createRunWithServerSession(String serverSession) {
    return create()
        .withElementName(IScoutRuntimeTypes.RunWithServerSession)
        .withElement("value", b -> b.classLiteral(Ensure.notNull(serverSession)));
  }

  public static IAnnotationGenerator<?> createRunWith(String runner) {
    return create()
        .withElementName(IScoutRuntimeTypes.RunWith)
        .withElement("value", b -> b.classLiteral(Ensure.notNull(runner)));
  }

  public static IAnnotationGenerator<?> createData(String pageDataType) {
    return create()
        .withElementName(IScoutRuntimeTypes.Data)
        .withElement(DataAnnotation.VALUE_ELEMENT_NAME, b -> b.classLiteral(Ensure.notNull(pageDataType)));
  }

  public static IAnnotationGenerator<?> createFormData() {
    return createFormData(null, null, null);
  }

  public static IAnnotationGenerator<?> createFormData(CharSequence formDataClass, SdkCommand sdkCommand, DefaultSubtypeSdkCommand defaultSubtypeCommand) {
    IAnnotationGenerator<?> generator = create()
        .withElementName(IScoutRuntimeTypes.FormData);
    if (formDataClass != null) {
      generator.withElement(FormDataAnnotation.VALUE_ELEMENT_NAME, b -> b.classLiteral(formDataClass));
    }
    if (sdkCommand != null && SdkCommand.DEFAULT != sdkCommand) {
      generator.withElement(FormDataAnnotation.SDK_COMMAND_ELEMENT_NAME, b -> b.ref(generator.elementName().get()).append(".SdkCommand.").append(sdkCommand.name()));
    }

    if (defaultSubtypeCommand != null && DefaultSubtypeSdkCommand.DEFAULT != defaultSubtypeCommand) {
      generator.withElement(FormDataAnnotation.DEFAULT_SUBTYPE_SDK_COMMAND_ELEMENT_NAME, b -> b.ref(generator.elementName().get()).append(".DefaultSubtypeSdkCommand.").append(defaultSubtypeCommand.name()));
    }
    return generator;
  }

  public static IAnnotationGenerator<?> createReplace() {
    return create()
        .withElementName(IScoutRuntimeTypes.Replace);
  }
}
