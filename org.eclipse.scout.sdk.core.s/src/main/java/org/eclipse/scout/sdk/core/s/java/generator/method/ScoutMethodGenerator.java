/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.generator.method;

import static org.eclipse.scout.sdk.core.util.Strings.capitalize;

import java.util.Set;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.builder.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.java.model.api.PropertyBean;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutVariousApi;
import org.eclipse.scout.sdk.core.s.java.builder.body.IScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.s.java.builder.body.ScoutMethodBodyBuilder;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link ScoutMethodGenerator}</h3>
 * 
 * @see ScoutDoMethodGenerator
 * @since 6.1.0
 */
public class ScoutMethodGenerator<TYPE extends IScoutMethodGenerator<TYPE, BODY>, BODY extends IScoutMethodBodyBuilder<?>> extends MethodGenerator<TYPE, BODY> implements IScoutMethodGenerator<TYPE, BODY> {

  protected ScoutMethodGenerator() {
  }

  protected ScoutMethodGenerator(IMethod method, IWorkingCopyTransformer transformer) {
    super(method, transformer);
  }

  public static IScoutMethodGenerator<?, ?> create(IMethod method, IWorkingCopyTransformer transformer) {
    return new ScoutMethodGenerator<>(method, transformer);
  }

  public static IScoutMethodGenerator<?, ?> create() {
    return new ScoutMethodGenerator<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected BODY createMethodBodyBuilder(ISourceBuilder<?> inner) {
    return (BODY) ScoutMethodBodyBuilder.create(inner, this);
  }

  /**
   * Creates an {@link IScoutMethodGenerator} which creates a form field getter of the form {@code public MyField
   * getMyField() { return getFieldByClass(MyField.class); }}
   *
   * @param fieldFqn
   *          The fully qualified name of the form field.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createFieldGetter(String fieldFqn) {
    var dotBasedFqn = Ensure.notBlank(fieldFqn).replace(JavaTypes.C_DOLLAR, JavaTypes.C_DOT);
    var fieldSimpleName = JavaTypes.simpleName(dotBasedFqn);
    return create()
        .asPublic()
        .withElementName(PropertyBean.GETTER_PREFIX + capitalize(fieldSimpleName))
        .withReturnType(fieldFqn)
        .withBody(b -> b.returnClause().appendGetFieldByClass(fieldFqn).semicolon());
  }

  /**
   * Creates a protected method that returns the text value of the given translation key of the form {@code @Override
   * protected methodName() { return TEXTS.get("nlsKeyName"); }}
   *
   * @param methodName
   *          The name of the method
   * @param nlsKeyName
   *          The nls key to insert
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createNlsMethod(String methodName, CharSequence nlsKeyName) {
    return createNlsMethodFrom(null, api -> methodName, nlsKeyName);
  }

  /**
   * Creates a protected method that returns the text value of the given translation key of the form {@code @Override
   * protected methodName() { return TEXTS.get("nlsKeyName"); }}
   *
   * @param api
   *          The {@link IApiSpecification} that contains the method name.
   * @param methodFunction
   *          A function that gets the required method name from the input api.
   * @param nlsKeyName
   *          The nls key to insert
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static <API extends IApiSpecification> IScoutMethodGenerator<?, ?> createNlsMethodFrom(Class<API> api, Function<API, String> methodFunction, CharSequence nlsKeyName) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .asProtected()
        .withElementNameFrom(api, methodFunction)
        .withReturnType(String.class.getName())
        .withBody(b -> b.appendTodo("verify translation")
            .returnClause().refClassFrom(IScoutApi.class, IScoutVariousApi::TEXTS).dot()
            .appendFrom(IScoutApi.class, a -> a.TEXTS().getMethodName()).parenthesisOpen().stringLiteral(nlsKeyName).parenthesisClose().semicolon());
  }

  /**
   * Creates a getConfiguredDisplayable method of the form:
   *
   * <pre>
   * &#64;Override
   * protected boolean getConfiguredDisplayable() {
   *   return true;
   * }
   * </pre>
   *
   * @param displayable
   *          The boolean return value.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredDisplayable(boolean displayable) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractColumn().getConfiguredDisplayableMethodName())
        .asProtected()
        .withReturnType(JavaTypes._boolean)
        .withBody(b -> b.returnClause().append(displayable).semicolon());
  }

  /**
   * Creates a getConfiguredPrimaryKey method of the form:
   *
   * <pre>
   * &#64;Override
   * protected boolean getConfiguredPrimaryKey() {
   *   return false;
   * }
   * </pre>
   *
   * @param primaryKey
   *          The boolean return value.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredPrimaryKey(boolean primaryKey) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractColumn().getConfiguredPrimaryKeyMethodName())
        .asProtected()
        .withReturnType(JavaTypes._boolean)
        .withBody(b -> b.returnClause().append(primaryKey).semicolon());
  }

  /**
   * Creates a getConfiguredWidth method of the form:
   *
   * <pre>
   * &#64;Override
   * protected int getConfiguredWidth() {
   *   return 60;
   * }
   * </pre>
   *
   * @param width
   *          The int return value.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredWidth(int width) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .asProtected()
        .withReturnType(JavaTypes._int)
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractColumn().getConfiguredWidthMethodName())
        .withBody(b -> b.returnClause().append(width).semicolon());
  }

  /**
   * Creates a getConfiguredMenuTypes method of the form:
   *
   * <pre>
   * &#64;Override
   * protected Set<? extends IMenuType> getConfiguredMenuTypes() {
   *   return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
   * }
   * </pre>
   *
   * @param menuTypes
   *          A sourceGenerator that allows to add menu types.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredMenuTypes(ISourceGenerator<IExpressionBuilder<?>> menuTypes) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractMenu().getConfiguredMenuTypesMethodName())
        .asProtected()
        .withReturnTypeFrom(IScoutApi.class, api -> new StringBuilder(Set.class.getName()).append(JavaTypes.C_GENERIC_START)
            .append(JavaTypes.C_QUESTION_MARK).append(' ').append(JavaTypes.EXTENDS).append(' ').append(api.IMenuType().fqn())
            .append(JavaTypes.C_GENERIC_END).toString())
        .withBody(b -> {
          b.returnClause().refClassFrom(IScoutApi.class, IScoutApi::CollectionUtility).dot().appendFrom(IScoutApi.class, api -> api.CollectionUtility().hashSetMethodName()).parenthesisOpen();
          if (menuTypes != null) {
            b.append(menuTypes.generalize(ExpressionBuilder::create));
          }
          b.parenthesisClose().semicolon();
        });
  }

  /**
   * Creates a getConfiguredMandatory method of the form:
   *
   * <pre>
   * &#64;Override
   * protected boolean getConfiguredMandatory() {
   *   return false;
   * }
   * </pre>
   *
   * @param mandatory
   *          The boolean return value.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredMandatory(boolean mandatory) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractFormField().getConfiguredMandatoryMethodName())
        .asProtected()
        .withReturnType(JavaTypes._boolean)
        .withBody(b -> b.returnClause().append(mandatory).semicolon());
  }

  /**
   * Creates a getConfiguredMaxLength method of the form:
   *
   * <pre>
   * &#64;Override
   * protected int getConfiguredMaxLength() {
   *   return 60;
   * }
   * </pre>
   *
   * @param maxLength
   *          The int return value.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredMaxLength(int maxLength) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .asProtected()
        .withReturnType(JavaTypes._int)
        .withElementNameFrom(IScoutApi.class, api -> api.AbstractStringField().getConfiguredMaxLengthMethodName())
        .withBody(b -> b.returnClause().append(maxLength).semicolon());
  }

  /**
   * Creates a getConfiguredService method of the form:
   *
   * <pre>
   * &#64;Override
   * protected Class<? extends ILookupService<EntityKey>> getConfiguredService() {
   *   return IEntityLookupService.class;
   * }
   * </pre>
   *
   * @param serviceIfc
   *          The fully qualified name of the service interface.
   * @param keyType
   *          The fully qualified name of the key type.
   * @return The created {@link IScoutMethodGenerator}.
   */
  public static IScoutMethodGenerator<?, ?> createGetConfiguredService(CharSequence serviceIfc, String keyType) {
    return create()
        .withAnnotation(AnnotationGenerator.createOverride())
        .asProtected()
        .withReturnTypeFrom(IScoutApi.class, api -> new StringBuilder(Class.class.getName()).append(JavaTypes.C_GENERIC_START)
            .append(JavaTypes.C_QUESTION_MARK).append(JavaTypes.C_SPACE).append(JavaTypes.EXTENDS).append(JavaTypes.C_SPACE)
            .append(api.ILookupService().fqn()).append(JavaTypes.C_GENERIC_START).append(keyType).append(JavaTypes.C_GENERIC_END)
            .append(JavaTypes.C_GENERIC_END).toString())
        .withElementNameFrom(IScoutApi.class, api -> api.LookupCall().getConfiguredServiceMethodName())
        .withBody(b -> b.returnClause().classLiteral(serviceIfc).semicolon());
  }
}
